package com.datenbank.backend.service;

import com.datenbank.backend.dto.ContentSummaryDto;
import com.datenbank.backend.dto.ItemCreateDto;
import com.datenbank.backend.dto.ItemResponseDto;
import com.datenbank.backend.dto.ValidatorResponseDto;
import com.datenbank.backend.entity.*;
import com.datenbank.backend.repository.*;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final AuthorRepository authorRepository;
    private final LicenseRepository licenseRepository;
    private final ItemTypeRepository itemTypeRepository;
    private final ItemRepresentationTemplateRepository templateRepository;
    private final TagRepository tagRepository;
    private final ValidatorRepository validatorRepository;
    private final ModifierRepository modifierRepository;
    private final ItemCollectionRepository collectionRepository;
    private final ItemContentsRepository itemContentsRepository;

    public ItemService(ItemRepository itemRepository,
                       AuthorRepository authorRepository,
                       LicenseRepository licenseRepository,
                       ItemTypeRepository itemTypeRepository,
                       ItemRepresentationTemplateRepository templateRepository,
                       TagRepository tagRepository,
                       ValidatorRepository validatorRepository,
                       ModifierRepository modifierRepository,
                       ItemCollectionRepository collectionRepository,
                       ItemContentsRepository itemContentsRepository) {
        this.itemRepository = itemRepository;
        this.authorRepository = authorRepository;
        this.licenseRepository = licenseRepository;
        this.itemTypeRepository = itemTypeRepository;
        this.templateRepository = templateRepository;
        this.tagRepository = tagRepository;
        this.validatorRepository = validatorRepository;
        this.modifierRepository = modifierRepository;
        this.collectionRepository = collectionRepository;
        this.itemContentsRepository = itemContentsRepository;
    }

    @Transactional(readOnly = true)
    public List<ItemResponseDto> getAllItems() {
        return itemRepository.findAll().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ItemResponseDto getItemById(UUID id) {
        Item item = findItemOrThrow(id);
        return convertToResponseDto(item);
    }

    @Transactional
    public ItemResponseDto createItem(ItemCreateDto dto) {
        Item item = new Item();
        applyDtoToEntity(dto, item);
        Item saved = itemRepository.save(item);
        return convertToResponseDto(saved);
    }

    @Transactional
    public ItemResponseDto updateItem(UUID id, ItemCreateDto dto) {
        Item item = findItemOrThrow(id);
        applyDtoToEntity(dto, item);
        Item saved = itemRepository.save(item);
        return convertToResponseDto(saved);
    }

    @Transactional
    public void deleteItem(UUID id) {
        if (!itemRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item nicht gefunden");
        }
        itemRepository.deleteById(id);
    }

    /**
     * Idempotent, da Set.
     */
    @Transactional
    public ItemResponseDto addTag(UUID itemId, UUID tagId) {
        Item item = findItemOrThrow(itemId);
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag nicht gefunden"));
        item.getTags().add(tag);
        return convertToResponseDto(itemRepository.save(item));
    }

    @Transactional
    public ItemResponseDto removeTag(UUID itemId, UUID tagId) {
        Item item = findItemOrThrow(itemId);
        item.getTags().removeIf(t -> t.getTagId().equals(tagId));
        return convertToResponseDto(itemRepository.save(item));
    }

    @Transactional(readOnly = true)
    public List<ItemResponseDto> getRootItems() {
        return itemRepository.findByRootItemIsNull().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ItemResponseDto> getItemsByRootId(UUID rootItemId) {
        return itemRepository.findByRootItem_ItemId(rootItemId).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Durchsucht und filtert Items nach verschiedenen Kriterien.
     * Alle Parameter sind optional — nur gesetzte Filter werden angewendet.
     */
    @Transactional(readOnly = true)
    public List<ItemResponseDto> searchItems(String search, UUID authorId, UUID itemTypeId, String tag) {
        Specification<Item> spec = Specification.where(null);

        if (StringUtils.hasText(search)) {
            spec = spec.and((root, query, cb) -> {
                Subquery<Long> subquery = query.subquery(Long.class);
                Root<ItemContents> icRoot = subquery.from(ItemContents.class);
                var cntJoin = icRoot.join("itemContent");

                subquery.select(cb.literal(1L));
                subquery.where(
                    cb.equal(icRoot.get("item"), root),
                    cb.like(cb.lower(cb.function("text", String.class,
                            cntJoin.get("jsonSerializedContent"))),
                            "%" + search.toLowerCase() + "%")
                );

                return cb.exists(subquery);
            });
        }

        if (authorId != null) {
            spec = spec.and((root, query, cb) ->
                cb.equal(root.get("author").get("authorId"), authorId));
        }

        if (itemTypeId != null) {
            spec = spec.and((root, query, cb) ->
                cb.equal(root.get("itemType").get("itemTypeId"), itemTypeId));
        }

        if (StringUtils.hasText(tag)) {
            spec = spec.and((root, query, cb) -> {
                query.distinct(true);
                Join<Item, Tag> tagJoin = root.join("tags", JoinType.LEFT);
                return cb.like(cb.lower(tagJoin.get("tag")),
                               "%" + tag.toLowerCase() + "%");
            });
        }

        return itemRepository.findAll(spec).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ItemResponseDto convertToCollection(UUID itemId) {
        Item item = findItemOrThrow(itemId);

        // Idempotent: hat das Item bereits eine Kollektion, wird keine zweite
        // angelegt (verhindert Duplikate bei Doppelklick bzw. Doppel-Request).
        if (!collectionRepository.existsByParentItem_ItemId(itemId)) {
            ItemCollection collection = new ItemCollection();
            collection.setParentItem(item);
            collection.setCollectionOrder(false);
            collectionRepository.save(collection);
        }

        return convertToResponseDto(item);
    }

    @Transactional(readOnly = true)
    public List<ValidatorResponseDto> getValidatorsForItem(UUID itemId) {
        Item item = findItemOrThrow(itemId);
        return item.getValidators().stream()
                .map(v -> {
                    ValidatorResponseDto dto = new ValidatorResponseDto();
                    dto.setValidatorId(v.getValidatorId());
                    dto.setDescription(v.getDescription());
                    dto.setValidator(v.getValidator());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public ItemResponseDto addValidatorToItem(UUID itemId, UUID validatorId) {
        Item item = findItemOrThrow(itemId);
        Validator validator = validatorRepository.findById(validatorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Validator nicht gefunden"));
        item.getValidators().add(validator);
        Item saved = itemRepository.save(item);
        return convertToResponseDto(saved);
    }

    @Transactional
    public ItemResponseDto removeValidatorFromItem(UUID itemId, UUID validatorId) {
        Item item = findItemOrThrow(itemId);
        boolean removed = item.getValidators().removeIf(v -> v.getValidatorId().equals(validatorId));
        if (!removed) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Validator nicht mit diesem Item verknüpft");
        }
        Item saved = itemRepository.save(item);
        return convertToResponseDto(saved);
    }

    private Item findItemOrThrow(UUID id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Item nicht gefunden"));
    }

    private void applyDtoToEntity(ItemCreateDto dto, Item item) {
        // NOT NULL
        item.setAuthor(authorRepository.findById(dto.getAuthorId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Author nicht gefunden")));

        item.setLicense(licenseRepository.findById(dto.getLicenseId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "License nicht gefunden")));

        item.setItemType(itemTypeRepository.findById(dto.getItemTypeId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "ItemType nicht gefunden")));

        // Optionale Beziehungen (können null sein)
        if (dto.getItemTemplateId() != null) {
            item.setItemTemplate(templateRepository.findById(dto.getItemTemplateId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Template nicht gefunden")));
        } else {
            item.setItemTemplate(null);
        }

        if (dto.getRootItemId() != null) {
            item.setRootItem(itemRepository.findById(dto.getRootItemId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Root Item nicht gefunden")));
        } else {
            item.setRootItem(null);
        }

        // Many-to-Many: Tags
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(dto.getTagIds()));
            if (tags.size() != dto.getTagIds().size()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Mindestens ein Tag wurde nicht gefunden");
            }
            item.setTags(tags);
        } else {
            item.setTags(new HashSet<>());
        }

        // Many-to-Many: Validators
        if (dto.getValidatorIds() != null && !dto.getValidatorIds().isEmpty()) {
            Set<Validator> validators = new HashSet<>(
                    validatorRepository.findAllById(dto.getValidatorIds()));
            if (validators.size() != dto.getValidatorIds().size()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Mindestens ein Validator wurde nicht gefunden");
            }
            item.setValidators(validators);
        } else {
            item.setValidators(new HashSet<>());
        }

        // Many-to-Many: Modifiers
        if (dto.getModifierIds() != null && !dto.getModifierIds().isEmpty()) {
            Set<Modifier> modifiers = new HashSet<>(
                    modifierRepository.findAllById(dto.getModifierIds()));
            if (modifiers.size() != dto.getModifierIds().size()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Mindestens ein Modifier wurde nicht gefunden");
            }
            item.setModifiers(modifiers);
        } else {
            item.setModifiers(new HashSet<>());
        }
    }

    private ItemResponseDto convertToResponseDto(Item item) {
        ItemResponseDto dto = new ItemResponseDto();

        dto.setItemId(item.getItemId());

        if (item.getAuthor() != null) {
            dto.setAuthorId(item.getAuthor().getAuthorId());
            dto.setAuthorDescriptor(item.getAuthor().getDescriptor());
        }

        if (item.getLicense() != null) {
            dto.setLicenseId(item.getLicense().getLicenseId());
            dto.setLicenseName(item.getLicense().getLicense());
        }

        if (item.getItemType() != null) {
            dto.setItemTypeId(item.getItemType().getItemTypeId());
            dto.setItemTypeName(item.getItemType().getItemTypeName());
        }

        if (item.getItemTemplate() != null) {
            dto.setItemTemplateId(item.getItemTemplate().getItemTemplateId());
        }

        if (item.getRootItem() != null) {
            dto.setRootItemId(item.getRootItem().getItemId());
        }

        dto.setTagIds(item.getTags().stream()
                .map(Tag::getTagId)
                .collect(Collectors.toSet()));

        dto.setValidatorIds(item.getValidators().stream()
                .map(Validator::getValidatorId)
                .collect(Collectors.toSet()));

        dto.setModifierIds(item.getModifiers().stream()
                .map(Modifier::getModifierId)
                .collect(Collectors.toSet()));

        dto.setCreatedAt(item.getCreatedAt());
        dto.setUpdatedAt(item.getUpdatedAt());

        // 1.3 isCollection + order-Flag setzen (eine Abfrage statt nur exists)
    ItemCollection collection = collectionRepository
        .findFirstByParentItem_ItemId(item.getItemId())
        .orElse(null);
    dto.setCollection(collection != null);
    if (collection != null) {
        dto.setOrder(collection.getCollectionOrder());
        dto.setCollectionId(collection.getItemCollectionId());
    }

    // 1.4 contents setzen
    List<ContentSummaryDto> contentSummaries = itemContentsRepository
        .findByItem_ItemId(item.getItemId())
        .stream()
        .map(ic -> {
            ContentSummaryDto summary = new ContentSummaryDto();
            summary.setItemContentId(
                ic.getItemContent().getItemContentId());
            summary.setItemContentTypeName(
                ic.getItemContent().getItemContentType()
                    .getItemContentTypeName());
            summary.setHasJsonContent(
                ic.getItemContent().getJsonSerializedContent() != null);
            summary.setHasBlobContent(
                ic.getItemContent().getBlobSerializedContent() != null);
            return summary;
        })
        .collect(Collectors.toList());

    dto.setContents(contentSummaries);

    return dto;
    }
}