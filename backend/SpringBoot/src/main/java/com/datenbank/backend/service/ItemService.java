package com.datenbank.backend.service;

import com.datenbank.backend.dto.ItemDTO;
import com.datenbank.backend.model.*;
import com.datenbank.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class ItemService {

    @Autowired private ItemRepository itemRepository;
    @Autowired private AuthorRepository authorRepository;
    @Autowired private LicenseRepository licenseRepository;
    @Autowired private ItemTypeRepository itemTypeRepository;
    @Autowired private ItemRepresentationTemplateRepository templateRepository;

    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    public Optional<Item> getItemById(Long id) {
        return itemRepository.findById(id);
    }

    public Item createItem(ItemDTO dto) {
        Item item = new Item();
        return mapDtoToItem(dto, item);
    }

    public Item updateItem(Long id, ItemDTO dto) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item nicht gefunden"));
        return mapDtoToItem(dto, item);
    }

    public void deleteItem(Long id) {
        if (!itemRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item nicht gefunden");
        }
        itemRepository.deleteById(id);
    }

    // Hilfsmethode: DTO → Entity
    private Item mapDtoToItem(ItemDTO dto, Item item) {
        item.setAuthor(authorRepository.findById(dto.getAuthor_id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Author nicht gefunden")));

        item.setLicense(licenseRepository.findById(dto.getLicense_id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "License nicht gefunden")));

        item.setItemType(itemTypeRepository.findById(dto.getItem_type_id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ItemType nicht gefunden")));

        if (dto.getItem_template_id() != null) {
            item.setItemTemplate(templateRepository.findById(dto.getItem_template_id())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Template nicht gefunden")));
        }

        if (dto.getRoot_item_id() != null) {
            item.setRootItem(itemRepository.findById(dto.getRoot_item_id())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Root Item nicht gefunden")));
        }

        return itemRepository.save(item);
    }
}