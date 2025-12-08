package com.rgs.recipeapi;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthorCascadeDeleteTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @BeforeEach
    void setUp() {
        ingredientRepository.deleteAll();
        recipeRepository.deleteAll();
        authorRepository.deleteAll();
    }

    @Test
    void deletingAuthorShouldCascadeDeleteRecipes() throws Exception {
        // Setup: Create author with recipe
        Author author = new Author();
        author.setName("Test Author");
        author = authorRepository.save(author);

        Recipe recipe = new Recipe();
        recipe.setTitle("Test Recipe");
        recipe.setAuthor(author);
        recipe = recipeRepository.save(recipe);

        Long authorId = author.getId();

        // Expected behavior: Deleting author should cascade delete recipes
        mockMvc.perform(delete("/authors/" + authorId))
                .andExpect(status().isNoContent());

        // Author should be gone
        assertThat(authorRepository.findById(authorId)).isEmpty();

        // Recipe should also be gone (cascade delete)
        assertThat(recipeRepository.findAll()).isEmpty();
    }

    @Test
    void deletingAuthorShouldCascadeDeleteRecipesAndIngredients() throws Exception {
        // Setup: Create author with recipe that has ingredients
        Author author = new Author();
        author.setName("Chef Author");
        author = authorRepository.save(author);

        Recipe recipe = new Recipe();
        recipe.setTitle("Recipe with Ingredients");
        recipe.setAuthor(author);
        recipe = recipeRepository.save(recipe);

        Ingredient ingredient = new Ingredient();
        ingredient.setName("Salt");
        ingredient.setQuantity(1.0f);
        ingredient.setUnit("tsp");
        ingredient.setRecipe(recipe);
        ingredient = ingredientRepository.save(ingredient);

        Long authorId = author.getId();

        // Expected behavior: Deleting author should cascade delete recipes and their ingredients
        mockMvc.perform(delete("/authors/" + authorId))
                .andExpect(status().isNoContent());

        // Author should be gone
        assertThat(authorRepository.findById(authorId)).isEmpty();

        // Recipe should also be gone (cascade delete)
        assertThat(recipeRepository.findAll()).isEmpty();

        // Ingredient should also be gone (cascade delete through recipe)
        assertThat(ingredientRepository.findAll()).isEmpty();
    }
}
