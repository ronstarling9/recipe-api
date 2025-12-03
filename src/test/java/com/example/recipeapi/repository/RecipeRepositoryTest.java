package com.example.recipeapi.repository;

import com.example.recipeapi.entity.Author;
import com.example.recipeapi.entity.Recipe;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RecipeRepositoryTest {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Test
    void shouldSaveRecipeWithAuthor() {
        Author author = new Author();
        author.setName("Escoffier");
        author = authorRepository.save(author);

        Recipe recipe = new Recipe();
        recipe.setTitle("Peach Melba");
        recipe.setDescription("Classic French dessert");
        recipe.setInstructions("Poach peaches, add ice cream, top with raspberry sauce.");
        recipe.setAuthor(author);

        Recipe saved = recipeRepository.save(recipe);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("Peach Melba");
        assertThat(saved.getAuthor().getName()).isEqualTo("Escoffier");
    }
}
