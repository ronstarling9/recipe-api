package com.rgs.recipeapi.controller;

import com.rgs.recipeapi.repository.AuthorRepository;
import com.rgs.recipeapi.repository.IngredientRepository;
import com.rgs.recipeapi.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

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
    void shouldReturnHealthStatus() throws Exception {
        mockMvc.perform(get("/admin/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.database").value("UP"));
    }

    @Test
    void shouldReturnDatabaseStats() throws Exception {
        mockMvc.perform(get("/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authorCount").exists())
                .andExpect(jsonPath("$.recipeCount").exists())
                .andExpect(jsonPath("$.ingredientCount").exists());
    }

    @Test
    void shouldReloadSeedData() throws Exception {
        // Verify database is empty after setUp
        mockMvc.perform(get("/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authorCount").value(0))
                .andExpect(jsonPath("$.recipeCount").value(0));

        // Reload seed data
        mockMvc.perform(post("/admin/data/reload"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Data reloaded successfully"))
                .andExpect(jsonPath("$.recipesLoaded").value(greaterThanOrEqualTo(1)));

        // Verify data was loaded
        mockMvc.perform(get("/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recipeCount").value(greaterThanOrEqualTo(1)));
    }
}
