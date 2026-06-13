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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemCollectionServiceTest {

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
        author.setAuthorId(1);

        item1 = new Item();
        item1.setItemId(1);
        item1.setAuthor(author);

        item2 = new Item();
        item2.setItemId(2);
        item2.setAuthor(author);

        item3 = new Item();
        item3.setItemId(3);
        item3.setAuthor(author);

        collection = new ItemCollection();
        collection.setItemCollectionId(10);
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
        when(collectionRepository.findById(10)).thenReturn(Optional.of(collection));
        when(subItemRepository.findByCollection_ItemCollectionIdOrderByPositionAsc(10))
                .thenReturn(subItems);
        when(collectionRepository.save(collection)).thenReturn(collection);
        when(subItemRepository.saveAll(subItems)).thenReturn(subItems);

        ItemCollectionResponseDto result = service.toggleOrder(10, true);

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

        when(collectionRepository.findById(10)).thenReturn(Optional.of(collection));
        when(subItemRepository.findByCollection_ItemCollectionIdOrderByPositionAsc(10))
                .thenReturn(subItems);
        when(collectionRepository.save(collection)).thenReturn(collection);
        when(subItemRepository.saveAll(subItems)).thenReturn(subItems);

        ItemCollectionResponseDto result = service.toggleOrder(10, false);

        assertFalse(result.getOrder());
        assertNull(sub1.getPosition());
        assertNull(sub2.getPosition());
        assertNull(sub3.getPosition());
    }

    @Test
    void toggleOrder_collectionNotFound_throws404() {
        when(collectionRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> service.toggleOrder(999, true));
    }

    // ── updateSubItemPosition ──

    @Test
    void updateSubItemPosition_movesFirstToThird() {
        sub1.setPosition(1);
        sub2.setPosition(2);
        sub3.setPosition(3);

        when(subItemRepository.findByCollection_ItemCollectionIdOrderByPositionAsc(10))
                .thenReturn(new ArrayList<>(List.of(sub1, sub2, sub3)));

        service.updateSubItemPosition(10, 1, 3);

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

        when(subItemRepository.findByCollection_ItemCollectionIdOrderByPositionAsc(10))
                .thenReturn(new ArrayList<>(List.of(sub1, sub2, sub3)));

        service.updateSubItemPosition(10, 2, 5);

        assertEquals(1, sub1.getPosition());
        assertEquals(2, sub3.getPosition());
        assertEquals(3, sub2.getPosition());
    }

    @Test
    void updateSubItemPosition_itemNotFound_throws404() {
        when(subItemRepository.findByCollection_ItemCollectionIdOrderByPositionAsc(10))
                .thenReturn(new ArrayList<>(List.of(sub1, sub2)));

        assertThrows(ResponseStatusException.class,
                () -> service.updateSubItemPosition(10, 999, 1));
    }

    // ── removeItemFromCollection ──

    @Test
    void removeItemFromCollection_removesMiddleAndRecalculates() {
        sub1.setPosition(1);
        sub2.setPosition(2);
        sub3.setPosition(3);

        when(subItemRepository.findByCollection_ItemCollectionIdOrderByPositionAsc(10))
                .thenReturn(new ArrayList<>(List.of(sub1, sub2, sub3)));

        service.removeItemFromCollection(10, 2);

        verify(subItemRepository).delete(sub2);
        assertEquals(1, sub1.getPosition());
        assertEquals(2, sub3.getPosition());
    }

    @Test
    void removeItemFromCollection_removesLastAndRecalculates() {
        sub1.setPosition(1);
        sub2.setPosition(2);
        sub3.setPosition(3);

        when(subItemRepository.findByCollection_ItemCollectionIdOrderByPositionAsc(10))
                .thenReturn(new ArrayList<>(List.of(sub1, sub2, sub3)));

        service.removeItemFromCollection(10, 3);

        verify(subItemRepository).delete(sub3);
        assertEquals(1, sub1.getPosition());
        assertEquals(2, sub2.getPosition());
    }

    @Test
    void removeItemFromCollection_itemNotFound_throws404() {
        when(subItemRepository.findByCollection_ItemCollectionIdOrderByPositionAsc(10))
                .thenReturn(new ArrayList<>(List.of(sub1, sub2)));

        assertThrows(ResponseStatusException.class,
                () -> service.removeItemFromCollection(10, 999));
    }
}
