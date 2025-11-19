package com.firststudent.platform.viviendasmartbackend.bonus.domain.services;

import java.math.BigDecimal;
import java.util.Optional;

import com.firststudent.platform.viviendasmartbackend.bonus.domain.model.aggregates.Bonus;
import com.firststudent.platform.viviendasmartbackend.bonus.domain.model.valueobjects.BonusType;

public interface BonusCommandService {
    /**
     * Crea un nuevo bono
     */
    Bonus create(BonusType bonusType, BigDecimal amount, Long creditId);
    
    /**
     * Actualiza un bono existente
     */
    Optional<Bonus> update(Long bonusId, BonusType bonusType, BigDecimal amount);
    
    /**
     * Elimina un bono
     */
    void delete(Long bonusId);
}

