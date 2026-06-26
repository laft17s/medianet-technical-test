package com.laft.account.infrastructure.adapter.out.db;

import com.laft.account.application.port.out.AccountRepositoryPort;
import com.laft.account.domain.AccountDomain;
import com.laft.account.infrastructure.adapter.out.db.mapper.AccountMapper;
import com.laft.account.repository.dao.AccountRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class AccountDbAdapter implements AccountRepositoryPort {
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    public AccountDbAdapter(AccountRepository accountRepository, AccountMapper accountMapper) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
    }

    @Override
    @Transactional
    public AccountDomain save(AccountDomain account) {
        return accountMapper.toDomain(accountRepository.save(accountMapper.toEntity(account)));
    }

    private Optional<com.laft.account.repository.entity.Account> findEntityByAccountNumber(String accountNumber) {
        com.laft.account.repository.converter.CryptoConverter crypto = new com.laft.account.repository.converter.CryptoConverter();
        String encryptedId = crypto.convertToDatabaseColumn(accountNumber);
        Optional<com.laft.account.repository.entity.Account> found = accountRepository.findById(encryptedId);
        if (found.isEmpty()) {
            found = accountRepository.findById(accountNumber);
        }
        return found;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AccountDomain> findByAccountNumber(String accountNumber) {
        return findEntityByAccountNumber(accountNumber).map(accountMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AccountDomain> findByAccountId(String accountId) {
        return accountRepository.findByAccountId(accountId).map(accountMapper::toDomain);
    }

    @Override
    @Transactional
    public void delete(String accountNumber) {
        findEntityByAccountNumber(accountNumber)
                .ifPresent(entity -> accountRepository.deleteById(entity.getAccountNumber()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountDomain> findByClientId(Long clientId) {
        return accountRepository.findByClientId(clientId).stream()
                .map(accountMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountDomain> findAll() {
        return accountRepository.findAll().stream()
                .map(accountMapper::toDomain)
                .collect(Collectors.toList());
    }
}
