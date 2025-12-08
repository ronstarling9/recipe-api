package com.rgs.recipeapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Security test to verify H2 console is disabled.
 * See RCPAPI-1: H2 Database Console vulnerability.
 */
@SpringBootTest
class H2ConsoleDisabledTest {

    @Value("${spring.h2.console.enabled:false}")
    private boolean h2ConsoleEnabled;

    @Test
    void h2ConsoleShouldBeDisabled() {
        // The H2 console must be disabled to prevent database compromise
        assertFalse(h2ConsoleEnabled,
            "H2 console must be disabled. Set spring.h2.console.enabled=false in application.properties");
    }
}
