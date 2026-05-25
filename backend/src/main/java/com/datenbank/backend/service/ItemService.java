package com.datenbank.backend.service;

import com.datenbank.backend.dto.ItemCreateDto;
import com.datenbank.backend.dto.ItemResponseDto;
import com.datenbank.backend.entity.*;
import com.datenbank.backend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service-Schicht für die Geschäftslogik der Item-Entität.
 *
 * Aufgaben:
 *  - Umwandlung zwischen DTOs (Frontend) und Entities (Datenbank)
 *  - Validierung referenzierter Entitäten (Author, License, etc.)
 *  - CRUD-Operationen mit korrekter Fehlerbehandlung
 */
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

    /**
     * Constructor-Injection: Spring übergibt automatisch die Repositories.
     * Modern und besser testbar als @Autowired auf Feldern.
     */
    public ItemService(ItemRepository itemRepository,
                       AuthorRepository authorRepository,
                       LicenseRepository licenseRepository,
                       ItemTypeRepository itemTypeRepository,
                       ItemRepresentationTemplateRepository templateRepository,
                       TagRepository tagRepository,
                       ValidatorRepository validatorRepository,
                       ModifierRepository modifierRepository) {
        this.itemRepository = itemRepository;
        this.authorRepository = authorRepository;
        this.licenseRepository = licenseRepository;
        this.itemTypeRepository = itemTypeRepository;
        this.templateRepository = templateRepository;
        this.tagRepository = tagRepository;
        this.validatorRepository = validatorRepository;
        this.modifierRepository = modifierRepository;
    }

    // =========================================================================
    // CRUD-Operationen
    // =========================================================================

    /**
     * Liefert alle Items als Liste von ResponseDTOs.
     */
    @Transactional(readOnly = true)
    public List<ItemResponseDto> getAllItems() {
        return itemRepository.findAll().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Liefert ein einzelnes Item als ResponseDTO.
     * Wirft 404, falls nicht gefunden.
     */
    @Transactional(readOnly = true)
    public ItemResponseDto getItemById(Integer id) {
        Item item = findItemOrThrow(id);
        return convertToResponseDto(item);
    }

    /**
     * Erstellt ein neues Item aus einem CreateDTO.
     */
    @Transactional
    public ItemResponseDto createItem(ItemCreateDto dto) {
        Item item = new Item();
        applyDtoToEntity(dto, item);
        Item saved = itemRepository.save(item);
        return convertToResponseDto(saved);
    }

    /**
     * Aktualisiert ein existierendes Item.
     * Wirft 404, falls das Item nicht existiert.
     */
    @Transactional
    public ItemResponseDto updateItem(Integer id, ItemCreateDto dto) {
        Item item = findItemOrThrow(id);
        applyDtoToEntity(dto, item);
        Item saved = itemRepository.save(item);
        return convertToResponseDto(saved);
    }

    /**
     * Löscht ein Item.
     * Wirft 404, falls das Item nicht existiert.
     */
    @Transactional
    public void deleteItem(Integer id) {
        if (!itemRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item nicht gefunden");
        }
        itemRepository.deleteById(id);
    }

    // =========================================================================
    // Hilfsmethoden (private)
    // =========================================================================

    /**
     * Holt ein Item aus der DB oder wirft 404.
     */
    private Item findItemOrThrow(Integer id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Item nicht gefunden"));
    }

    /**
     * Wendet die Daten eines CreateDTO auf eine Item-Entity an.
     * Wird sowohl bei Create als auch bei Update verwendet.
     */
    private void applyDtoToEntity(ItemCreateDto dto, Item item) {
        // Pflicht-Beziehungen (NOT NULL)
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

    /**
     * Wandelt eine Item-Entity in ein ResponseDTO um.
     */
    private ItemResponseDto convertToResponseDto(Item item) {
        ItemResponseDto dto = new ItemResponseDto();

        dto.setItemId(item.getItemId());

        // Author
        if (item.getAuthor() != null) {
            dto.setAuthorId(item.getAuthor().getAuthorId());
            dto.setAuthorDescriptor(item.getAuthor().getDescriptor());
        }

        // License
        if (item.getLicense() != null) {
            dto.setLicenseId(item.getLicense().getLicenseId());
            dto.setLicenseName(item.getLicense().getLicense());
        }

        // ItemType
        if (item.getItemType() != null) {
            dto.setItemTypeId(item.getItemType().getItemTypeId());
            dto.setItemTypeName(item.getItemType().getItemTypeName());
        }

        // Optionale Beziehungen
        if (item.getItemTemplate() != null) {
            dto.setItemTemplateId(item.getItemTemplate().getItemTemplateId());
        }

        if (item.getRootItem() != null) {
            dto.setRootItemId(item.getRootItem().getItemId());
        }

        // Many-to-Many als IDs
        dto.setTagIds(item.getTags().stream()
                .map(Tag::getTagId)
                .collect(Collectors.toSet()));

        dto.setValidatorIds(item.getValidators().stream()
                .map(Validator::getValidatorId)
                .collect(Collectors.toSet()));

        dto.setModifierIds(item.getModifiers().stream()
                .map(Modifier::getModifierId)
                .collect(Collectors.toSet()));

        // Timestamps
        dto.setCreatedAt(item.getCreatedAt());
        dto.setUpdatedAt(item.getUpdatedAt());

        return dto;
    }
}