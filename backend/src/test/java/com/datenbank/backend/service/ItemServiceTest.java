package com.datenbank.backend.service;

import com.datenbank.backend.dto.*;
import com.datenbank.backend.entity.*;
import com.datenbank.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    private static final UUID AUTHOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID LICENSE_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID ITEM_TYPE_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID TEMPLATE_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");
    private static final UUID TAG_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");
    private static final UUID VALIDATOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000006");
    private static final UUID MODIFIER_ID = UUID.fromString("00000000-0000-0000-0000-000000000007");
    private static final UUID ITEM_ID = UUID.fromString("00000000-0000-0000-0000-000000001000");
    private static final UUID NOT_FOUND_ID = UUID.fromString("00000000-0000-0000-0000-00000000FFFF");
    private static final UUID ROOT_ITEM_ID = UUID.fromString("00000000-0000-0000-0000-000000001001");

    @Mock private ItemRepository itemRepository;
    @Mock private AuthorRepository authorRepository;
    @Mock private LicenseRepository licenseRepository;
    @Mock private ItemTypeRepository itemTypeRepository;
    @Mock private ItemRepresentationTemplateRepository templateRepository;
    @Mock private TagRepository tagRepository;
    @Mock private ValidatorRepository validatorRepository;
    @Mock private ModifierRepository modifierRepository;
    @Mock private ItemCollectionRepository collectionRepository;
    @Mock private ItemContentsRepository itemContentsRepository;

    @InjectMocks
    private ItemService service;

    private Author author;
    private License license;
    private ItemType itemType;
    private ItemRepresentationTemplate template;
    private Tag tag;
    private Validator validator;
    private Modifier modifier;
    private Item item;
    private Item rootItem;
    private ItemCreateDto createDto;

    @BeforeEach
    void setUp() {
        author = new Author();
        author.setAuthorId(AUTHOR_ID);
        author.setDescriptor("Test Author");

        license = new License();
        license.setLicenseId(LICENSE_ID);
        license.setLicense("CC-BY-4.0");

        itemType = new ItemType();
        itemType.setItemTypeId(ITEM_TYPE_ID);
        itemType.setItemTypeName("SQL Exercise");

        template = new ItemRepresentationTemplate();
        template.setItemTemplateId(TEMPLATE_ID);
        template.setTemplate("<xml/>");

        tag = new Tag();
        tag.setTagId(TAG_ID);
        tag.setTag("sql");

        validator = new Validator();
        validator.setValidatorId(VALIDATOR_ID);
        validator.setDescription("Must contain JOIN");
        validator.setValidator("regex:.*JOIN.*");

        modifier = new Modifier();
        modifier.setModifierId(MODIFIER_ID);
        modifier.setDescription("Variant A");
        modifier.setModifier("...");

        rootItem = new Item();
        rootItem.setItemId(ROOT_ITEM_ID);
        rootItem.setAuthor(author);
        rootItem.setLicense(license);
        rootItem.setItemType(itemType);

        item = new Item();
        item.setItemId(ITEM_ID);
        item.setAuthor(author);
        item.setLicense(license);
        item.setItemType(itemType);
        item.setItemTemplate(template);
        item.setRootItem(rootItem);
        item.setTags(new HashSet<>(Set.of(tag)));
        item.setValidators(new HashSet<>(Set.of(validator)));
        item.setModifiers(new HashSet<>(Set.of(modifier)));
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());

        createDto = new ItemCreateDto();
        createDto.setAuthorId(AUTHOR_ID);
        createDto.setLicenseId(LICENSE_ID);
        createDto.setItemTypeId(ITEM_TYPE_ID);
        createDto.setItemTemplateId(TEMPLATE_ID);
        createDto.setRootItemId(ROOT_ITEM_ID);
        createDto.setTagIds(new HashSet<>(Set.of(TAG_ID)));
        createDto.setValidatorIds(new HashSet<>(Set.of(VALIDATOR_ID)));
        createDto.setModifierIds(new HashSet<>(Set.of(MODIFIER_ID)));
    }

    private void mockConvertDeps() {
        when(collectionRepository.findFirstByParentItem_ItemId(any(UUID.class)))
                .thenReturn(Optional.empty());
        when(itemContentsRepository.findByItem_ItemId(any(UUID.class)))
                .thenReturn(List.of());
    }

    // ================================================================
    // getAllItems
    // ================================================================

    @Test
    void getAllItems_returnsList() {
        when(itemRepository.findAll()).thenReturn(List.of(item));
        mockConvertDeps();

        List<ItemResponseDto> result = service.getAllItems();

        assertEquals(1, result.size());
        assertEquals(ITEM_ID, result.get(0).getItemId());
    }

    @Test
    void getAllItems_emptyList() {
        when(itemRepository.findAll()).thenReturn(List.of());

        List<ItemResponseDto> result = service.getAllItems();

        assertTrue(result.isEmpty());
    }

    // ================================================================
    // getItemById
    // ================================================================

    @Test
    void getItemById_returnsItem() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        mockConvertDeps();

        ItemResponseDto result = service.getItemById(ITEM_ID);

        assertEquals(ITEM_ID, result.getItemId());
        assertEquals(AUTHOR_ID, result.getAuthorId());
        assertEquals("Test Author", result.getAuthorDescriptor());
        assertEquals(LICENSE_ID, result.getLicenseId());
        assertEquals("CC-BY-4.0", result.getLicenseName());
        assertEquals(ITEM_TYPE_ID, result.getItemTypeId());
        assertEquals("SQL Exercise", result.getItemTypeName());
    }

    @Test
    void getItemById_notFound_throws404() {
        when(itemRepository.findById(NOT_FOUND_ID)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> service.getItemById(NOT_FOUND_ID));
    }

    // ================================================================
    // createItem
    // ================================================================

    @Test
    void createItem_createsSuccessfully() {
        when(authorRepository.findById(AUTHOR_ID)).thenReturn(Optional.of(author));
        when(licenseRepository.findById(LICENSE_ID)).thenReturn(Optional.of(license));
        when(itemTypeRepository.findById(ITEM_TYPE_ID)).thenReturn(Optional.of(itemType));
        when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template));
        when(itemRepository.findById(ROOT_ITEM_ID)).thenReturn(Optional.of(rootItem));
        when(tagRepository.findAllById(Set.of(TAG_ID))).thenReturn(List.of(tag));
        when(validatorRepository.findAllById(Set.of(VALIDATOR_ID))).thenReturn(List.of(validator));
        when(modifierRepository.findAllById(Set.of(MODIFIER_ID))).thenReturn(List.of(modifier));
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        mockConvertDeps();

        ItemResponseDto result = service.createItem(createDto);

        assertEquals(ITEM_ID, result.getItemId());
        assertEquals(AUTHOR_ID, result.getAuthorId());
        assertEquals(LICENSE_ID, result.getLicenseId());
        assertEquals(ITEM_TYPE_ID, result.getItemTypeId());
        assertEquals(Set.of(TAG_ID), result.getTagIds());
        assertEquals(Set.of(VALIDATOR_ID), result.getValidatorIds());
        assertEquals(Set.of(MODIFIER_ID), result.getModifierIds());
    }

    @Test
    void createItem_noOptionals_createsSuccessfully() {
        createDto.setItemTemplateId(null);
        createDto.setRootItemId(null);
        createDto.setTagIds(new HashSet<>());
        createDto.setValidatorIds(new HashSet<>());
        createDto.setModifierIds(new HashSet<>());

        when(authorRepository.findById(AUTHOR_ID)).thenReturn(Optional.of(author));
        when(licenseRepository.findById(LICENSE_ID)).thenReturn(Optional.of(license));
        when(itemTypeRepository.findById(ITEM_TYPE_ID)).thenReturn(Optional.of(itemType));
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        mockConvertDeps();

        ItemResponseDto result = service.createItem(createDto);

        assertNotNull(result);
    }

    @Test
    void createItem_authorNotFound_throws404() {
        when(authorRepository.findById(AUTHOR_ID)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> service.createItem(createDto));
    }

    @Test
    void createItem_tagNotFound_throws404() {
        createDto.setItemTemplateId(null);
        createDto.setRootItemId(null);
        createDto.setModifierIds(new HashSet<>());
        createDto.setValidatorIds(new HashSet<>());

        when(authorRepository.findById(AUTHOR_ID)).thenReturn(Optional.of(author));
        when(licenseRepository.findById(LICENSE_ID)).thenReturn(Optional.of(license));
        when(itemTypeRepository.findById(ITEM_TYPE_ID)).thenReturn(Optional.of(itemType));
        when(tagRepository.findAllById(Set.of(TAG_ID))).thenReturn(List.of());

        assertThrows(ResponseStatusException.class,
                () -> service.createItem(createDto));
    }

    // ================================================================
    // updateItem
    // ================================================================

    @Test
    void updateItem_updatesSuccessfully() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(authorRepository.findById(AUTHOR_ID)).thenReturn(Optional.of(author));
        when(licenseRepository.findById(LICENSE_ID)).thenReturn(Optional.of(license));
        when(itemTypeRepository.findById(ITEM_TYPE_ID)).thenReturn(Optional.of(itemType));
        when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template));
        when(itemRepository.findById(ROOT_ITEM_ID)).thenReturn(Optional.of(rootItem));
        when(tagRepository.findAllById(Set.of(TAG_ID))).thenReturn(List.of(tag));
        when(validatorRepository.findAllById(Set.of(VALIDATOR_ID))).thenReturn(List.of(validator));
        when(modifierRepository.findAllById(Set.of(MODIFIER_ID))).thenReturn(List.of(modifier));
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        mockConvertDeps();

        ItemResponseDto result = service.updateItem(ITEM_ID, createDto);

        assertEquals(ITEM_ID, result.getItemId());
    }

    @Test
    void updateItem_notFound_throws404() {
        when(itemRepository.findById(NOT_FOUND_ID)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> service.updateItem(NOT_FOUND_ID, createDto));
    }

    // ================================================================
    // deleteItem
    // ================================================================

    @Test
    void deleteItem_deletes() {
        when(itemRepository.existsById(ITEM_ID)).thenReturn(true);

        service.deleteItem(ITEM_ID);

        verify(itemRepository).deleteById(ITEM_ID);
    }

    @Test
    void deleteItem_notFound_throws404() {
        when(itemRepository.existsById(NOT_FOUND_ID)).thenReturn(false);

        assertThrows(ResponseStatusException.class,
                () -> service.deleteItem(NOT_FOUND_ID));
    }

    // ================================================================
    // getRootItems / getItemsByRootId
    // ================================================================

    @Test
    void getRootItems_returnsRoots() {
        when(itemRepository.findByRootItemIsNull()).thenReturn(List.of(item));
        mockConvertDeps();

        List<ItemResponseDto> result = service.getRootItems();

        assertEquals(1, result.size());
    }

    @Test
    void getRootItems_emptyList() {
        when(itemRepository.findByRootItemIsNull()).thenReturn(List.of());

        List<ItemResponseDto> result = service.getRootItems();

        assertTrue(result.isEmpty());
    }

    @Test
    void getItemsByRootId_returnsChildren() {
        when(itemRepository.findByRootItem_ItemId(ROOT_ITEM_ID)).thenReturn(List.of(item));
        mockConvertDeps();

        List<ItemResponseDto> result = service.getItemsByRootId(ROOT_ITEM_ID);

        assertEquals(1, result.size());
    }

    // ================================================================
    // searchItems
    // ================================================================

    @Test
    void searchItems_withText_returnsFiltered() {
        when(itemRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class)))
                .thenReturn(List.of(item));
        mockConvertDeps();

        List<ItemResponseDto> result = service.searchItems("test", null, null, null);

        assertEquals(1, result.size());
    }

    @Test
    void searchItems_withAuthorId_returnsFiltered() {
        when(itemRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class)))
                .thenReturn(List.of(item));
        mockConvertDeps();

        List<ItemResponseDto> result = service.searchItems(null, AUTHOR_ID, null, null);

        assertEquals(1, result.size());
    }

    @Test
    void searchItems_withItemTypeId_returnsFiltered() {
        when(itemRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class)))
                .thenReturn(List.of(item));
        mockConvertDeps();

        List<ItemResponseDto> result = service.searchItems(null, null, ITEM_TYPE_ID, null);

        assertEquals(1, result.size());
    }

    @Test
    void searchItems_withTag_returnsFiltered() {
        when(itemRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class)))
                .thenReturn(List.of(item));
        mockConvertDeps();

        List<ItemResponseDto> result = service.searchItems(null, null, null, "sql");

        assertEquals(1, result.size());
    }

    @Test
    void searchItems_noParams_returnsAll() {
        when(itemRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class)))
                .thenReturn(List.of(item));
        mockConvertDeps();

        List<ItemResponseDto> result = service.searchItems(null, null, null, null);

        assertEquals(1, result.size());
    }

    // ================================================================
    // addTag / removeTag
    // ================================================================

    @Test
    void addTag_addsTagToItem() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(tagRepository.findById(TAG_ID)).thenReturn(Optional.of(tag));
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        mockConvertDeps();

        ItemResponseDto result = service.addTag(ITEM_ID, TAG_ID);

        assertNotNull(result);
    }

    @Test
    void addTag_tagNotFound_throws404() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(tagRepository.findById(TAG_ID)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> service.addTag(ITEM_ID, TAG_ID));
    }

    @Test
    void removeTag_removesTag() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        mockConvertDeps();

        ItemResponseDto result = service.removeTag(ITEM_ID, TAG_ID);

        assertNotNull(result);
    }

    @Test
    void addTag_itemNotFound_throws404() {
        when(itemRepository.findById(NOT_FOUND_ID)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> service.addTag(NOT_FOUND_ID, TAG_ID));
    }

    // ================================================================
    // Validator-Verknüpfungen
    // ================================================================

    @Test
    void getValidatorsForItem_returnsList() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));

        List<ValidatorResponseDto> result = service.getValidatorsForItem(ITEM_ID);

        assertEquals(1, result.size());
        assertEquals(VALIDATOR_ID, result.get(0).getValidatorId());
        assertEquals("Must contain JOIN", result.get(0).getDescription());
    }

    @Test
    void getValidatorsForItem_itemNotFound_throws404() {
        when(itemRepository.findById(NOT_FOUND_ID)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> service.getValidatorsForItem(NOT_FOUND_ID));
    }

    @Test
    void addValidatorToItem_addsValidator() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(validatorRepository.findById(VALIDATOR_ID)).thenReturn(Optional.of(validator));
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        mockConvertDeps();

        ItemResponseDto result = service.addValidatorToItem(ITEM_ID, VALIDATOR_ID);

        assertNotNull(result);
    }

    @Test
    void addValidatorToItem_validatorNotFound_throws404() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(validatorRepository.findById(VALIDATOR_ID)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> service.addValidatorToItem(ITEM_ID, VALIDATOR_ID));
    }

    @Test
    void addValidatorToCollection_throws400() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(collectionRepository.existsByParentItem_ItemId(ITEM_ID)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.addValidatorToItem(ITEM_ID, VALIDATOR_ID));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void removeValidatorFromItem_removesValidator() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        mockConvertDeps();

        ItemResponseDto result = service.removeValidatorFromItem(ITEM_ID, VALIDATOR_ID);

        assertNotNull(result);
    }

    @Test
    void removeValidatorFromItem_notLinked_throws404() {
        Item itemWithoutValidators = new Item();
        itemWithoutValidators.setItemId(ITEM_ID);
        itemWithoutValidators.setValidators(new HashSet<>());

        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(itemWithoutValidators));

        assertThrows(ResponseStatusException.class,
                () -> service.removeValidatorFromItem(ITEM_ID, VALIDATOR_ID));
    }

    // ================================================================
    // convertToCollection
    // ================================================================

    @Test
    void convertToCollection_createsCollection() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(collectionRepository.save(any(ItemCollection.class))).thenReturn(new ItemCollection());
        mockConvertDeps();

        ItemResponseDto result = service.convertToCollection(ITEM_ID);

        assertNotNull(result);
        verify(collectionRepository).save(any(ItemCollection.class));
    }

    @Test
    void convertToCollection_itemNotFound_throws404() {
        when(itemRepository.findById(NOT_FOUND_ID)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> service.convertToCollection(NOT_FOUND_ID));
    }
}
