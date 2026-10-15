package com.hackacode.hotel;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.net.Socket;

public abstract class AbstractIntegrationTest {

    private static PostgreSQLContainer<?> postgres;
    private static boolean useLocalPostgres = false;

    static {
        // 1. Detectar si PostgreSQL ya está activo en localhost:5432 (ej. Docker Compose)
        try (Socket socket = new Socket("localhost", 5432)) {
            useLocalPostgres = true;
            System.out.println(">>> Conectando a PostgreSQL 16 en localhost:5432 (Docker Compose / Servicio Local)");
        } catch (Exception ignored) {
            // 2. Si no hay instancia local, iniciar Testcontainers dinámicamente
            try {
                postgres = new PostgreSQLContainer<>("postgres:16-alpine")
                        .withDatabaseName("test_agencia_db")
                        .withUsername("test_user")
                        .withPassword("test_pass");
                postgres.start();
                System.out.println(">>> Contenedor Testcontainers PostgreSQL 16 iniciado exitosamente");
            } catch (Exception ex) {
                System.err.println(">>> Error iniciando Testcontainers: " + ex.getMessage());
            }
        }
    }

    public static boolean isDatabaseConnected() {
        return useLocalPostgres || (postgres != null && postgres.isRunning());
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        if (useLocalPostgres) {
            registry.add("spring.datasource.url", () -> "jdbc:postgresql://localhost:5432/agencia_turismo_db");
            registry.add("spring.datasource.username", () -> "agencia_user");
            registry.add("spring.datasource.password", () -> "agencia_password_secret");
        } else if (postgres != null && postgres.isRunning()) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl);
            registry.add("spring.datasource.username", postgres::getUsername);
            registry.add("spring.datasource.password", postgres::getPassword);
        }
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }
}
