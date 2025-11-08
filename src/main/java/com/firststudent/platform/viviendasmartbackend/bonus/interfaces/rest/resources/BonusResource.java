package com.firststudent.platform.viviendasmartbackend.bonus.interfaces.rest.resources;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Recurso para información del bono en respuestas REST
 */
public record BonusResource(
    Long id,
    String name,
    BigDecimal amount,
    String requirements,
    Date createdAt,
    Date updatedAt
) {}

