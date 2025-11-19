package com.firststudent.platform.viviendasmartbackend.cost.interfaces.rest.resources;

import com.firststudent.platform.viviendasmartbackend.bonus.domain.model.valueobjects.BonusType;
import com.firststudent.platform.viviendasmartbackend.cost.domain.model.valueobjects.CostType;

import java.math.BigDecimal;

public record CreateCostResource (
        CostType costType,
        BigDecimal amount,
        Integer periodNumber,
        Long creditId
){}

