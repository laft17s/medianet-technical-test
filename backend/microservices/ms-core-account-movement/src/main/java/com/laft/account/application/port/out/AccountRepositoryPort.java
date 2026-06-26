package com.laft.account.application.port.out;

import com.laft.account.domain.AccountDomain;
import java.util.List;
import java.util.Optional;

public interface AccountRepositoryPort {
    AccountDomain save(AccountDomain account);
    Optional<AccountDomain> findByAccountNumber(String accountNumber);
    Optional<AccountDomain> findByAccountId(String accountId);
    void delete(String accountNumber);
    List<AccountDomain> findByClientId(Long clientId);
    List<AccountDomain> findAll();
}
