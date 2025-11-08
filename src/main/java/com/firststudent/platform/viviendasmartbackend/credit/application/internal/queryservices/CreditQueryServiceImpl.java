package com.firststudent.platform.viviendasmartbackend.credit.application.internal.queryservices;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.firststudent.platform.viviendasmartbackend.credit.domain.model.aggregates.Credit;
import com.firststudent.platform.viviendasmartbackend.credit.domain.services.CreditQueryService;
import com.firststudent.platform.viviendasmartbackend.credit.infrastructure.persistence.jpa.repositories.CreditRepository;

@Service
public class CreditQueryServiceImpl implements CreditQueryService {

    private final CreditRepository creditRepository;

    public CreditQueryServiceImpl(CreditRepository creditRepository) {
        this.creditRepository = creditRepository;
    }

    @Override
    public List<Credit> getAll() {
        return creditRepository.findAll();
    }

    @Override
    public Optional<Credit> getById(Long id) {
        return creditRepository.findById(id);
    }

    @Override
    public List<Credit> getByBonusId(Long bonusId) {
        return creditRepository.findByBonusId(bonusId);
    }
}

