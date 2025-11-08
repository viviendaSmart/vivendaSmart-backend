package com.firststudent.platform.viviendasmartbackend.bonus.domain.services;

import java.math.BigDecimal;
import java.util.Optional;

import com.firststudent.platform.viviendasmartbackend.bonus.domain.model.aggregates.Bonus;

public interface BonusCommandService {
    /**
     * Crea un nuevo bono
     */
    Bonus create(String name, BigDecimal amount, String requirements);
    
    /**
     * Actualiza un bono existente
     */
    Optional<Bonus> update(Long bonusId, String name, BigDecimal amount, String requirements);
    
    /**
     * Elimina un bono
     */
    void delete(Long bonusId);
}

