package com.firststudent.platform.viviendasmartbackend.credit.domain.services;

import java.util.List;
import java.util.Optional;

import com.firststudent.platform.viviendasmartbackend.credit.domain.model.aggregates.Credit;

public interface CreditQueryService {
    /**
     * Obtiene todos los créditos
     */
    List<Credit> getAll();
    
    /**
     * Obtiene un crédito por su ID
     */
    Optional<Credit> getById(Long id);
    
    /**
     * Obtiene todos los créditos que tienen un bono específico
     */
    List<Credit> getByBonusId(Long bonusId);
}

