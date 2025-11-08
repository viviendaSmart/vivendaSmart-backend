package com.firststudent.platform.viviendasmartbackend.bonus.interfaces.rest.resources;

import java.math.BigDecimal;

/**
 * Recurso para crear un nuevo bono
 */
public record CreateBonusResource(
    String name,
    BigDecimal amount,
    String requirements
) {}

