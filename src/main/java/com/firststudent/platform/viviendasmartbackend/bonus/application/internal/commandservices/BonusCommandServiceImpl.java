package com.firststudent.platform.viviendasmartbackend.bonus.application.internal.commandservices;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.firststudent.platform.viviendasmartbackend.bonus.domain.model.aggregates.Bonus;
import com.firststudent.platform.viviendasmartbackend.bonus.domain.services.BonusCommandService;
import com.firststudent.platform.viviendasmartbackend.bonus.infrastructure.persistence.jpa.repositories.BonusRepository;

@Service
public class BonusCommandServiceImpl implements BonusCommandService {

    private final BonusRepository bonusRepository;

    public BonusCommandServiceImpl(BonusRepository bonusRepository) {
        this.bonusRepository = bonusRepository;
    }

    @Override
    @Transactional
    public Bonus create(String name, BigDecimal amount, String requirements) {
        Bonus bonus = new Bonus(name, amount, requirements);
        return bonusRepository.save(bonus);
    }

    @Override
    @Transactional
    public Optional<Bonus> update(Long bonusId, String name, BigDecimal amount, String requirements) {
        return bonusRepository.findById(bonusId).map(existing -> {
            existing.updateDetails(name, amount, requirements);
            return bonusRepository.save(existing);
        });
    }

    @Override
    @Transactional
    public void delete(Long bonusId) {
        bonusRepository.deleteById(bonusId);
    }
}

