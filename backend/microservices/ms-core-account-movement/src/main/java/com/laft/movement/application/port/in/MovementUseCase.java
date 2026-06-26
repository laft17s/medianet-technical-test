package com.laft.movement.application.port.in;

import com.laft.movement.domain.MovementDomain;
import java.math.BigDecimal;
import java.util.List;

public interface MovementUseCase {
    MovementDomain registerMovement(MovementDomain movement);
    MovementDomain getMovementById(Long movementId);
    MovementDomain updateMovement(Long movementId, String movementType, BigDecimal value);
    void deleteMovement(Long movementId);
    List<MovementDomain> getMovementsByAccount(String accountNumber);
    List<MovementDomain> getAllMovements();
}
