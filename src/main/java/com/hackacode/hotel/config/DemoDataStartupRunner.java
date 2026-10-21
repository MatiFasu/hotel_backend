package com.hackacode.hotel.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Profile("demo")
public class DemoDataStartupRunner implements CommandLineRunner {

    private final DemoSeederService demoSeederService;

    @Autowired
    public DemoDataStartupRunner(DemoSeederService demoSeederService) {
        this.demoSeederService = demoSeederService;
    }

    @Override
    public void run(String... args) {
        Map<String, Object> result = demoSeederService.cargarDatosDemoSiVacio();
        System.out.println(">>> [DemoDataStartupRunner] " + result.get("message"));
    }
}
