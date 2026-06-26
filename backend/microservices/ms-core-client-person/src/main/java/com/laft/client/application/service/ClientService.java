package com.laft.client.application.service;

import com.laft.client.application.port.in.ClientUseCase;
import com.laft.client.application.port.out.ClientRepositoryPort;
import com.laft.client.domain.ClientDomain;
import com.laft.commons.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClientService implements ClientUseCase {

    private final ClientRepositoryPort clientRepositoryPort;

    public ClientService(ClientRepositoryPort clientRepositoryPort) {
        this.clientRepositoryPort = clientRepositoryPort;
    }

    @Override
    public ClientDomain createClient(ClientDomain client) {
        return clientRepositoryPort.save(client);
    }

    @Override
    public ClientDomain getClient(Long clientId) {
        return clientRepositoryPort.findByClientId(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));
    }

    @Override
    public ClientDomain updateClient(ClientDomain client) {
        ClientDomain existing = getClient(client.getClientId());
        // Update logic
        existing.setName(client.getName());
        existing.setGender(client.getGender());
        existing.setAge(client.getAge());
        existing.setAddress(client.getAddress());
        existing.setPhone(client.getPhone());
        existing.setPassword(client.getPassword());
        existing.setStatus(client.getStatus());
        return clientRepositoryPort.save(existing);
    }

    @Override
    public ClientDomain updateClientStatus(Long clientId, Boolean status) {
        ClientDomain existing = getClient(clientId);
        existing.setStatus(status);
        return clientRepositoryPort.save(existing);
    }

    @Override
    public void deleteClient(Long clientId) {
        clientRepositoryPort.delete(clientId);
    }

    @Override
    public List<ClientDomain> getAllClients() {
        return clientRepositoryPort.findAll();
    }



    public ClientDomain validateCredentials(String identification, String password) {
        Optional<ClientDomain> clientOpt = clientRepositoryPort.findByIdentification(identification);
        if (clientOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        ClientDomain client = clientOpt.get();
        if (!client.getPassword().equals(password)) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        return client;
    }
}
