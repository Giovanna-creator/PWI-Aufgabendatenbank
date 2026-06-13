package com.datenbank.backend.dto;

import com.datenbank.backend.entity.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class FrontendDtoMapper {

    private FrontendDtoMapper() {}

    public static FrontendItemDto toItemDto(Item item, boolean isCollection, Boolean order, List<ItemCollectionSubItem> subItems) {
        FrontendItemDto dto = new FrontendItemDto();
        dto.setId(String.valueOf(item.getItemId()));
        dto.setItemType(item.getItemType() != null ? item.getItemType().getItemTypeName() : "exercise");
        dto.setAuthor(item.getAuthor() != null ? item.getAuthor().getDescriptor() : null);
        dto.setRepresentationTemplate(item.getItemTemplate() != null ? item.getItemTemplate().getTemplate() : null);
        dto.setLicense(item.getLicense() != null ? item.getLicense().getLicense() : null);
        dto.setRootItemId(item.getRootItem() != null ? String.valueOf(item.getRootItem().getItemId()) : null);

        List<FrontendContentDto> contentDtos = new ArrayList<>();
        if (item.getItemContents() != null) {
            contentDtos = item.getItemContents().stream()
                    .map(ic -> toContentDto(ic.getItemContent(), ic.getPurpose()))
                    .collect(Collectors.toList());
        }
        dto.setContents(contentDtos);

        if (isCollection) {
            dto.setOrder(order);
            dto.setItems(new ArrayList<>());
            if (subItems != null) {
                dto.setItems(subItems.stream()
                        .map(FrontendDtoMapper::toCollectionItemDto)
                        .collect(Collectors.toList()));
            }
        }

        return dto;
    }

    public static FrontendContentDto toContentDto(ItemContent content, String purpose) {
        FrontendContentDto dto = new FrontendContentDto();
        dto.setId(String.valueOf(content.getItemContentId()));
        dto.setPurpose(purpose);
        dto.setJsonContent(content.getJsonSerializedContent());
        dto.setBlobContent(content.getBlobSerializedContent() != null ? "present" : null);
        dto.setContentType(content.getItemContentType() != null ? content.getItemContentType().getItemContentTypeName() : null);
        return dto;
    }

    public static FrontendCollectionItemDto toCollectionItemDto(ItemCollectionSubItem subItem) {
        FrontendCollectionItemDto dto = new FrontendCollectionItemDto();
        dto.setId(String.valueOf(subItem.getSubItem().getItemId()));
        dto.setChildItemId(String.valueOf(subItem.getSubItem().getItemId()));
        dto.setPosition(subItem.getPosition());
        if (subItem.getItemCollection() != null) {
            dto.setOrder(subItem.getItemCollection().getCollectionOrder());
        }
        return dto;
    }
}
