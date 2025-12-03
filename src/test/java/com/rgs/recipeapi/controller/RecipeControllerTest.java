package com.rgs.recipeapi.controller;

import com.rgs.recipeapi.entity.Author;
import com.rgs.recipeapi.entity.Recipe;
import com.rgs.recipeapi.repository.AuthorRepository;
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
class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Author testAuthor;

    @BeforeEach
    void setUp() {
        recipeRepository.deleteAll();
        authorRepository.deleteAll();

        testAuthor = new Author();
        testAuthor.setName("Test Author");
        testAuthor = authorRepository.save(testAuthor);
    }

    @Test
    void shouldCreateRecipe() throws Exception {
        Recipe recipe = new Recipe();
        recipe.setTitle("Chocolate Cake");
        recipe.setDescription("Rich chocolate cake");
        recipe.setInstructions("Mix and bake.");
        recipe.setAuthor(testAuthor);

        mockMvc.perform(post("/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recipe)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Chocolate Cake"));
    }

    @Test
    void shouldGetAllRecipes() throws Exception {
        Recipe recipe = new Recipe();
        recipe.setTitle("Apple Pie");
        recipe.setAuthor(testAuthor);
        recipeRepository.save(recipe);

        mockMvc.perform(get("/recipes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Apple Pie"));
    }

    @Test
    void shouldGetRecipeById() throws Exception {
        Recipe recipe = new Recipe();
        recipe.setTitle("Pavlova");
        recipe.setAuthor(testAuthor);
        recipe = recipeRepository.save(recipe);

        mockMvc.perform(get("/recipes/" + recipe.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Pavlova"));
    }

    @Test
    void shouldUpdateRecipe() throws Exception {
        Recipe recipe = new Recipe();
        recipe.setTitle("Old Title");
        recipe.setAuthor(testAuthor);
        recipe = recipeRepository.save(recipe);

        recipe.setTitle("New Title");

        mockMvc.perform(put("/recipes/" + recipe.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recipe)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"));
    }

    @Test
    void shouldDeleteRecipe() throws Exception {
        Recipe recipe = new Recipe();
        recipe.setTitle("To Delete");
        recipe.setAuthor(testAuthor);
        recipe = recipeRepository.save(recipe);

        mockMvc.perform(delete("/recipes/" + recipe.getId()))
                .andExpect(status().isNoContent());
    }
}
