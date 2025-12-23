package com.rgs.recipeapi.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Security test to verify H2 console is disabled.
 *
 * The H2 console poses a critical security risk (CVE-2021-42392)
 * allowing remote code execution via JNDI injection.
 */
@SpringBootTest
class H2ConsoleSecurityTest {

    @Value("${spring.h2.console.enabled}")
    private boolean h2ConsoleEnabled;

    @Test
    void shouldHaveH2ConsoleDisabled() {
        assertFalse(h2ConsoleEnabled,
            "H2 console must be disabled for security. " +
            "Set spring.h2.console.enabled=false in application.properties");
    }
}
