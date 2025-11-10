package com.firststudent.platform.viviendasmartbackend.client.domain.services;

import java.math.BigDecimal;
import java.util.Optional;

import com.firststudent.platform.viviendasmartbackend.client.domain.model.aggregates.Client;
import com.firststudent.platform.viviendasmartbackend.client.domain.model.valueobjects.MaritalStatus;

public interface ClientCommandService {
    /**
     * Crea un nuevo cliente
     */
    Client create(String dni, BigDecimal monthlyIncome, String ocupation, String name, String surname, String address,
                  String business, String earningtype, Boolean credithistory, Boolean support, MaritalStatus maritalStatus, String phoneNumber, Long userId);
    
    /**
     * Actualiza un cliente existente
     */
    Optional<Client> update(Long clientId, BigDecimal monthlyIncome, String ocupation, String name, String surname,
                            String address, String business, String earningtype, Boolean credithistory, Boolean support, MaritalStatus maritalStatus, String phoneNumber);
    
    /**
     * Elimina un cliente
     */
    void delete(Long clientId);
}

