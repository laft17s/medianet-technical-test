package com.laft.movement.application.service;

import com.laft.account.application.port.in.AccountUseCase;
import com.laft.account.domain.AccountDomain;
import com.laft.commons.exception.InsufficientBalanceException;
import com.laft.commons.exception.ResourceNotFoundException;
import com.laft.movement.application.port.in.MovementUseCase;
import com.laft.movement.application.port.out.MovementEventPort;
import com.laft.movement.application.port.out.MovementRepositoryPort;
import com.laft.movement.domain.MovementDomain;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MovementService implements MovementUseCase {

    private final MovementRepositoryPort movementRepositoryPort;
    private final MovementEventPort movementEventPort;
    private final AccountUseCase accountUseCase;

    public MovementService(MovementRepositoryPort movementRepositoryPort, 
                           MovementEventPort movementEventPort, 
                           AccountUseCase accountUseCase) {
        this.movementRepositoryPort = movementRepositoryPort;
        this.movementEventPort = movementEventPort;
        this.accountUseCase = accountUseCase;
    }

    @Override
    @Transactional
    public MovementDomain registerMovement(MovementDomain movement) {
        List<MovementDomain> history = movementRepositoryPort.findByAccountNumberOrderByDateDesc(movement.getAccountNumber());
        BigDecimal currentBalance;
        
        if (history.isEmpty()) {
            AccountDomain account = accountUseCase.getAccount(movement.getAccountNumber());
            currentBalance = account.getInitialBalance();
        } else {
            currentBalance = history.get(0).getBalance();
        }

        BigDecimal newBalance = currentBalance.add(movement.getValue());

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientBalanceException("Saldo no disponible");
        }

        movement.setBalance(newBalance);
        movement.setDate(LocalDateTime.now());
        
        MovementDomain saved = movementRepositoryPort.save(movement);
        
        movementEventPort.publishMovementCreated(saved);
        
        return saved;
    }

    @Override
    public List<MovementDomain> getAllMovements() {
        return movementRepositoryPort.findAll();
    }

    @Override
    public List<MovementDomain> getMovementsByAccount(String accountNumber) {
        return movementRepositoryPort.findByAccountNumberOrderByDateDesc(accountNumber);
    }

    @Override
    public MovementDomain getMovementById(Long movementId) {
        return movementRepositoryPort.findById(movementId)
                .orElseThrow(() -> new ResourceNotFoundException("Movement not found: " + movementId));
    }

    @Override
    @Transactional
    public MovementDomain updateMovement(Long movementId, String movementType, BigDecimal value) {
        MovementDomain existing = getMovementById(movementId);
        if (movementType != null && !movementType.isBlank()) {
            existing.setMovementType(movementType);
        }
        if (value != null) {
            existing.setValue(value);
        }
        return movementRepositoryPort.save(existing);
    }

    @Override
    @Transactional
    public void deleteMovement(Long movementId) {
        getMovementById(movementId);
        movementRepositoryPort.deleteById(movementId);
    }
}
