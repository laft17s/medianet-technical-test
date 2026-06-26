package com.laft.client.infrastructure.config;

import com.laft.client.application.port.out.ClientRepositoryPort;
import com.laft.client.domain.ClientDomain;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;

@Configuration
public class AdminSeeder {

    @Value("${app.admin.name}")
    private String adminName;

    @Value("${app.admin.gender}")
    private String adminGender;

    @Value("${app.admin.age}")
    private Integer adminAge;

    @Value("${app.admin.identification}")
    private String adminIdentification;

    @Value("${app.admin.address}")
    private String adminAddress;

    @Value("${app.admin.phone}")
    private String adminPhone;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Bean
    public CommandLineRunner seedAdmin(ClientRepositoryPort clientRepositoryPort, org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        return args -> {
            Optional<ClientDomain> adminOpt = clientRepositoryPort.findByClientId(9999L);
            ClientDomain admin;
            if (adminOpt.isEmpty()) {
                try {
                    jdbcTemplate.execute("SELECT setval(pg_get_serial_sequence('\"db-clients\".person', 'id'), coalesce(max(id),0) + 1, false) FROM \"db-clients\".person;");
                } catch (Exception e) {
                    System.out.println("Could not update sequence: " + e.getMessage());
                }
                admin = new ClientDomain();
                admin.setClientId(9999L);
            } else {
                admin = adminOpt.get();
            }
            
            admin.setName(adminName);
            admin.setGender(adminGender);
            admin.setAge(adminAge);
            admin.setIdentification(adminIdentification);
            admin.setAddress(adminAddress);
            admin.setPhone(adminPhone);
            admin.setPassword(adminPassword);
            admin.setStatus(true);
            
            clientRepositoryPort.save(admin);
            System.out.println("Admin user seeded/updated successfully.");
        };
    }
}
