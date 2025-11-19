package com.firststudent.platform.viviendasmartbackend.cost.interfaces.rest.resources;

import com.firststudent.platform.viviendasmartbackend.cost.domain.model.valueobjects.CostType;

import java.math.BigDecimal;
import java.util.Date;

public record CostResource (
        Long id,
        CostType costType,
        BigDecimal amount,
        Integer periodNumber,
        Long creditId,
        Date createdAt,
        Date updatedAt
){}
