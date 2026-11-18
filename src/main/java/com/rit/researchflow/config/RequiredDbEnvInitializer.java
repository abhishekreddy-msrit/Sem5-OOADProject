package com.rit.researchflow.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Fails fast with a clear message when required MariaDB settings are missing.
 * Runs after {@link DotEnvEnvironmentPostProcessor}.
 */
public class RequiredDbEnvInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final List<String> REQUIRED = List.of("DB_URL", "DB_USER", "DB_PASSWORD");

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        List<String> missing = new ArrayList<>();
        for (String key : REQUIRED) {
            String value = environment.getProperty(key);
            if (value == null || value.isBlank() || value.startsWith("${")) {
                missing.add(key);
            }
        }
        if (!missing.isEmpty()) {
            throw new IllegalStateException(
                    "Missing required database configuration: " + String.join(", ", missing)
                            + ". Copy .env.example to .env in the project root and set DB_URL, DB_USER, and DB_PASSWORD "
                            + "(or export the same variables in your environment). Do not commit .env.");
        }
    }
}
