package com.datenbank.backend.service;

import com.datenbank.backend.dto.ItemCollectionResponseDto;
import com.datenbank.backend.entity.*;
import com.datenbank.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemCollectionServiceTest {

    private static final UUID AUTHOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ITEM_1_ID = UUID.fromString("00000000-0000-0000-0000-000000000011");
    private static final UUID ITEM_2_ID = UUID.fromString("00000000-0000-0000-0000-000000000012");
    private static final UUID ITEM_3_ID = UUID.fromString("00000000-0000-0000-0000-000000000013");
    private static final UUID COLLECTION_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID NOT_FOUND_ID = UUID.fromString("00000000-0000-0000-0000-00000000FFFF");

    @Mock
    private ItemCollectionRepository collectionRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemCollectionSubItemRepository subItemRepository;

    @InjectMocks
    private ItemCollectionService service;

    private ItemCollection collection;
    private Item item1;
    private Item item2;
    private Item item3;
    private ItemCollectionSubItem sub1;
    private ItemCollectionSubItem sub2;
    private ItemCollectionSubItem sub3;
    private List<ItemCollectionSubItem> subItems;

    @BeforeEach
    void setUp() {
        Author author = new Author();
        author.setAuthorId(AUTHOR_ID);

        item1 = new Item();
        item1.setItemId(ITEM_1_ID);
        item1.setAuthor(author);

        item2 = new Item();
        item2.setItemId(ITEM_2_ID);
        item2.setAuthor(author);

        item3 = new Item();
        item3.setItemId(ITEM_3_ID);
        item3.setAuthor(author);

        collection = new ItemCollection();
        collection.setItemCollectionId(COLLECTION_ID);
        collection.setCollectionOrder(false);

        sub1 = new ItemCollectionSubItem(collection, item1, null);
        sub2 = new ItemCollectionSubItem(collection, item2, null);
        sub3 = new ItemCollectionSubItem(collection, item3, null);
        subItems = new ArrayList<>(List.of(sub1, sub2, sub3));
        collection.setSubItems(subItems);
    }

    // ── toggleOrder ──

    @Test
    void toggleOrder_true_assignsSequentialPositions() {
        when(collectionRepository.findById(COLLECTION_ID)).thenReturn(Optional.of(collection));
        when(subItemRepository.findByCollection_ItemCollectionIdOrderByPositionAsc(COLLECTION_ID))
                .thenReturn(subItems);
        when(collectionRepository.save(collection)).thenReturn(collection);
        when(subItemRepository.saveAll(subItems)).thenReturn(subItems);

        ItemCollectionResponseDto result = service.toggleOrder(COLLECTION_ID, true);

        assertTrue(result.getOrder());
        assertEquals(1, sub1.getPosition());
        assertEquals(2, sub2.getPosition());
        assertEquals(3, sub3.getPosition());
    }

    @Test
    void toggleOrder_false_clearsAllPositions() {
        sub1.setPosition(1);
        sub2.setPosition(2);
        sub3.setPosition(3);

        when(collectionRepository.findById(COLLECTION_ID)).thenReturn(Optional.of(collection));
        when(subItemRepository.findByCollection_ItemCollectionIdOrderByPositionAsc(COLLECTION_ID))
                .thenReturn(subItems);
        when(collectionRepository.save(collection)).thenReturn(collection);
        when(subItemRepository.saveAll(subItems)).thenReturn(subItems);

        ItemCollectionResponseDto result = service.toggleOrder(COLLECTION_ID, false);

        assertFalse(result.getOrder());
        assertNull(sub1.getPosition());
        assertNull(sub2.getPosition());
        assertNull(sub3.getPosition());
    }

    @Test
    void toggleOrder_collectionNotFound_throws404() {
        when(collectionRepository.findById(NOT_FOUND_ID)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> service.toggleOrder(NOT_FOUND_ID, true));
    }

    // ── updateSubItemPosition ──

    @Test
    void updateSubItemPosition_movesFirstToThird() {
        sub1.setPosition(1);
        sub2.setPosition(2);
        sub3.setPosition(3);

        when(subItemRepository.findByCollection_ItemCollectionIdOrderByPositionAsc(COLLECTION_ID))
                .thenReturn(new ArrayList<>(List.of(sub1, sub2, sub3)));

        service.updateSubItemPosition(COLLECTION_ID, ITEM_1_ID, 3);

        assertEquals(1, sub2.getPosition());
        assertEquals(2, sub3.getPosition());
        assertEquals(3, sub1.getPosition());
        verify(subItemRepository).saveAll(anyList());
    }

    @Test
    void updateSubItemPosition_moveToEnd_assignsLastPosition() {
        sub1.setPosition(1);
        sub2.setPosition(2);
        sub3.setPosition(3);

        when(subItemRepository.findByCollection_ItemCollectionIdOrderByPositionAsc(COLLECTION_ID))
                .thenReturn(new ArrayList<>(List.of(sub1, sub2, sub3)));

        service.updateSubItemPosition(COLLECTION_ID, ITEM_2_ID, 5);

        assertEquals(1, sub1.getPosition());
        assertEquals(2, sub3.getPosition());
        assertEquals(3, sub2.getPosition());
    }

    @Test
    void updateSubItemPosition_itemNotFound_throws404() {
        when(subItemRepository.findByCollection_ItemCollectionIdOrderByPositionAsc(COLLECTION_ID))
                .thenReturn(new ArrayList<>(List.of(sub1, sub2)));

        assertThrows(ResponseStatusException.class,
                () -> service.updateSubItemPosition(COLLECTION_ID, NOT_FOUND_ID, 1));
    }

    // ── removeItemFromCollection ──

    @Test
    void removeItemFromCollection_removesMiddleAndRecalculates() {
        sub1.setPosition(1);
        sub2.setPosition(2);
        sub3.setPosition(3);

        when(subItemRepository.findByCollection_ItemCollectionIdOrderByPositionAsc(COLLECTION_ID))
                .thenReturn(new ArrayList<>(List.of(sub1, sub2, sub3)));

        service.removeItemFromCollection(COLLECTION_ID, ITEM_2_ID);

        verify(subItemRepository).delete(sub2);
        assertEquals(1, sub1.getPosition());
        assertEquals(2, sub3.getPosition());
    }

    @Test
    void removeItemFromCollection_removesLastAndRecalculates() {
        sub1.setPosition(1);
        sub2.setPosition(2);
        sub3.setPosition(3);

        when(subItemRepository.findByCollection_ItemCollectionIdOrderByPositionAsc(COLLECTION_ID))
                .thenReturn(new ArrayList<>(List.of(sub1, sub2, sub3)));

        service.removeItemFromCollection(COLLECTION_ID, ITEM_3_ID);

        verify(subItemRepository).delete(sub3);
        assertEquals(1, sub1.getPosition());
        assertEquals(2, sub2.getPosition());
    }

    @Test
    void removeItemFromCollection_itemNotFound_throws404() {
        when(subItemRepository.findByCollection_ItemCollectionIdOrderByPositionAsc(COLLECTION_ID))
                .thenReturn(new ArrayList<>(List.of(sub1, sub2)));

        assertThrows(ResponseStatusException.class,
                () -> service.removeItemFromCollection(COLLECTION_ID, NOT_FOUND_ID));
    }
}
