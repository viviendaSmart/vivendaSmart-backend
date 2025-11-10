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
    String ocupation,
    String name,
    String surname,
    String address,
    String business,
    String earningtype,
    Boolean credithistory,
    Boolean support,
    MaritalStatus maritalStatus,
    String phoneNumber,
    Long userId,
    Date createdAt,
    Date updatedAt
) {}

