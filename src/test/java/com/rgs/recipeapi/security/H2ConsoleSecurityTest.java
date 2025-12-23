package com.rgs.recipeapi.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class H2ConsoleSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void h2ConsoleShouldBeDisabled() throws Exception {
        // Security Test: Verify H2 database console is NOT accessible
        // The H2 console at /h2-console is a known RCE vulnerability vector
        // See: CVE-2021-42392, https://jfrog.com/blog/the-jndi-strikes-back-unauthenticated-rce-in-h2-database-console/
        //
        // Expected: 404 Not Found (console disabled)
        // Vulnerable: 200 OK or 3xx redirect (console enabled)

        mockMvc.perform(get("/h2-console"))
                .andExpect(status().isNotFound());
    }
}
