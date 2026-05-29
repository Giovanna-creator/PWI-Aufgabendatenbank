package com.datenbank.backend.service;

import com.datenbank.backend.dto.TagCreateDto;
import com.datenbank.backend.dto.TagResponseDto;
import com.datenbank.backend.model.Tag;
import com.datenbank.backend.repository.TagRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public List<TagResponseDto> getAllTags() {
        return tagRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public List<TagResponseDto> getRootTags() {
        return tagRepository.findByParentTagIsNull()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public List<TagResponseDto> getChildTags(Integer parentId) {
        return tagRepository.findByParentTag_TagId(parentId.longValue())
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public TagResponseDto getTagById(Integer id) {
        Tag tag = tagRepository.findById(id.longValue())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag nicht gefunden"));

        return toResponseDto(tag);
    }

    public TagResponseDto createTag(TagCreateDto dto) {

        Tag tag = new Tag();

        tag.setTag(dto.getTag());
        tag.setDescription(dto.getDescription());

        if (dto.getParentTagId() != null) {
            Tag parent = tagRepository.findById(dto.getParentTagId().longValue())
                    .orElseThrow(() ->
                            new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent-Tag nicht gefunden"));

            tag.setParentTag(parent);
        }

        return toResponseDto(tagRepository.save(tag));
    }

    public TagResponseDto updateTag(Integer id, TagCreateDto dto) {

        Tag tag = tagRepository.findById(id.longValue())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag nicht gefunden"));

        tag.setTag(dto.getTag());
        tag.setDescription(dto.getDescription());

        if (dto.getParentTagId() != null) {
            Tag parent = tagRepository.findById(dto.getParentTagId().longValue())
                    .orElseThrow(() ->
                            new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent-Tag nicht gefunden"));

            tag.setParentTag(parent);
        } else {
            tag.setParentTag(null);
        }

        return toResponseDto(tagRepository.save(tag));
    }

    public void deleteTag(Integer id) {

        if (!tagRepository.existsById(id.longValue())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag nicht gefunden");
        }

        tagRepository.deleteById(id.longValue());
    }

    private TagResponseDto toResponseDto(Tag tag) {

        TagResponseDto dto = new TagResponseDto();

        dto.setTagId(tag.getTag_id().intValue());
        dto.setTag(tag.getTag());
        dto.setDescription(tag.getDescription());

        if (tag.getParentTag() != null) {
            dto.setParentTagId(tag.getParentTag().getTag_id().intValue());
        }

        return dto;
    }
}