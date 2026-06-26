package com.laft.movement.application.service;

import com.laft.account.application.port.in.AccountUseCase;
import com.laft.account.domain.AccountDomain;
import com.laft.commons.exception.InsufficientBalanceException;
import com.laft.movement.application.port.out.MovementEventPort;
import com.laft.movement.application.port.out.MovementRepositoryPort;
import com.laft.movement.domain.MovementDomain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovementServiceTest {

    @Mock
    private MovementRepositoryPort movementRepositoryPort;

    @Mock
    private MovementEventPort movementEventPort;

    @Mock
    private AccountUseCase accountUseCase;

    @InjectMocks
    private MovementService movementService;

    @Test
    void registerMovement_WithEmptyHistory_ShouldUseInitialBalance() {
        MovementDomain movement = new MovementDomain();
        movement.setAccountNumber("12345");
        movement.setValue(BigDecimal.valueOf(50));

        AccountDomain account = new AccountDomain();
        account.setAccountNumber("12345");
        account.setInitialBalance(BigDecimal.valueOf(100));

        when(movementRepositoryPort.findByAccountNumberOrderByDateDesc("12345")).thenReturn(Collections.emptyList());
        when(accountUseCase.getAccount("12345")).thenReturn(account);
        when(movementRepositoryPort.save(any(MovementDomain.class))).thenAnswer(i -> i.getArgument(0));

        MovementDomain result = movementService.registerMovement(movement);

        assertNotNull(result);
        assertEquals(0, result.getBalance().compareTo(BigDecimal.valueOf(150))); // 100 + 50
        verify(movementEventPort, times(1)).publishMovementCreated(any());
    }

    @Test
    void registerMovement_WithExistingHistory_ShouldUseLastBalance() {
        MovementDomain movement = new MovementDomain();
        movement.setAccountNumber("12345");
        movement.setValue(BigDecimal.valueOf(-20));

        MovementDomain lastMovement = new MovementDomain();
        lastMovement.setBalance(BigDecimal.valueOf(200));

        when(movementRepositoryPort.findByAccountNumberOrderByDateDesc("12345")).thenReturn(List.of(lastMovement));
        when(movementRepositoryPort.save(any(MovementDomain.class))).thenAnswer(i -> i.getArgument(0));

        MovementDomain result = movementService.registerMovement(movement);

        assertNotNull(result);
        assertEquals(0, result.getBalance().compareTo(BigDecimal.valueOf(180))); // 200 - 20
        verify(accountUseCase, never()).getAccount(anyString());
        verify(movementEventPort, times(1)).publishMovementCreated(any());
    }

    @Test
    void registerMovement_InsufficientBalance_ShouldThrowException() {
        MovementDomain movement = new MovementDomain();
        movement.setAccountNumber("12345");
        movement.setValue(BigDecimal.valueOf(-200));

        MovementDomain lastMovement = new MovementDomain();
        lastMovement.setBalance(BigDecimal.valueOf(100));

        when(movementRepositoryPort.findByAccountNumberOrderByDateDesc("12345")).thenReturn(List.of(lastMovement));

        InsufficientBalanceException exception = assertThrows(InsufficientBalanceException.class, 
            () -> movementService.registerMovement(movement));

        assertEquals("Saldo no disponible", exception.getMessage());
        verify(movementRepositoryPort, never()).save(any());
        verify(movementEventPort, never()).publishMovementCreated(any());
    }
}
