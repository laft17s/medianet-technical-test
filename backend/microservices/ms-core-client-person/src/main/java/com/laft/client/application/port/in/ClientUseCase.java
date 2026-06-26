package com.laft.client.application.port.in;

import com.laft.client.domain.ClientDomain;
import java.util.List;

public interface ClientUseCase {
    ClientDomain createClient(ClientDomain client);
    ClientDomain getClient(Long clientId);
    ClientDomain updateClient(ClientDomain client);
    ClientDomain updateClientStatus(Long clientId, Boolean status);
    void deleteClient(Long clientId);
    List<ClientDomain> getAllClients();
    ClientDomain validateCredentials(String identification, String password);
}
