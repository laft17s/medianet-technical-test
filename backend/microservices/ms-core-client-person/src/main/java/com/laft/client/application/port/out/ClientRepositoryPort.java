package com.laft.client.application.port.out;

import com.laft.client.domain.ClientDomain;
import java.util.List;
import java.util.Optional;

public interface ClientRepositoryPort {
    ClientDomain save(ClientDomain client);
    Optional<ClientDomain> findByClientId(Long clientId);
    Optional<ClientDomain> findByIdentification(String identification);
    void delete(Long clientId);
    List<ClientDomain> findAll();
}
