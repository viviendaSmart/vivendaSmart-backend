package com.firststudent.platform.viviendasmartbackend.property.interfaces.rest.resources;

import java.math.BigDecimal;
import java.util.Date;

public record PropertyResource(
    Long id,
    String address,
    BigDecimal price,
    BigDecimal size,
    String photo,
    Long ownerId,
    Date createdAt,
    Date updatedAt
) {}


