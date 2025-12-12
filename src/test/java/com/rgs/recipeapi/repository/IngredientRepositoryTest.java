package com.rgs.recipeapi.repository;

import com.rgs.recipeapi.entity.Author;
import com.rgs.recipeapi.entity.Ingredient;
import com.rgs.recipeapi.entity.Recipe;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class IngredientRepositoryTest {

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Test
    void shouldSaveIngredientWithRecipe() {
        Author author = new Author();
        author.setName("Test Author");
        author = authorRepository.save(author);

        Recipe recipe = new Recipe();
        recipe.setTitle("Test Recipe");
        recipe.setAuthor(author);
        recipe = recipeRepository.save(recipe);

        Ingredient ingredient = new Ingredient();
        ingredient.setName("Sugar");
        ingredient.setQuantity(2.0f);
        ingredient.setUnit("cups");
        ingredient.setRecipe(recipe);

        Ingredient saved = ingredientRepository.save(ingredient);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Sugar");
        assertThat(saved.getQuantity()).isEqualTo(2.0f);
    }

    @Test
    void shouldRejectNegativeQuantity() {
        Author author = new Author();
        author.setName("Test Author");
        author = authorRepository.save(author);

        Recipe recipe = new Recipe();
        recipe.setTitle("Test Recipe");
        recipe.setAuthor(author);
        recipe = recipeRepository.save(recipe);

        Ingredient ingredient = new Ingredient();
        ingredient.setName("Sugar");
        ingredient.setQuantity(-5.0f);
        ingredient.setUnit("cups");
        ingredient.setRecipe(recipe);

        assertThatThrownBy(() -> ingredientRepository.save(ingredient))
                .isInstanceOf(ConstraintViolationException.class);
    }
}
