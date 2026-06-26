package com.laft.client.repository.entity;

import jakarta.persistence.*;
import com.laft.client.repository.converter.CryptoConverter;

@Entity
@Table(name = "client", schema = "\"db-clients\"")
@PrimaryKeyJoinColumn(name = "id")
public class Client extends Person {

    @Column(name = "client_id", unique = true, nullable = false)
    private Long clientId;

    @Column(name = "password", nullable = false)
    @Convert(converter = CryptoConverter.class)
    private String password;

    @Column(name = "status", nullable = false)
    private Boolean status;

    // Getters and Setters
    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }
}
