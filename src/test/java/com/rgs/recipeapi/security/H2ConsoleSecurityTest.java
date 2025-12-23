package com.rgs.recipeapi.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class H2ConsoleSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldNotExposeH2Console() throws Exception {
        mockMvc.perform(get("/h2-console"))
                .andExpect(status().isNotFound());
    }
}
