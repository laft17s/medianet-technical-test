package com.laft.account.application.service;

import com.laft.account.application.port.in.AccountUseCase;
import com.laft.account.application.port.out.AccountRepositoryPort;
import com.laft.account.domain.AccountDomain;
import com.laft.commons.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AccountService implements AccountUseCase {
    private final AccountRepositoryPort accountRepositoryPort;

    public AccountService(AccountRepositoryPort accountRepositoryPort) {
        this.accountRepositoryPort = accountRepositoryPort;
    }

    @Override
    public AccountDomain createAccount(AccountDomain account) {
        if (account.getAccountId() == null || account.getAccountId().isBlank()) {
            account.setAccountId(UUID.randomUUID().toString());
        }
        return accountRepositoryPort.save(account);
    }

    @Override
    public AccountDomain getAccount(String accountNumber) {
        return accountRepositoryPort.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));
    }

    @Override
    public AccountDomain getAccountById(String accountId) {
        return accountRepositoryPort.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found by ID: " + accountId));
    }

    @Override
    public AccountDomain updateAccount(AccountDomain account) {
        AccountDomain existing = getAccount(account.getAccountNumber());
        existing.setAccountType(account.getAccountType());
        existing.setStatus(account.getStatus());
        return accountRepositoryPort.save(existing);
    }

    @Override
    public void deleteAccount(String accountNumber) {
        AccountDomain existing = getAccount(accountNumber);
        existing.setStatus(false);
        accountRepositoryPort.save(existing);
    }

    @Override
    public List<AccountDomain> getAllAccounts() {
        return accountRepositoryPort.findAll();
    }

    @Override
    public List<AccountDomain> getAccountsByClient(Long clientId) {
        return accountRepositoryPort.findByClientId(clientId);
    }
}
