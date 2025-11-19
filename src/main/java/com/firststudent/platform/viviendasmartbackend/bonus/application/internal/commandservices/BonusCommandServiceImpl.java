package com.firststudent.platform.viviendasmartbackend.bonus.application.internal.commandservices;

import java.math.BigDecimal;
import java.util.Optional;

import com.firststudent.platform.viviendasmartbackend.bonus.domain.model.valueobjects.BonusType;
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
    public Bonus create(BonusType bonusType, BigDecimal amount, Long creditId) {
        Bonus bonus = new Bonus(bonusType, amount, creditId);
        return bonusRepository.save(bonus);
    }

    @Override
    @Transactional
    public Optional<Bonus> update(Long bonusId, BonusType bonusType, BigDecimal amount) {
        return bonusRepository.findById(bonusId).map(existing -> {
            existing.updateDetails(bonusType, amount);
            return bonusRepository.save(existing);
        });
    }

    @Override
    @Transactional
    public void delete(Long bonusId) {
        bonusRepository.deleteById(bonusId);
    }
}

