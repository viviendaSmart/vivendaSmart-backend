package com.firststudent.platform.viviendasmartbackend.client.interfaces.rest.resources;

import java.math.BigDecimal;

import com.firststudent.platform.viviendasmartbackend.client.domain.model.valueobjects.MaritalStatus;

/**
 * Recurso para crear un nuevo cliente
 */
public record CreateClientResource(
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
    Long userId
) {}

