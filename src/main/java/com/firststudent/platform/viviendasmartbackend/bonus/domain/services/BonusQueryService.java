package com.firststudent.platform.viviendasmartbackend.bonus.domain.services;

import java.util.List;
import java.util.Optional;

import com.firststudent.platform.viviendasmartbackend.bonus.domain.model.aggregates.Bonus;

public interface BonusQueryService {
    /**
     * Obtiene todos los bonos
     */
    List<Bonus> getAll();
    
    /**
     * Obtiene un bono por su ID
     */
    Optional<Bonus> getById(Long id);
}

