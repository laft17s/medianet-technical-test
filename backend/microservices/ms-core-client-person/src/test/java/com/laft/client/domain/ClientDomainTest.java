package com.laft.client.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClientDomainTest {

    @Test
    void shouldInheritPersonAttributes() {
        ClientDomain client = new ClientDomain();
        client.setName("Jose Lema");
        client.setGender("Masculino");
        client.setAge(30);
        client.setIdentification("1710034065");
        client.setAddress("Otavalo sn y principal");
        client.setPhone("098254785");
        client.setClientId(1L);
        client.setPassword("1234");
        client.setStatus(true);

        assertEquals("Jose Lema", client.getName());
        assertEquals("1710034065", client.getIdentification());
        assertTrue(client.getStatus());
        assertEquals(1L, client.getClientId());
    }

    @Test
    void inactiveClientShouldHaveStatusFalse() {
        ClientDomain client = new ClientDomain();
        client.setStatus(false);

        assertFalse(client.getStatus());
    }
}
