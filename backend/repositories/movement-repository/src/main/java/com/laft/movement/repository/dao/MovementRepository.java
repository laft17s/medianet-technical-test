package com.laft.movement.repository.dao;

import com.laft.movement.repository.entity.Movement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovementRepository extends JpaRepository<Movement, Long> {
    List<Movement> findByAccountNumber(String accountNumber);
    List<Movement> findByAccountNumberOrderByDateDesc(String accountNumber);
}
