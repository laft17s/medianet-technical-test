package com.laft.movement.infrastructure.adapter.out.db;

import com.laft.movement.application.port.out.MovementRepositoryPort;
import com.laft.movement.domain.MovementDomain;
import com.laft.movement.infrastructure.adapter.out.db.mapper.MovementMapper;
import com.laft.movement.repository.dao.MovementRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MovementDbAdapter implements MovementRepositoryPort {
    private final MovementRepository repository;
    private final MovementMapper mapper;

    public MovementDbAdapter(MovementRepository repository, MovementMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public MovementDomain save(MovementDomain movement) {
        return mapper.toDomain(repository.save(mapper.toEntity(movement)));
    }

    @Override
    public List<MovementDomain> findByAccountNumber(String accountNumber) {
        com.laft.movement.repository.converter.CryptoConverter crypto = new com.laft.movement.repository.converter.CryptoConverter();
        String encryptedId = crypto.convertToDatabaseColumn(accountNumber);
        return repository.findByAccountNumber(encryptedId).stream()
                .map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<MovementDomain> findByAccountNumberOrderByDateDesc(String accountNumber) {
        com.laft.movement.repository.converter.CryptoConverter crypto = new com.laft.movement.repository.converter.CryptoConverter();
        String encryptedId = crypto.convertToDatabaseColumn(accountNumber);
        return repository.findByAccountNumberOrderByDateDesc(encryptedId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<MovementDomain> findById(Long movementId) {
        return repository.findById(movementId).map(mapper::toDomain);
    }

    @Override
    public void deleteById(Long movementId) {
        repository.deleteById(movementId);
    }

    @Override
    public List<MovementDomain> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
