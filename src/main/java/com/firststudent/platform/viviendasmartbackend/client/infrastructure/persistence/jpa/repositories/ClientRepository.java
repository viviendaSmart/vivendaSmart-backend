package com.firststudent.platform.viviendasmartbackend.client.infrastructure.persistence.jpa.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.firststudent.platform.viviendasmartbackend.client.domain.model.aggregates.Client;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByDniAndDeletedFalse(String dni);
    List<Client> findByUserIdAndDeletedFalse(Long userId);
    List<Client> findAllByDeletedFalse();
    Optional<Client> findByIdAndDeletedFalse(Long id);
}

