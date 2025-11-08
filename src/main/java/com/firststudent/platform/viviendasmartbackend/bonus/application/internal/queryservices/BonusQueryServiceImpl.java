package com.firststudent.platform.viviendasmartbackend.bonus.application.internal.queryservices;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.firststudent.platform.viviendasmartbackend.bonus.domain.model.aggregates.Bonus;
import com.firststudent.platform.viviendasmartbackend.bonus.domain.services.BonusQueryService;
import com.firststudent.platform.viviendasmartbackend.bonus.infrastructure.persistence.jpa.repositories.BonusRepository;

@Service
public class BonusQueryServiceImpl implements BonusQueryService {

    private final BonusRepository bonusRepository;

    public BonusQueryServiceImpl(BonusRepository bonusRepository) {
        this.bonusRepository = bonusRepository;
    }

    @Override
    public List<Bonus> getAll() {
        return bonusRepository.findAll();
    }

    @Override
    public Optional<Bonus> getById(Long id) {
        return bonusRepository.findById(id);
    }
}

