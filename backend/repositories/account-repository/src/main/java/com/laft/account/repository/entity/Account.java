package com.laft.account.repository.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import com.laft.account.repository.converter.CryptoConverter;
import java.util.UUID;

@Entity
@Table(name = "account", schema = "\"db-accounts\"")
public class Account {
    @Id
    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "account_id", nullable = false, unique = true)
    private String accountId;

    @Column(name = "account_type", nullable = false)
    private String accountType;

    @Column(name = "initial_balance", nullable = false)
    private BigDecimal initialBalance;

    @Column(name = "status", nullable = false)
    private Boolean status;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    // Getters and Setters
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public BigDecimal getInitialBalance() { return initialBalance; }
    public void setInitialBalance(BigDecimal initialBalance) { this.initialBalance = initialBalance; }
    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }
    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    @PrePersist
    public void generateId() {
        if (this.accountId == null || this.accountId.isBlank()) {
            this.accountId = UUID.randomUUID().toString();
        }
    }
}
