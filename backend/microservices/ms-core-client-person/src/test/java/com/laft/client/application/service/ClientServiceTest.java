package com.laft.client.application.service;

import com.laft.commons.exception.ResourceNotFoundException;
import com.laft.client.application.port.out.ClientRepositoryPort;
import com.laft.client.domain.ClientDomain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepositoryPort clientRepositoryPort;

    @InjectMocks
    private ClientService clientService;

    @Test
    void createClient_ShouldReturnSavedClient() {
        ClientDomain newClient = new ClientDomain();
        newClient.setName("Test User");
        newClient.setIdentification("123456");

        ClientDomain savedClient = new ClientDomain();
        savedClient.setClientId(1L);
        savedClient.setName("Test User");
        savedClient.setIdentification("123456");

        when(clientRepositoryPort.save(any(ClientDomain.class))).thenReturn(savedClient);

        ClientDomain result = clientService.createClient(newClient);

        assertNotNull(result);
        assertEquals(1L, result.getClientId());
        assertEquals("Test User", result.getName());
        verify(clientRepositoryPort, times(1)).save(newClient);
    }

    @Test
    void getClient_WhenExists_ShouldReturnClient() {
        ClientDomain existingClient = new ClientDomain();
        existingClient.setClientId(1L);
        existingClient.setName("Existing User");

        when(clientRepositoryPort.findByClientId(1L)).thenReturn(Optional.of(existingClient));

        ClientDomain result = clientService.getClient(1L);

        assertNotNull(result);
        assertEquals("Existing User", result.getName());
        verify(clientRepositoryPort, times(1)).findByClientId(1L);
    }

    @Test
    void getClient_WhenNotExists_ShouldThrowResourceNotFoundException() {
        when(clientRepositoryPort.findByClientId(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> clientService.getClient(99L));
        verify(clientRepositoryPort, times(1)).findByClientId(99L);
    }

    @Test
    void deleteClient_WhenExists_ShouldCallDelete() {
        doNothing().when(clientRepositoryPort).delete(1L);

        clientService.deleteClient(1L);

        verify(clientRepositoryPort, times(1)).delete(1L);
    }
}
