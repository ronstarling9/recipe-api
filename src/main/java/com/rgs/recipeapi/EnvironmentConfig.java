package com.rgs.recipeapi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentConfig {

    @Value("${app.password:}")
    private String password;

    @Value("${app.aws.access.key.id:}")
    private String awsAccessKeyId;

    public String getPassword() {
        return password;
    }

    public String getAwsAccessKeyId() {
        return awsAccessKeyId;
    }
}
