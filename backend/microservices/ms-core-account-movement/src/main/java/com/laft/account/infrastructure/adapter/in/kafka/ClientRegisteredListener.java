package com.laft.account.infrastructure.adapter.in.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laft.account.application.port.in.AccountUseCase;
import com.laft.account.domain.AccountDomain;
import com.laft.movement.application.port.in.MovementUseCase;
import com.laft.movement.domain.MovementDomain;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class ClientRegisteredListener {

    private final AccountUseCase accountUseCase;
    private final MovementUseCase movementUseCase;
    private final ObjectMapper objectMapper;

    public ClientRegisteredListener(AccountUseCase accountUseCase, MovementUseCase movementUseCase) {
        this.accountUseCase = accountUseCase;
        this.movementUseCase = movementUseCase;
        this.objectMapper = new ObjectMapper();
    }

    @KafkaListener(topics = "client-registered-topic", groupId = "ms-account-group")
    public void listenClientRegistered(String message) {
        try {
            JsonNode payload = objectMapper.readTree(message);
            Long clientId = payload.get("clientId").asLong();
            String accountType = payload.get("accountType").asText();
            double initialBalance = payload.get("initialBalance").asDouble();

            if (clientId <= 0) {
                System.err.println("Invalid clientId in Kafka message: " + clientId);
                return;
            }

            // Generate unique account number
            String accountNumber = UUID.randomUUID().toString().substring(0, 10).replace("-", "").toUpperCase();

            // Create Account
            AccountDomain account = new AccountDomain();
            account.setAccountNumber(accountNumber);
            account.setAccountType(accountType);
            account.setInitialBalance(BigDecimal.valueOf(0.0)); // Initial is 0, movement adds 5
            account.setStatus(true);
            account.setClientId(clientId);
            
            accountUseCase.createAccount(account);

            // Create Initial Movement
            MovementDomain movement = new MovementDomain();
            movement.setAccountNumber(accountNumber);
            movement.setMovementType("Deposito Inicial");
            movement.setValue(BigDecimal.valueOf(initialBalance));
            movement.setBalance(BigDecimal.valueOf(initialBalance));
            movement.setDate(LocalDateTime.now());
            
            movementUseCase.registerMovement(movement);
            
            System.out.println("Account and initial movement created for client: " + clientId);

        } catch (Exception e) {
            System.err.println("Failed to process client-registered-topic event: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
