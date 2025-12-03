package com.rgs.recipeapi.dto;

import java.util.List;

public class RecipeData {
    private List<AuthorData> authors;
    private List<RecipeItemData> recipes;

    public List<AuthorData> getAuthors() { return authors; }
    public void setAuthors(List<AuthorData> authors) { this.authors = authors; }
    public List<RecipeItemData> getRecipes() { return recipes; }
    public void setRecipes(List<RecipeItemData> recipes) { this.recipes = recipes; }

    public static class AuthorData {
        private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class RecipeItemData {
        private String title;
        private String description;
        private String instructions;
        private String author;
        private List<IngredientData> ingredients;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getInstructions() { return instructions; }
        public void setInstructions(String instructions) { this.instructions = instructions; }
        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
        public List<IngredientData> getIngredients() { return ingredients; }
        public void setIngredients(List<IngredientData> ingredients) { this.ingredients = ingredients; }
    }

    public static class IngredientData {
        private Float quantity;
        private String unit;
        private String name;

        public Float getQuantity() { return quantity; }
        public void setQuantity(Float quantity) { this.quantity = quantity; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
