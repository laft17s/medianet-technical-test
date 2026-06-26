package com.laft.movement.infrastructure.adapter.out.db.mapper;

import com.laft.movement.domain.MovementDomain;
import com.laft.movement.repository.entity.Movement;
import com.laft.movement.repository.converter.CryptoConverter;
import org.springframework.stereotype.Component;

@Component
public class MovementMapper {
    private final CryptoConverter cryptoConverter = new CryptoConverter();

    public MovementDomain toDomain(Movement entity) {
        if (entity == null) return null;
        MovementDomain domain = new MovementDomain();
        domain.setMovementId(entity.getId());
        domain.setDate(entity.getDate());
        domain.setMovementType(entity.getMovementType());
        domain.setValue(entity.getValue());
        domain.setBalance(entity.getBalance());
        domain.setAccountNumber(cryptoConverter.convertToEntityAttribute(entity.getAccountNumber()));
        return domain;
    }

    public Movement toEntity(MovementDomain domain) {
        if (domain == null) return null;
        Movement entity = new Movement();
        entity.setId(domain.getMovementId());
        entity.setDate(domain.getDate());
        entity.setMovementType(domain.getMovementType());
        entity.setValue(domain.getValue());
        entity.setBalance(domain.getBalance());
        entity.setAccountNumber(cryptoConverter.convertToDatabaseColumn(domain.getAccountNumber()));
        return entity;
    }
}
