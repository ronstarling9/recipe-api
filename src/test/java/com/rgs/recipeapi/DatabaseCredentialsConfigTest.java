package com.rgs.recipeapi;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that database credentials are configured to use environment variables
 * with secure defaults, rather than hardcoded weak credentials.
 */
class DatabaseCredentialsConfigTest {

    @Test
    void shouldConfigureDatasourceWithEnvironmentVariablePlaceholders() throws IOException {
        Properties properties = new Properties();
        properties.load(new ClassPathResource("application.properties").getInputStream());

        String username = properties.getProperty("spring.datasource.username");
        String password = properties.getProperty("spring.datasource.password");

        // Verify username uses environment variable placeholder with default
        assertThat(username)
            .as("datasource.username should use DB_USERNAME env var with 'sa' as default")
            .isEqualTo("${DB_USERNAME:sa}");

        // Verify password uses environment variable placeholder with default
        assertThat(password)
            .as("datasource.password should use DB_PASSWORD env var with empty default")
            .isEqualTo("${DB_PASSWORD:}");
    }
}
