package com.firststudent.platform.viviendasmartbackend.bonus.interfaces.rest.resources;

import com.firststudent.platform.viviendasmartbackend.bonus.domain.model.valueobjects.BonusType;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Recurso para información del bono en respuestas REST
 */
public record BonusResource(
    Long id,
    BonusType bonusType,
    BigDecimal amount,
    Long creditId,
    Date createdAt,
    Date updatedAt
) {}

