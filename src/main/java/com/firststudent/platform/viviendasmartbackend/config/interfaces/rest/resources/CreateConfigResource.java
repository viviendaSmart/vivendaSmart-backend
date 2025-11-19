package com.firststudent.platform.viviendasmartbackend.config.interfaces.rest.resources;

import com.firststudent.platform.viviendasmartbackend.config.domain.model.valueobjects.Exchange;
import com.firststudent.platform.viviendasmartbackend.config.domain.model.valueobjects.RateType;
import com.firststudent.platform.viviendasmartbackend.config.domain.model.valueobjects.GraceType;

import java.math.BigDecimal;

public record CreateConfigResource(
        BigDecimal rate,
        RateType rateType,
        Exchange exchange,
        GraceType termtype,
        Integer term,
        Long userId
) {
}
