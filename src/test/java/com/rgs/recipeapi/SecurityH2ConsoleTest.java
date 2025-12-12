package com.rgs.recipeapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityH2ConsoleTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void h2ConsoleShouldBeDisabled() throws Exception {
        // The H2 console should not be accessible as it's a security risk
        mockMvc.perform(get("/h2-console"))
                .andExpect(status().isNotFound());
    }
}
