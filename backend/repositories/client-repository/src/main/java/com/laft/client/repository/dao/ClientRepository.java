package com.laft.client.repository.dao;

import com.laft.client.repository.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByClientId(Long clientId);
    Optional<Client> findByIdentification(String identification);
    void deleteByClientId(Long clientId);
}
