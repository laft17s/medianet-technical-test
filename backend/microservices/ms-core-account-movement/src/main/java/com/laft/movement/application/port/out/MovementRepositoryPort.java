package com.laft.movement.application.port.out;

import com.laft.movement.domain.MovementDomain;
import java.util.List;

public interface MovementRepositoryPort {
    MovementDomain save(MovementDomain movement);
    java.util.Optional<MovementDomain> findById(Long movementId);
    void deleteById(Long movementId);
    List<MovementDomain> findByAccountNumber(String accountNumber);
    List<MovementDomain> findByAccountNumberOrderByDateDesc(String accountNumber);
    List<MovementDomain> findAll();
}
