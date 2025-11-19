package com.firststudent.platform.viviendasmartbackend.bonus.interfaces.rest.resources;

import com.firststudent.platform.viviendasmartbackend.bonus.domain.model.valueobjects.BonusType;

import java.math.BigDecimal;

/**
 * Recurso para crear un nuevo bono
 */
public record CreateBonusResource(
    BonusType bonusType,
    BigDecimal amount,
    Long creditId
) {}

