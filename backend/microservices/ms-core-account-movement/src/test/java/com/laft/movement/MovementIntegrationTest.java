package com.laft.movement;

import com.laft.account.domain.AccountDomain;
import com.laft.account.application.port.in.AccountUseCase;
import com.laft.movement.domain.MovementDomain;
import com.laft.movement.application.port.in.MovementUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class MovementIntegrationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @SuppressWarnings("unchecked")
        public KafkaTemplate<String, String> kafkaTemplate() {
            return mock(KafkaTemplate.class);
        }
    }

    @Autowired
    private MovementUseCase movementUseCase;

    @Autowired
    private AccountUseCase accountUseCase;

    @Test
    public void testCreateMovementUpdatesBalanceAndPersists() {
        // Arrange: Create an account
        AccountDomain account = new AccountDomain();
        account.setAccountNumber("INT-TEST-001");
        account.setAccountType("Ahorros");
        account.setInitialBalance(new java.math.BigDecimal("100.0"));
        account.setStatus(true);
        account.setClientId(1L);
        accountUseCase.createAccount(account);

        // Act: Create a deposit movement
        MovementDomain deposit = new MovementDomain();
        deposit.setAccountNumber("INT-TEST-001");
        deposit.setMovementType("DEPOSITO");
        deposit.setValue(new java.math.BigDecimal("50.0"));
        MovementDomain createdDeposit = movementUseCase.registerMovement(deposit);

        // Assert: Check balance updated
        assertNotNull(createdDeposit.getMovementId());
        assertTrue(new java.math.BigDecimal("150.00").compareTo(createdDeposit.getBalance()) == 0, "Balance should be 150.00");
    }

    @Test
    public void testCreateMovementInsufficientBalanceThrowsException() {
        // Arrange: Create an account
        AccountDomain account = new AccountDomain();
        account.setAccountNumber("INT-TEST-002");
        account.setAccountType("Ahorros");
        account.setInitialBalance(new java.math.BigDecimal("50.0"));
        account.setStatus(true);
        account.setClientId(1L);
        accountUseCase.createAccount(account);

        // Act & Assert: Attempt to withdraw more than available
        MovementDomain withdrawal = new MovementDomain();
        withdrawal.setAccountNumber("INT-TEST-002");
        withdrawal.setMovementType("RETIRO");
        withdrawal.setValue(new java.math.BigDecimal("-100.0")); // Service uses addition, so withdrawal must be negative

        // Assuming service throws exception (InsufficientBalanceException)
        assertThrows(RuntimeException.class, () -> {
            movementUseCase.registerMovement(withdrawal);
        });
    }
}
