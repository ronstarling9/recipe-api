package com.rgs.recipeapi.controller;

import com.rgs.recipeapi.dto.RecipeData;
import com.rgs.recipeapi.entity.Author;
import com.rgs.recipeapi.entity.Ingredient;
import com.rgs.recipeapi.entity.Recipe;
import com.rgs.recipeapi.repository.AuthorRepository;
import com.rgs.recipeapi.repository.IngredientRepository;
import com.rgs.recipeapi.repository.RecipeRepository;
import tools.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AuthorRepository authorRepository;
    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    private final ObjectMapper objectMapper;

    public AdminController(AuthorRepository authorRepository,
                          RecipeRepository recipeRepository,
                          IngredientRepository ingredientRepository,
                          ObjectMapper objectMapper) {
        this.authorRepository = authorRepository;
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        try {
            authorRepository.count();
            health.put("database", "UP");
        } catch (Exception e) {
            health.put("database", "DOWN");
        }
        return ResponseEntity.ok(health);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> stats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("authorCount", authorRepository.count());
        stats.put("recipeCount", recipeRepository.count());
        stats.put("ingredientCount", ingredientRepository.count());
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/data/reload")
    public ResponseEntity<Map<String, Object>> reloadData() {
        try {
            // Clear existing data in correct order (FK constraints)
            ingredientRepository.deleteAll();
            recipeRepository.deleteAll();
            authorRepository.deleteAll();

            // Load data from JSON
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
            int recipesLoaded = 0;
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
                recipesLoaded++;
            }

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Data reloaded successfully");
            result.put("recipesLoaded", recipesLoaded);
            result.put("authorsLoaded", authorMap.size());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Failed to reload data");
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
