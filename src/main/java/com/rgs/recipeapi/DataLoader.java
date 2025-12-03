package com.rgs.recipeapi;

import com.rgs.recipeapi.dto.RecipeData;
import com.rgs.recipeapi.entity.Author;
import com.rgs.recipeapi.entity.Ingredient;
import com.rgs.recipeapi.entity.Recipe;
import com.rgs.recipeapi.repository.AuthorRepository;
import com.rgs.recipeapi.repository.IngredientRepository;
import com.rgs.recipeapi.repository.RecipeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Component
public class DataLoader implements CommandLineRunner {

    private final AuthorRepository authorRepository;
    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    private final ObjectMapper objectMapper;

    public DataLoader(AuthorRepository authorRepository,
                      RecipeRepository recipeRepository,
                      IngredientRepository ingredientRepository,
                      ObjectMapper objectMapper) {
        this.authorRepository = authorRepository;
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        InputStream inputStream = new ClassPathResource("data/recipes.json").getInputStream();
        RecipeData data = objectMapper.readValue(inputStream, RecipeData.class);

        // Create authors and build lookup map
        Map<String, Author> authorMap = new HashMap<>();
        for (RecipeData.AuthorData authorData : data.getAuthors()) {
            Author author = new Author();
            author.setName(authorData.getName());
            author = authorRepository.save(author);
            authorMap.put(authorData.getName(), author);
        }

        // Create recipes with ingredients
        for (RecipeData.RecipeItemData recipeData : data.getRecipes()) {
            Recipe recipe = new Recipe();
            recipe.setTitle(recipeData.getTitle());
            recipe.setDescription(recipeData.getDescription());
            recipe.setInstructions(recipeData.getInstructions());
            recipe.setAuthor(authorMap.get(recipeData.getAuthor()));
            recipe = recipeRepository.save(recipe);

            for (RecipeData.IngredientData ingredientData : recipeData.getIngredients()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setQuantity(ingredientData.getQuantity());
                ingredient.setUnit(ingredientData.getUnit());
                ingredient.setName(ingredientData.getName());
                ingredient.setRecipe(recipe);
                ingredientRepository.save(ingredient);
            }
        }

        System.out.println("Loaded " + data.getRecipes().size() + " recipes from JSON.");
    }
}
