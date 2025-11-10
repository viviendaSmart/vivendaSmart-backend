package com.firststudent.platform.viviendasmartbackend.client.application.internal.commandservices;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.firststudent.platform.viviendasmartbackend.client.domain.model.aggregates.Client;
import com.firststudent.platform.viviendasmartbackend.client.domain.model.valueobjects.MaritalStatus;
import com.firststudent.platform.viviendasmartbackend.client.domain.services.ClientCommandService;
import com.firststudent.platform.viviendasmartbackend.client.infrastructure.persistence.jpa.repositories.ClientRepository;

@Service
public class ClientCommandServiceImpl implements ClientCommandService {

    private final ClientRepository clientRepository;

    public ClientCommandServiceImpl(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    @Transactional
    public Client create(String dni, BigDecimal monthlyIncome, String ocupation, String name, String surname,
                         String address,  String business, String earningtype, Boolean credithistory, Boolean support,
                         MaritalStatus maritalStatus, String phoneNumber, Long userId) {
        Client client = new Client(dni, monthlyIncome, ocupation, name, surname, address, business, earningtype, credithistory, support, maritalStatus, phoneNumber, userId);
        return clientRepository.save(client);
    }

    @Override
    @Transactional
    public Optional<Client> update(Long clientId, BigDecimal monthlyIncome, String ocupation,  String name, String surname, String address,
                                   String business, String earningtype, Boolean credithistory, Boolean support, MaritalStatus maritalStatus, String phoneNumber) {
        return clientRepository.findById(clientId).map(existing -> {
            existing.updateDetails(monthlyIncome, ocupation, name, surname, address, business,earningtype,credithistory,support,maritalStatus, phoneNumber);
            return clientRepository.save(existing);
        });
    }

    @Override
    @Transactional
    public void delete(Long clientId) {
        clientRepository.deleteById(clientId);
    }
}

