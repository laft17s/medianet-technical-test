package com.laft.client.infrastructure.adapter.out.db;

import com.laft.client.application.port.out.ClientRepositoryPort;
import com.laft.client.domain.ClientDomain;
import com.laft.client.infrastructure.adapter.out.db.mapper.ClientMapper;
import com.laft.client.repository.dao.ClientRepository;
import com.laft.client.repository.entity.Client;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ClientDbAdapter implements ClientRepositoryPort {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    public ClientDbAdapter(ClientRepository clientRepository, ClientMapper clientMapper) {
        this.clientRepository = clientRepository;
        this.clientMapper = clientMapper;
    }

    @Override
    @Transactional
    public ClientDomain save(ClientDomain client) {
        Client entity = clientMapper.toEntity(client);
        if (entity.getClientId() == null || entity.getClientId() == 0L) {
            // Assign a temporary random ID to pass the NOT NULL constraint
            entity.setClientId((long) (Math.random() * Integer.MAX_VALUE));
            entity = clientRepository.save(entity);
            // Sync client_id with the auto-generated id
            entity.setClientId(entity.getId());
        }
        return clientMapper.toDomain(clientRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ClientDomain> findByClientId(Long clientId) {
        return clientRepository.findByClientId(clientId).map(clientMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ClientDomain> findByIdentification(String identification) {
        return clientRepository.findByIdentification(identification).map(clientMapper::toDomain);
    }

    @Override
    @Transactional
    public void delete(Long clientId) {
        clientRepository.deleteByClientId(clientId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientDomain> findAll() {
        return clientRepository.findAll().stream().map(clientMapper::toDomain).collect(Collectors.toList());
    }
}
