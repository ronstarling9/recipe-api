package com.rgs.recipeapi.controller;

import com.rgs.recipeapi.entity.Author;
import com.rgs.recipeapi.entity.Ingredient;
import com.rgs.recipeapi.entity.Recipe;
import com.rgs.recipeapi.repository.AuthorRepository;
import com.rgs.recipeapi.repository.IngredientRepository;
import com.rgs.recipeapi.repository.RecipeRepository;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class IngredientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Recipe testRecipe;

    @BeforeEach
    void setUp() {
        ingredientRepository.deleteAll();
        recipeRepository.deleteAll();
        authorRepository.deleteAll();

        Author author = new Author();
        author.setName("Test Author");
        author = authorRepository.save(author);

        testRecipe = new Recipe();
        testRecipe.setTitle("Test Recipe");
        testRecipe.setAuthor(author);
        testRecipe = recipeRepository.save(testRecipe);
    }

    @Test
    void shouldCreateIngredient() throws Exception {
        Ingredient ingredient = new Ingredient();
        ingredient.setName("Flour");
        ingredient.setQuantity(2.0f);
        ingredient.setUnit("cups");

        mockMvc.perform(post("/recipes/" + testRecipe.getId() + "/ingredients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ingredient)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Flour"));
    }

    @Test
    void shouldGetIngredientsForRecipe() throws Exception {
        Ingredient ingredient = new Ingredient();
        ingredient.setName("Sugar");
        ingredient.setQuantity(1.0f);
        ingredient.setUnit("cup");
        ingredient.setRecipe(testRecipe);
        ingredientRepository.save(ingredient);

        mockMvc.perform(get("/recipes/" + testRecipe.getId() + "/ingredients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Sugar"));
    }

    @Test
    void shouldUpdateIngredient() throws Exception {
        Ingredient ingredient = new Ingredient();
        ingredient.setName("Butter");
        ingredient.setQuantity(1.0f);
        ingredient.setUnit("stick");
        ingredient.setRecipe(testRecipe);
        ingredient = ingredientRepository.save(ingredient);

        ingredient.setQuantity(2.0f);

        mockMvc.perform(put("/recipes/" + testRecipe.getId() + "/ingredients/" + ingredient.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ingredient)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(2.0));
    }

    @Test
    void shouldDeleteIngredient() throws Exception {
        Ingredient ingredient = new Ingredient();
        ingredient.setName("Salt");
        ingredient.setQuantity(1.0f);
        ingredient.setUnit("tsp");
        ingredient.setRecipe(testRecipe);
        ingredient = ingredientRepository.save(ingredient);

        mockMvc.perform(delete("/recipes/" + testRecipe.getId() + "/ingredients/" + ingredient.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldAcceptPositiveQuantityOnCreate() throws Exception {
        Ingredient ingredient = new Ingredient();
        ingredient.setName("Sugar");
        ingredient.setQuantity(5.0f);
        ingredient.setUnit("cups");

        mockMvc.perform(post("/recipes/" + testRecipe.getId() + "/ingredients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ingredient)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantity").value(5.0));
    }

    @Test
    void shouldAcceptZeroQuantityOnCreate() throws Exception {
        Ingredient ingredient = new Ingredient();
        ingredient.setName("Optional garnish");
        ingredient.setQuantity(0.0f);
        ingredient.setUnit("cups");

        mockMvc.perform(post("/recipes/" + testRecipe.getId() + "/ingredients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ingredient)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantity").value(0.0));
    }

    @Test
    void shouldRejectNegativeQuantityOnCreate() throws Exception {
        Ingredient ingredient = new Ingredient();
        ingredient.setName("Sugar");
        ingredient.setQuantity(-5.0f);
        ingredient.setUnit("cups");

        mockMvc.perform(post("/recipes/" + testRecipe.getId() + "/ingredients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ingredient)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldAcceptPositiveQuantityOnUpdate() throws Exception {
        Ingredient ingredient = new Ingredient();
        ingredient.setName("Butter");
        ingredient.setQuantity(1.0f);
        ingredient.setUnit("stick");
        ingredient.setRecipe(testRecipe);
        ingredient = ingredientRepository.save(ingredient);

        ingredient.setQuantity(3.0f);

        mockMvc.perform(put("/recipes/" + testRecipe.getId() + "/ingredients/" + ingredient.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ingredient)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(3.0));
    }

    @Test
    void shouldAcceptZeroQuantityOnUpdate() throws Exception {
        Ingredient ingredient = new Ingredient();
        ingredient.setName("Butter");
        ingredient.setQuantity(1.0f);
        ingredient.setUnit("stick");
        ingredient.setRecipe(testRecipe);
        ingredient = ingredientRepository.save(ingredient);

        ingredient.setQuantity(0.0f);

        mockMvc.perform(put("/recipes/" + testRecipe.getId() + "/ingredients/" + ingredient.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ingredient)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(0.0));
    }

    @Test
    void shouldRejectNegativeQuantityOnUpdate() throws Exception {
        Ingredient ingredient = new Ingredient();
        ingredient.setName("Butter");
        ingredient.setQuantity(1.0f);
        ingredient.setUnit("stick");
        ingredient.setRecipe(testRecipe);
        ingredient = ingredientRepository.save(ingredient);

        ingredient.setQuantity(-2.0f);

        mockMvc.perform(put("/recipes/" + testRecipe.getId() + "/ingredients/" + ingredient.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ingredient)))
                .andExpect(status().isBadRequest());
    }
}
