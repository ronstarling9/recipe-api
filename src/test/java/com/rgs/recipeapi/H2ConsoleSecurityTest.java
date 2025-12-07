package com.rgs.recipeapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
class H2ConsoleSecurityTest {

    @Value("${spring.h2.console.enabled:false}")
    private boolean h2ConsoleEnabled;

    @Test
    void h2ConsoleShouldBeDisabled() {
        // The H2 console must be disabled for security reasons.
        // When enabled, it provides an attack vector for arbitrary SQL execution,
        // data exfiltration, and potential RCE.
        assertFalse(h2ConsoleEnabled,
            "H2 console is enabled! This is a security vulnerability. " +
            "Set spring.h2.console.enabled=false in application.properties");
    }
}
