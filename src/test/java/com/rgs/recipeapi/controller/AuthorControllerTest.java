package com.rgs.recipeapi.controller;

import com.rgs.recipeapi.entity.Author;
import com.rgs.recipeapi.repository.AuthorRepository;
import com.rgs.recipeapi.repository.IngredientRepository;
import com.rgs.recipeapi.repository.RecipeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ingredientRepository.deleteAll();
        recipeRepository.deleteAll();
        authorRepository.deleteAll();
    }

    @Test
    void shouldCreateAuthor() throws Exception {
        Author author = new Author();
        author.setName("Eliza Acton");

        mockMvc.perform(post("/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(author)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Eliza Acton"));
    }

    @Test
    void shouldGetAllAuthors() throws Exception {
        Author author = new Author();
        author.setName("Escoffier");
        authorRepository.save(author);

        mockMvc.perform(get("/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Escoffier"));
    }

    @Test
    void shouldGetAuthorById() throws Exception {
        Author author = new Author();
        author.setName("Escoffier");
        author = authorRepository.save(author);

        mockMvc.perform(get("/authors/" + author.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Escoffier"));
    }

    @Test
    void shouldUpdateAuthor() throws Exception {
        Author author = new Author();
        author.setName("Old Name");
        author = authorRepository.save(author);

        author.setName("New Name");

        mockMvc.perform(put("/authors/" + author.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(author)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"));
    }

    @Test
    void shouldDeleteAuthor() throws Exception {
        Author author = new Author();
        author.setName("To Delete");
        author = authorRepository.save(author);

        mockMvc.perform(delete("/authors/" + author.getId()))
                .andExpect(status().isNoContent());
    }
}
