package com.laft.account.infrastructure.adapter.out.db.mapper;

import com.laft.account.domain.AccountDomain;
import com.laft.account.repository.entity.Account;
import com.laft.account.repository.converter.CryptoConverter;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {
    private final CryptoConverter cryptoConverter = new CryptoConverter();

    public AccountDomain toDomain(Account entity) {
        if (entity == null) return null;
        AccountDomain domain = new AccountDomain();
        domain.setAccountNumber(cryptoConverter.convertToEntityAttribute(entity.getAccountNumber()));
        domain.setAccountId(entity.getAccountId());
        domain.setAccountType(entity.getAccountType());
        domain.setInitialBalance(entity.getInitialBalance());
        domain.setStatus(entity.getStatus());
        domain.setClientId(entity.getClientId());
        return domain;
    }

    public Account toEntity(AccountDomain domain) {
        if (domain == null) return null;
        Account entity = new Account();
        entity.setAccountNumber(cryptoConverter.convertToDatabaseColumn(domain.getAccountNumber()));
        String accountId = domain.getAccountId();
        entity.setAccountId(accountId != null && !accountId.isBlank() ? accountId : null);
        entity.setAccountType(domain.getAccountType());
        entity.setInitialBalance(domain.getInitialBalance());
        entity.setStatus(domain.getStatus());
        entity.setClientId(domain.getClientId());
        return entity;
    }
}
