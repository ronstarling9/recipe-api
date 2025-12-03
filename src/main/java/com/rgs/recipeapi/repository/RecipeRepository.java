package com.rgs.recipeapi.repository;

import com.rgs.recipeapi.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
}
