package com.rgs.recipeapi.repository;

import com.rgs.recipeapi.entity.Author;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AuthorRepositoryTest {

    @Autowired
    private AuthorRepository authorRepository;

    @Test
    void shouldSaveAndRetrieveAuthor() {
        Author author = new Author();
        author.setName("Eliza Acton");

        Author saved = authorRepository.save(author);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Eliza Acton");
    }
}
