package com.datenbank.backend.service;

import com.datenbank.backend.dto.ItemContentResponseDto;
import com.datenbank.backend.entity.*;
import com.datenbank.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
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

    @InjectMocks
    private ItemContentService service;

    private ItemContent content1;
    private ItemContent content2;
    private Item item;
    private ItemContents link1;
    private ItemContents link2;

    @BeforeEach
    void setUp() {
        Author author = new Author();
        author.setAuthorId(AUTHOR_ID);
        author.setDescriptor("Test Author");

        License license = new License();
        license.setLicenseId(LICENSE_ID);
        license.setLicense("CC-BY-4.0");

        ItemContentType contentType = new ItemContentType();
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
}
