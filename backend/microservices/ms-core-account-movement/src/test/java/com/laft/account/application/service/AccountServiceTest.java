package com.laft.account.application.service;

import com.laft.commons.exception.ResourceNotFoundException;
import com.laft.account.application.port.out.AccountRepositoryPort;
import com.laft.account.domain.AccountDomain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepositoryPort accountRepositoryPort;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccount_ShouldReturnSavedAccount() {
        AccountDomain newAccount = new AccountDomain();
        newAccount.setAccountNumber("12345");
        newAccount.setClientId(1L);
        newAccount.setInitialBalance(BigDecimal.valueOf(100));

        AccountDomain savedAccount = new AccountDomain();
        savedAccount.setAccountNumber("12345");
        savedAccount.setClientId(1L);
        savedAccount.setInitialBalance(BigDecimal.valueOf(100));

        when(accountRepositoryPort.save(any(AccountDomain.class))).thenReturn(savedAccount);

        AccountDomain result = accountService.createAccount(newAccount);

        assertNotNull(result);
        assertEquals("12345", result.getAccountNumber());
        verify(accountRepositoryPort, times(1)).save(newAccount);
    }

    @Test
    void createAccount_ShouldGenerateAccountIdWhenMissing() {
        AccountDomain newAccount = new AccountDomain();
        newAccount.setAccountNumber("99999");
        newAccount.setAccountId("");
        newAccount.setClientId(1L);

        when(accountRepositoryPort.save(any(AccountDomain.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccountDomain result = accountService.createAccount(newAccount);

        assertNotNull(result.getAccountId());
        assertFalse(result.getAccountId().isBlank());
    }

    @Test
    void getAccount_WhenExists_ShouldReturnAccount() {
        AccountDomain existingAccount = new AccountDomain();
        existingAccount.setAccountNumber("12345");

        when(accountRepositoryPort.findByAccountNumber("12345")).thenReturn(Optional.of(existingAccount));

        AccountDomain result = accountService.getAccount("12345");

        assertNotNull(result);
        assertEquals("12345", result.getAccountNumber());
        verify(accountRepositoryPort, times(1)).findByAccountNumber("12345");
    }

    @Test
    void getAccount_WhenNotExists_ShouldThrowResourceNotFoundException() {
        when(accountRepositoryPort.findByAccountNumber("99999")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountService.getAccount("99999"));
        verify(accountRepositoryPort, times(1)).findByAccountNumber("99999");
    }

    @Test
    void getAccountsByClient_ShouldReturnAccountList() {
        AccountDomain account1 = new AccountDomain();
        account1.setAccountNumber("111");
        AccountDomain account2 = new AccountDomain();
        account2.setAccountNumber("222");

        when(accountRepositoryPort.findByClientId(1L)).thenReturn(Arrays.asList(account1, account2));

        List<AccountDomain> result = accountService.getAccountsByClient(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(accountRepositoryPort, times(1)).findByClientId(1L);
    }

    @Test
    void deleteAccount_WhenExists_ShouldCallDelete() {
        AccountDomain existingAccount = new AccountDomain();
        existingAccount.setAccountNumber("12345");
        existingAccount.setStatus(true);

        when(accountRepositoryPort.findByAccountNumber("12345")).thenReturn(Optional.of(existingAccount));
        when(accountRepositoryPort.save(any(AccountDomain.class))).thenReturn(existingAccount);

        accountService.deleteAccount("12345");

        verify(accountRepositoryPort, times(1)).findByAccountNumber("12345");
        verify(accountRepositoryPort, times(1)).save(existingAccount);
        assertFalse(existingAccount.getStatus());
    }
}
