package com.rgs.recipeapi.controller;

import com.rgs.recipeapi.entity.Author;
import com.rgs.recipeapi.entity.Ingredient;
import com.rgs.recipeapi.entity.Recipe;
import com.rgs.recipeapi.repository.RecipeRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/recipes")
public class RecipeController {

    private final RecipeRepository recipeRepository;

    public RecipeController(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    @GetMapping
    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    @GetMapping("/search")
    public List<Recipe> searchRecipes(@RequestParam(required = false) List<String> keywords) {
        if (keywords == null || keywords.isEmpty() || keywords.stream().allMatch(String::isBlank)) {
            return List.of();
        }

        Specification<Recipe> spec = (root, query, cb) -> {
            List<Predicate> keywordPredicates = new ArrayList<>();

            for (String keyword : keywords) {
                if (keyword == null || keyword.isBlank()) {
                    continue;
                }
                String pattern = "%" + keyword.toLowerCase() + "%";
                List<Predicate> fieldPredicates = new ArrayList<>();

                fieldPredicates.add(cb.like(cb.lower(root.get("title")), pattern));
                fieldPredicates.add(cb.like(cb.lower(root.get("description")), pattern));
                fieldPredicates.add(cb.like(cb.lower(root.get("instructions")), pattern));

                Join<Recipe, Author> authorJoin = root.join("author", JoinType.LEFT);
                fieldPredicates.add(cb.like(cb.lower(authorJoin.get("name")), pattern));

                Join<Recipe, Ingredient> ingredientJoin = root.join("ingredients", JoinType.LEFT);
                fieldPredicates.add(cb.like(cb.lower(ingredientJoin.get("name")), pattern));

                keywordPredicates.add(cb.or(fieldPredicates.toArray(new Predicate[0])));
            }

            query.distinct(true);
            return cb.and(keywordPredicates.toArray(new Predicate[0]));
        };

        return recipeRepository.findAll(spec);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Recipe> getRecipeById(@PathVariable Long id) {
        return recipeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Recipe> createRecipe(@RequestBody Recipe recipe) {
        Recipe saved = recipeRepository.save(recipe);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Recipe> updateRecipe(@PathVariable Long id, @RequestBody Recipe recipe) {
        return recipeRepository.findById(id)
                .map(existing -> {
                    existing.setTitle(recipe.getTitle());
                    existing.setDescription(recipe.getDescription());
                    existing.setInstructions(recipe.getInstructions());
                    existing.setAuthor(recipe.getAuthor());
                    return ResponseEntity.ok(recipeRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id) {
        if (recipeRepository.existsById(id)) {
            recipeRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
