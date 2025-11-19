package com.firststudent.platform.viviendasmartbackend.cost.application.internal.queryServices;

import com.firststudent.platform.viviendasmartbackend.cost.domain.model.aggregates.Cost;
import com.firststudent.platform.viviendasmartbackend.cost.domain.services.CostQueryService;
import com.firststudent.platform.viviendasmartbackend.cost.infrastructure.persistence.jpa.repositories.CostRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CostQueryServiceImpl implements CostQueryService {
    private final CostRepository costRepository;

    public CostQueryServiceImpl(CostRepository costRepository) {
        this.costRepository = costRepository;
    }

    @Override
    public List<Cost> getAll() {
        return costRepository.findAll();
    }

    @Override
    public Optional<Cost> getById(Long id) {
        return costRepository.findById(id);
    }
}
