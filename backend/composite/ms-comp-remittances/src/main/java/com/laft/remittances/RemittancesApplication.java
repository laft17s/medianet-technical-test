package com.laft.remittances;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication(scanBasePackages = "com.laft")
@OpenAPIDefinition(info = @Info(title = "Medianet Core Banking API", version = "1.0", description = "API for Remittances, Accounts, and Clients"))
public class RemittancesApplication {
    public static void main(String[] args) {
        SpringApplication.run(RemittancesApplication.class, args);
    }
}
