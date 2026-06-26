package com.laft.account.repository.dao;

import com.laft.account.repository.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    List<Account> findByClientId(Long clientId);
    Optional<Account> findByAccountId(String accountId);
}
