package com.rgs.recipeapi.controller;

import com.rgs.recipeapi.entity.Author;
import com.rgs.recipeapi.entity.Ingredient;
import com.rgs.recipeapi.entity.Recipe;
import com.rgs.recipeapi.repository.AuthorRepository;
import com.rgs.recipeapi.repository.IngredientRepository;
import com.rgs.recipeapi.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RecipeSearchTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    private Author testAuthor;
    private Recipe testRecipe;

    @BeforeEach
    void setUp() {
        ingredientRepository.deleteAll();
        recipeRepository.deleteAll();
        authorRepository.deleteAll();

        testAuthor = new Author();
        testAuthor.setName("Gordon Ramsay");
        testAuthor = authorRepository.save(testAuthor);

        testRecipe = new Recipe();
        testRecipe.setTitle("Beef Wellington");
        testRecipe.setDescription("A classic British dish with tender beef");
        testRecipe.setInstructions("Wrap beef in puff pastry and bake");
        testRecipe.setAuthor(testAuthor);
        testRecipe = recipeRepository.save(testRecipe);

        Ingredient ingredient = new Ingredient();
        ingredient.setName("Mushroom Duxelles");
        ingredient.setQuantity(200f);
        ingredient.setUnit("grams");
        ingredient.setRecipe(testRecipe);
        ingredientRepository.save(ingredient);
    }

    @Test
    void shouldReturnEmptyListWhenNoKeywordsProvided() throws Exception {
        mockMvc.perform(get("/recipes/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenKeywordsArrayIsEmpty() throws Exception {
        mockMvc.perform(get("/recipes/search").param("keywords", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldFindRecipeByTitleKeyword() throws Exception {
        mockMvc.perform(get("/recipes/search").param("keywords", "Wellington"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Beef Wellington"));
    }

    @Test
    void shouldFindRecipeByDescriptionKeyword() throws Exception {
        mockMvc.perform(get("/recipes/search").param("keywords", "British"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Beef Wellington"));
    }

    @Test
    void shouldFindRecipeByInstructionsKeyword() throws Exception {
        mockMvc.perform(get("/recipes/search").param("keywords", "pastry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Beef Wellington"));
    }

    @Test
    void shouldFindRecipeByIngredientNameKeyword() throws Exception {
        mockMvc.perform(get("/recipes/search").param("keywords", "Mushroom"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Beef Wellington"));
    }

    @Test
    void shouldFindRecipeByAuthorNameKeyword() throws Exception {
        mockMvc.perform(get("/recipes/search").param("keywords", "Ramsay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Beef Wellington"));
    }

    @Test
    void shouldFindRecipeWithMultipleKeywords() throws Exception {
        mockMvc.perform(get("/recipes/search")
                        .param("keywords", "Beef")
                        .param("keywords", "Gordon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Beef Wellington"));
    }

    @Test
    void shouldReturnEmptyListWhenNoRecipesMatchKeyword() throws Exception {
        mockMvc.perform(get("/recipes/search").param("keywords", "Pizza"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldBeCaseInsensitive() throws Exception {
        mockMvc.perform(get("/recipes/search").param("keywords", "beef"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Beef Wellington"));
    }

    @Test
    void shouldNotReturnDuplicatesWhenMultipleFieldsMatch() throws Exception {
        mockMvc.perform(get("/recipes/search").param("keywords", "Beef"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
