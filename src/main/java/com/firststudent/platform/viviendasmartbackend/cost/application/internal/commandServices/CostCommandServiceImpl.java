package com.firststudent.platform.viviendasmartbackend.cost.application.internal.commandServices;

import com.firststudent.platform.viviendasmartbackend.bonus.domain.model.aggregates.Bonus;
import com.firststudent.platform.viviendasmartbackend.bonus.domain.model.valueobjects.BonusType;
import com.firststudent.platform.viviendasmartbackend.bonus.infrastructure.persistence.jpa.repositories.BonusRepository;
import com.firststudent.platform.viviendasmartbackend.cost.domain.model.aggregates.Cost;
import com.firststudent.platform.viviendasmartbackend.cost.domain.model.valueobjects.CostType;
import com.firststudent.platform.viviendasmartbackend.cost.domain.services.CostCommandService;
import com.firststudent.platform.viviendasmartbackend.cost.infrastructure.persistence.jpa.repositories.CostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class CostCommandServiceImpl implements CostCommandService {
    private final CostRepository costRepository;

    public CostCommandServiceImpl(CostRepository costRepository) {
        this.costRepository = costRepository;
    }

    @Override
    @Transactional
    public Cost create(CostType costType, BigDecimal amount, Integer periodNumber,Long creditId) {
        Cost cost = new Cost(costType, amount, periodNumber, creditId);
        return costRepository.save(cost);
    }

    @Override
    @Transactional
    public Optional<Cost> update(Long costId, CostType costType, BigDecimal amount, Integer periodNumber) {
        return costRepository.findById(costId).map(existing -> {
            existing.updateDetails(costType, amount, periodNumber);
            return costRepository.save(existing);
        });
    }

    @Override
    @Transactional
    public void delete(Long costId) {
        costRepository.deleteById(costId);
    }
}
