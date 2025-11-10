package com.firststudent.platform.viviendasmartbackend.client.application.internal.queryservices;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.firststudent.platform.viviendasmartbackend.client.domain.model.aggregates.Client;
import com.firststudent.platform.viviendasmartbackend.client.domain.services.ClientQueryService;
import com.firststudent.platform.viviendasmartbackend.client.infrastructure.persistence.jpa.repositories.ClientRepository;

@Service
public class ClientQueryServiceImpl implements ClientQueryService {

    private final ClientRepository clientRepository;

    public ClientQueryServiceImpl(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public List<Client> getAll() {
        return clientRepository.findAll();
    }

    @Override
    public Optional<Client> getById(Long id) {
        return clientRepository.findById(id);
    }

    @Override
    public Optional<Client> getByDni(String dni) {
        return clientRepository.findByDni(dni);
    }

    @Override
    public List<Client> getByUserId(Long userId) {
        return clientRepository.findByUserId(userId);
    }
}

