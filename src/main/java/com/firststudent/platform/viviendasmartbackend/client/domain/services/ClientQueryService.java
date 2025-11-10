package com.firststudent.platform.viviendasmartbackend.client.domain.services;

import java.util.List;
import java.util.Optional;

import com.firststudent.platform.viviendasmartbackend.client.domain.model.aggregates.Client;

public interface ClientQueryService {
    /**
     * Obtiene todos los clientes
     */
    List<Client> getAll();
    
    /**
     * Obtiene un cliente por su ID
     */
    Optional<Client> getById(Long id);
    
    /**
     * Obtiene un cliente por su DNI
     */
    Optional<Client> getByDni(String dni);
    
    /**
     * Obtiene un cliente por el ID del usuario asociado
     */
    List<Client> getByUserId(Long userId);
}

