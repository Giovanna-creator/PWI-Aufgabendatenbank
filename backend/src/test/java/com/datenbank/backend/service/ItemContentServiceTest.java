package com.datenbank.backend.service;

import com.datenbank.backend.dto.ItemContentCreateDto;
import com.datenbank.backend.dto.ItemContentResponseDto;
import com.datenbank.backend.entity.*;
import com.datenbank.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemContentServiceTest {

    private static final UUID AUTHOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID LICENSE_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID CONTENT_TYPE_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID ITEM_ID = UUID.fromString("00000000-0000-0000-0000-000000000100");
    private static final UUID CONTENT_1_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID CONTENT_2_ID = UUID.fromString("00000000-0000-0000-0000-000000000102");

    @Mock
    private ItemContentRepository contentRepository;

    @Mock
    private ItemContentsRepository itemContentsRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private LicenseRepository licenseRepository;

    @Mock
    private ItemContentTypeRepository contentTypeRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemContentService service;

    private Author author;
    private License license;
    private ItemContentType contentType;
    private ItemContent content1;
    private ItemContent content2;
    private Item item;
    private ItemContents link1;
    private ItemContents link2;
    private ItemContentCreateDto createDto;

    @BeforeEach
    void setUp() {
        author = new Author();
        author.setAuthorId(AUTHOR_ID);
        author.setDescriptor("Test Author");

        license = new License();
        license.setLicenseId(LICENSE_ID);
        license.setLicense("CC-BY-4.0");

        contentType = new ItemContentType();
        contentType.setItemContentTypeId(CONTENT_TYPE_ID);
        contentType.setItemContentTypeName("text/markdown");

        item = new Item();
        item.setItemId(ITEM_ID);

        content1 = new ItemContent();
        content1.setItemContentId(CONTENT_1_ID);
        content1.setAuthor(author);
        content1.setLicense(license);
        content1.setItemContentType(contentType);
        content1.setJsonSerializedContent("{\"text\": \"Content 1\"}");

        content2 = new ItemContent();
        content2.setItemContentId(CONTENT_2_ID);
        content2.setAuthor(author);
        content2.setLicense(license);
        content2.setItemContentType(contentType);
        content2.setJsonSerializedContent("{\"text\": \"Content 2\"}");

        link1 = new ItemContents(item, content1, "Aufgabenstellung");
        link2 = new ItemContents(item, content2, "Hinweis");

        createDto = new ItemContentCreateDto();
        createDto.setAuthorId(AUTHOR_ID);
        createDto.setLicenseId(LICENSE_ID);
        createDto.setItemContentTypeId(CONTENT_TYPE_ID);
        createDto.setJsonSerializedContent("{\"text\": \"New Content\"}");
    }

    @Test
    void getContentsByItemId_returnsContents() {
        when(itemContentsRepository.findByItem_ItemId(ITEM_ID))
                .thenReturn(List.of(link1, link2));

        List<ItemContentResponseDto> result = service.getContentsByItemId(ITEM_ID);

        assertEquals(2, result.size());
        assertEquals("{\"text\": \"Content 1\"}", result.get(0).getJsonSerializedContent());
        assertEquals("{\"text\": \"Content 2\"}", result.get(1).getJsonSerializedContent());
    }

    @Test
    void getContentsByItemId_noContents_returnsEmptyList() {
        when(itemContentsRepository.findByItem_ItemId(ITEM_ID))
                .thenReturn(List.of());

        List<ItemContentResponseDto> result = service.getContentsByItemId(ITEM_ID);

        assertTrue(result.isEmpty());
    }

    // ================================================================
    // getAll
    // ================================================================

    @Test
    void getAll_returnsList() {
        when(contentRepository.findAll()).thenReturn(List.of(content1, content2));

        List<ItemContentResponseDto> result = service.getAll();

        assertEquals(2, result.size());
    }

    @Test
    void getAll_emptyList() {
        when(contentRepository.findAll()).thenReturn(List.of());

        List<ItemContentResponseDto> result = service.getAll();

        assertTrue(result.isEmpty());
    }

    // ================================================================
    // getById
    // ================================================================

    @Test
    void getById_returnsContent() {
        when(contentRepository.findById(CONTENT_1_ID)).thenReturn(Optional.of(content1));

        ItemContentResponseDto result = service.getById(CONTENT_1_ID);

        assertEquals(CONTENT_1_ID, result.getItemContentId());
        assertEquals("{\"text\": \"Content 1\"}", result.getJsonSerializedContent());
    }

    @Test
    void getById_notFound_throws404() {
        when(contentRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> service.getById(CONTENT_1_ID));
    }

    // ================================================================
    // create
    // ================================================================

    @Test
    void create_createsContent() {
        when(authorRepository.findById(AUTHOR_ID)).thenReturn(Optional.of(author));
        when(licenseRepository.findById(LICENSE_ID)).thenReturn(Optional.of(license));
        when(contentTypeRepository.findById(CONTENT_TYPE_ID)).thenReturn(Optional.of(contentType));
        when(contentRepository.save(any(ItemContent.class))).thenReturn(content1);

        ItemContentResponseDto result = service.create(createDto);

        assertNotNull(result);
        assertEquals(CONTENT_1_ID, result.getItemContentId());
    }

    @Test
    void create_authorNotFound_throws404() {
        when(authorRepository.findById(AUTHOR_ID)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> service.create(createDto));
    }

    // ================================================================
    // createForItem
    // ================================================================

    @Test
    void createForItem_createsAndLinks() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(authorRepository.findById(AUTHOR_ID)).thenReturn(Optional.of(author));
        when(licenseRepository.findById(LICENSE_ID)).thenReturn(Optional.of(license));
        when(contentTypeRepository.findById(CONTENT_TYPE_ID)).thenReturn(Optional.of(contentType));
        when(contentRepository.save(any(ItemContent.class))).thenReturn(content1);
        when(itemContentsRepository.save(any(ItemContents.class))).thenReturn(link1);

        createDto.setPurpose("Aufgabenstellung");
        ItemContentResponseDto result = service.createForItem(ITEM_ID, createDto);

        assertNotNull(result);
        verify(itemContentsRepository).save(any(ItemContents.class));
    }

    @Test
    void createForItem_itemNotFound_throws404() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> service.createForItem(ITEM_ID, createDto));
    }

    // ================================================================
    // update
    // ================================================================

    @Test
    void update_updatesContent() {
        when(contentRepository.findById(CONTENT_1_ID)).thenReturn(Optional.of(content1));
        when(authorRepository.findById(AUTHOR_ID)).thenReturn(Optional.of(author));
        when(licenseRepository.findById(LICENSE_ID)).thenReturn(Optional.of(license));
        when(contentTypeRepository.findById(CONTENT_TYPE_ID)).thenReturn(Optional.of(contentType));
        when(contentRepository.save(any(ItemContent.class))).thenReturn(content1);

        ItemContentResponseDto result = service.update(CONTENT_1_ID, createDto);

        assertNotNull(result);
    }

    @Test
    void update_withPurpose_updatesLinks() {
        when(contentRepository.findById(CONTENT_1_ID)).thenReturn(Optional.of(content1));
        when(authorRepository.findById(AUTHOR_ID)).thenReturn(Optional.of(author));
        when(licenseRepository.findById(LICENSE_ID)).thenReturn(Optional.of(license));
        when(contentTypeRepository.findById(CONTENT_TYPE_ID)).thenReturn(Optional.of(contentType));
        when(contentRepository.save(any(ItemContent.class))).thenReturn(content1);
        when(itemContentsRepository.findByItemContent_ItemContentId(CONTENT_1_ID))
                .thenReturn(List.of(link1));
        when(itemContentsRepository.saveAll(anyList())).thenReturn(List.of(link1));

        createDto.setPurpose("Neuer Zweck");
        ItemContentResponseDto result = service.update(CONTENT_1_ID, createDto);

        assertNotNull(result);
        verify(itemContentsRepository).saveAll(anyList());
    }

    @Test
    void update_notFound_throws404() {
        when(contentRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> service.update(CONTENT_1_ID, createDto));
    }

    // ================================================================
    // uploadBlob
    // ================================================================

    @Test
    void uploadBlob_storesBlob() {
        byte[] blob = "binary data".getBytes();
        when(contentRepository.findById(CONTENT_1_ID)).thenReturn(Optional.of(content1));
        when(contentRepository.save(any(ItemContent.class))).thenReturn(content1);

        ItemContentResponseDto result = service.uploadBlob(CONTENT_1_ID, blob);

        assertNotNull(result);
    }

    @Test
    void uploadBlob_contentNotFound_throws404() {
        when(contentRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> service.uploadBlob(CONTENT_1_ID, new byte[0]));
    }

    // ================================================================
    // getBlobById
    // ================================================================

    @Test
    void getBlobById_returnsBlob() {
        byte[] blob = "binary data".getBytes();
        content1.setBlobSerializedContent(blob);
        when(contentRepository.findById(CONTENT_1_ID)).thenReturn(Optional.of(content1));

        byte[] result = service.getBlobById(CONTENT_1_ID);

        assertArrayEquals(blob, result);
    }

    @Test
    void getBlobById_noBlob_throws404() {
        content1.setBlobSerializedContent(null);
        when(contentRepository.findById(CONTENT_1_ID)).thenReturn(Optional.of(content1));

        assertThrows(ResponseStatusException.class,
                () -> service.getBlobById(CONTENT_1_ID));
    }

    @Test
    void getBlobById_contentNotFound_throws404() {
        when(contentRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> service.getBlobById(CONTENT_1_ID));
    }

    // ================================================================
    // delete
    // ================================================================

    @Test
    void delete_deletes() {
        when(contentRepository.existsById(CONTENT_1_ID)).thenReturn(true);

        service.delete(CONTENT_1_ID);

        verify(contentRepository).deleteById(CONTENT_1_ID);
    }

    @Test
    void delete_notFound_throws404() {
        when(contentRepository.existsById(any(UUID.class))).thenReturn(false);

        assertThrows(ResponseStatusException.class,
                () -> service.delete(CONTENT_1_ID));
    }
}
