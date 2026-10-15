package com.hackacode.hotel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class HotelApplicationTests extends AbstractIntegrationTest {

    @Test
    @DisplayName("El contexto de Spring Boot levanta correctamente conectado a PostgreSQL 16 con Flyway")
    void contextLoads() {
        assertTrue(AbstractIntegrationTest.isDatabaseConnected(), "La base de datos PostgreSQL debe estar conectada");
    }

}
