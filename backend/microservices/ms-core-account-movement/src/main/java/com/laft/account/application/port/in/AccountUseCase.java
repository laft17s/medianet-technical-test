package com.laft.account.application.port.in;

import com.laft.account.domain.AccountDomain;
import java.util.List;

public interface AccountUseCase {
    AccountDomain createAccount(AccountDomain account);
    AccountDomain getAccount(String accountNumber);
    AccountDomain getAccountById(String accountId);
    AccountDomain updateAccount(AccountDomain account);
    void deleteAccount(String accountNumber);
    List<AccountDomain> getAccountsByClient(Long clientId);
    List<AccountDomain> getAllAccounts();
}
