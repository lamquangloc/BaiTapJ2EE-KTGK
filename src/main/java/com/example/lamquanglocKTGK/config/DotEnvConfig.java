package com.example.lamquanglocKTGK.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DotEnvConfig {
    static {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        // Load .env variables into system environment
        dotenv.entries()
                .forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
    }
}
