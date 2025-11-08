package com.firststudent.platform.viviendasmartbackend.property.interfaces.rest.resources;

import java.math.BigDecimal;

public record CreatePropertyResource(
    String address,
    BigDecimal price,
    BigDecimal size,
    String photo,
    Long ownerId
) {}


