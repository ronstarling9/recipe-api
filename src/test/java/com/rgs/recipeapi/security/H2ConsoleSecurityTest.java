package com.rgs.recipeapi.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class H2ConsoleSecurityTest {

    @Autowired
    private Environment environment;

    @Test
    void shouldDisableH2Console() {
        // The H2 console must be explicitly disabled to prevent unauthorized database access
        String h2ConsoleEnabled = environment.getProperty("spring.h2.console.enabled");
        assertEquals("false", h2ConsoleEnabled,
                "H2 console must be disabled for security. Found: " + h2ConsoleEnabled);
    }
}
