package com.firststudent.platform.viviendasmartbackend.cost.domain.services;
import com.firststudent.platform.viviendasmartbackend.cost.domain.model.aggregates.Cost;

import java.util.List;
import java.util.Optional;

public interface CostQueryService {
    List<Cost> getAll();
    Optional<Cost> getById(Long id);
}
