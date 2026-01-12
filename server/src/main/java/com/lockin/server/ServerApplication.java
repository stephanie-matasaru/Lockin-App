package com.lockin.server;

import com.lockin.server.entities.Operator;
import com.lockin.server.repositories.OperatorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @Bean
    public CommandLineRunner run(OperatorRepository repository) {
        return args -> {
            Operator existingAdmin = repository.findByUsername("admin").orElse(null);

            if (existingAdmin == null) {
                Operator admin = new Operator(
                        "admin",
                        "admin123",
                        "Super Admin",
                        "0000",
                        "admin@lockin.com"
                );
                repository.save(admin);
                System.out.println("✅ DEFAULT ADMIN CREATED: User: 'admin', Pass: 'admin123'");
            } else {
                System.out.println("ℹ️ ADMIN EXISTS: Login with 'admin' / 'admin123'");
            }
        };
    }
}