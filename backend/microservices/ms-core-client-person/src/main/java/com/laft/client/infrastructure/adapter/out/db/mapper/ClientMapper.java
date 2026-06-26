package com.laft.client.infrastructure.adapter.out.db.mapper;

import com.laft.client.domain.ClientDomain;
import com.laft.client.repository.entity.Client;
import org.springframework.stereotype.Component;

@Component
public class ClientMapper {
    public ClientDomain toDomain(Client entity) {
        if (entity == null) return null;
        ClientDomain domain = new ClientDomain();
        domain.setId(entity.getId());
        domain.setName(entity.getName());
        domain.setGender(entity.getGender());
        domain.setAge(entity.getAge());
        domain.setIdentification(entity.getIdentification());
        domain.setAddress(entity.getAddress());
        domain.setPhone(entity.getPhone());
        domain.setClientId(entity.getClientId());
        domain.setPassword(entity.getPassword());
        domain.setStatus(entity.getStatus());
        return domain;
    }

    public Client toEntity(ClientDomain domain) {
        if (domain == null) return null;
        Client entity = new Client();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setGender(domain.getGender());
        entity.setAge(domain.getAge());
        entity.setIdentification(domain.getIdentification());
        entity.setAddress(domain.getAddress());
        entity.setPhone(domain.getPhone());
        entity.setClientId(domain.getClientId());
        entity.setPassword(domain.getPassword());
        entity.setStatus(domain.getStatus());
        return entity;
    }
}
