package com.firststudent.platform.viviendasmartbackend.config.interfaces.rest.resources;

import com.firststudent.platform.viviendasmartbackend.config.domain.model.valueobjects.Exchange;
import com.firststudent.platform.viviendasmartbackend.config.domain.model.valueobjects.RateType;
import com.firststudent.platform.viviendasmartbackend.config.domain.model.valueobjects.TermType;

import java.math.BigDecimal;
import java.util.Date;

public record ConfigResource(
        Long id,
        BigDecimal rate,
        RateType rateType,
        Exchange exchange,
        TermType termtype,
        Integer term,
        Long userId,
        Date createdAt,
        Date updatedAt
) {
}
