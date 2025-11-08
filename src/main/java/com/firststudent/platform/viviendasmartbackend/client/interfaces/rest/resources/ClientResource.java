package com.firststudent.platform.viviendasmartbackend.client.interfaces.rest.resources;

import java.math.BigDecimal;
import java.util.Date;

import com.firststudent.platform.viviendasmartbackend.client.domain.model.valueobjects.MaritalStatus;

/**
 * Recurso para información del cliente en respuestas REST
 */
public record ClientResource(
    Long id,
    String dni,
    BigDecimal monthlyIncome,
    String address,
    MaritalStatus maritalStatus,
    String phoneNumber,
    Long userId,
    Date createdAt,
    Date updatedAt
) {}

