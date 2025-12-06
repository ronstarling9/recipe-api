package com.rgs.recipeapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security test for RCPAPI-1: H2 Database Console Exposed
 *
 * The H2 console must be disabled to prevent attackers from:
 * - Executing arbitrary SQL (DROP, INSERT, etc.)
 * - Exfiltrating data
 * - Potentially achieving RCE
 */
@SpringBootTest
@AutoConfigureMockMvc
class H2ConsoleSecurityTest {

    @Value("${spring.h2.console.enabled:false}")
    private boolean h2ConsoleEnabled;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void h2ConsoleShouldBeDisabledInConfiguration() {
        // The H2 console must be explicitly disabled in configuration
        // This prevents the console from being exposed even if servlet registration changes
        assertFalse(h2ConsoleEnabled,
            "H2 console must be disabled (spring.h2.console.enabled=false) - " +
            "exposing it is a security vulnerability");
    }

    @Test
    void h2ConsoleEndpointShouldNotBeAccessible() throws Exception {
        // The H2 console endpoint should return 404 (not found)
        mockMvc.perform(get("/h2-console"))
                .andExpect(status().isNotFound());
    }
}
