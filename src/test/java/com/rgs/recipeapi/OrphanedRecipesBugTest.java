package com.rgs.recipeapi;

import com.rgs.recipeapi.entity.Author;
import com.rgs.recipeapi.entity.Recipe;
import com.rgs.recipeapi.repository.AuthorRepository;
import com.rgs.recipeapi.repository.IngredientRepository;
import com.rgs.recipeapi.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrphanedRecipesBugTest {

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
    void deletingAuthorWithRecipesShouldFailGracefully() throws Exception {
        // Setup: Create author with recipe
        Author author = new Author();
        author.setName("Test Author");
        author = authorRepository.save(author);

        Recipe recipe = new Recipe();
        recipe.setTitle("Test Recipe");
        recipe.setAuthor(author);
        recipe = recipeRepository.save(recipe);

        Long authorId = author.getId();

        // BUG DEMONSTRATION: The AuthorController.deleteAuthor() does NOT check
        // for associated recipes before attempting deletion. It simply calls
        // authorRepository.deleteById() which will fail if recipes exist.
        //
        // In a properly designed system, the controller should either:
        // 1. Return a 409 Conflict indicating recipes must be deleted first
        // 2. Cascade delete the associated recipes
        // 3. Reassign recipes to a default author
        //
        // Instead, the raw database constraint violation causes an unhandled exception.
        // This test verifies the bug exists by catching the expected exception.
        try {
            mockMvc.perform(delete("/authors/" + authorId));
            // If we get here without exception, the bug might have been fixed
            assertThat(false).as("Expected exception was not thrown - controller should validate").isTrue();
        } catch (Exception e) {
            // Bug confirmed: raw exception instead of graceful error handling
            assertThat(e.getCause().getMessage()).containsIgnoringCase("constraint violation");
        }

        // The author and recipe both still exist
        assertThat(authorRepository.findById(authorId)).isPresent();
        assertThat(recipeRepository.findAll()).hasSize(1);
    }

    @Test
    void deletingAuthorWithoutRecipesSucceeds() throws Exception {
        // Setup: Create author WITHOUT recipes
        Author author = new Author();
        author.setName("Lonely Author");
        author = authorRepository.save(author);

        Long authorId = author.getId();

        // This should succeed
        mockMvc.perform(delete("/authors/" + authorId))
                .andExpect(status().isNoContent());

        // Author is gone
        assertThat(authorRepository.findById(authorId)).isEmpty();
    }
}
