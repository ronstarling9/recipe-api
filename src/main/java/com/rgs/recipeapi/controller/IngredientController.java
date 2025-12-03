package com.rgs.recipeapi.controller;

import com.rgs.recipeapi.entity.Ingredient;
import com.rgs.recipeapi.repository.IngredientRepository;
import com.rgs.recipeapi.repository.RecipeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recipes/{recipeId}/ingredients")
public class IngredientController {

    private final IngredientRepository ingredientRepository;
    private final RecipeRepository recipeRepository;

    public IngredientController(IngredientRepository ingredientRepository,
                                RecipeRepository recipeRepository) {
        this.ingredientRepository = ingredientRepository;
        this.recipeRepository = recipeRepository;
    }

    @GetMapping
    public ResponseEntity<List<Ingredient>> getIngredients(@PathVariable Long recipeId) {
        if (!recipeRepository.existsById(recipeId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ingredientRepository.findByRecipeId(recipeId));
    }

    @PostMapping
    public ResponseEntity<Ingredient> createIngredient(@PathVariable Long recipeId,
                                                        @RequestBody Ingredient ingredient) {
        return recipeRepository.findById(recipeId)
                .map(recipe -> {
                    ingredient.setRecipe(recipe);
                    // INTENTIONAL BUG: No validation on quantity
                    Ingredient saved = ingredientRepository.save(ingredient);
                    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{ingredientId}")
    public ResponseEntity<Ingredient> updateIngredient(@PathVariable Long recipeId,
                                                        @PathVariable Long ingredientId,
                                                        @RequestBody Ingredient ingredient) {
        if (!recipeRepository.existsById(recipeId)) {
            return ResponseEntity.notFound().build();
        }
        return ingredientRepository.findById(ingredientId)
                .map(existing -> {
                    existing.setName(ingredient.getName());
                    existing.setQuantity(ingredient.getQuantity());
                    existing.setUnit(ingredient.getUnit());
                    return ResponseEntity.ok(ingredientRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{ingredientId}")
    public ResponseEntity<Void> deleteIngredient(@PathVariable Long recipeId,
                                                  @PathVariable Long ingredientId) {
        if (!recipeRepository.existsById(recipeId)) {
            return ResponseEntity.notFound().build();
        }
        if (ingredientRepository.existsById(ingredientId)) {
            ingredientRepository.deleteById(ingredientId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
