package com.datenbank.backend.service;

import com.datenbank.backend.dto.AuthorCreateDto;
import com.datenbank.backend.dto.AuthorResponseDto;
import com.datenbank.backend.entity.Author;
import com.datenbank.backend.repository.AuthorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AuthorService {

    private final AuthorRepository authorRepository;

    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    public List<AuthorResponseDto> getAllAuthors() {
        return authorRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public AuthorResponseDto getAuthorById(Integer id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Author nicht gefunden"));

        return toDto(author);
    }

    public AuthorResponseDto createAuthor(AuthorCreateDto dto) {
        Author author = new Author();
        author.setDescriptor(dto.getDescriptor());
        author.setMail(dto.getMail());

        return toDto(authorRepository.save(author));
    }

    public AuthorResponseDto updateAuthor(Integer id, AuthorCreateDto dto) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Author nicht gefunden"));

        author.setDescriptor(dto.getDescriptor());
        author.setMail(dto.getMail());

        return toDto(authorRepository.save(author));
    }

    public void deleteAuthor(Integer id) {
        if (!authorRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Author nicht gefunden");
        }
        authorRepository.deleteById(id);
    }

    private AuthorResponseDto toDto(Author author) {
        AuthorResponseDto dto = new AuthorResponseDto();
        dto.setAuthorId(author.getAuthor_id());
        dto.setDescriptor(author.getDescriptor());
        dto.setMail(author.getMail());
        return dto;
    }
}