package com.firststudent.platform.viviendasmartbackend.cost.domain.services;


import com.firststudent.platform.viviendasmartbackend.cost.domain.model.aggregates.Cost;
import com.firststudent.platform.viviendasmartbackend.cost.domain.model.valueobjects.CostType;

import java.math.BigDecimal;
import java.util.Optional;

public interface CostCommandService {

    Cost create(CostType costType, BigDecimal amount, Integer periodNumber, Long creditId);


    Optional<Cost> update(Long costId, CostType costType, BigDecimal amount, Integer periodNumber);

    void delete(Long costId);
}
