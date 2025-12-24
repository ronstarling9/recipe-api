package com.rgs.recipeapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestPropertySource(properties = {
    "app.password=SuperSecret123!@#",
    "app.aws.access.key.id=AKIAIOSFODNN7EXAMPLE"
})
public class EnvironmentConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void shouldLoadEnvironmentConfiguration() {
        // Verify that an EnvironmentConfig bean exists
        Object config = applicationContext.getBean("environmentConfig");
        assertNotNull(config, "EnvironmentConfig bean should be loaded");
    }

    @Test
    public void shouldLoadPasswordFromEnvironment() throws Exception {
        // This test will fail until we create the EnvironmentConfig class
        Object config = applicationContext.getBean("environmentConfig");
        assertNotNull(config, "EnvironmentConfig should exist");

        // Verify the password property is loaded
        String password = (String) config.getClass().getMethod("getPassword").invoke(config);
        assertEquals("SuperSecret123!@#", password, "Password should be loaded from environment");
    }

    @Test
    public void shouldLoadAwsAccessKeyFromEnvironment() throws Exception {
        // This test will fail until we create the EnvironmentConfig class
        Object config = applicationContext.getBean("environmentConfig");
        assertNotNull(config, "EnvironmentConfig should exist");

        // Verify the AWS access key property is loaded
        String awsKey = (String) config.getClass().getMethod("getAwsAccessKeyId").invoke(config);
        assertEquals("AKIAIOSFODNN7EXAMPLE", awsKey, "AWS access key should be loaded from environment");
    }
}
