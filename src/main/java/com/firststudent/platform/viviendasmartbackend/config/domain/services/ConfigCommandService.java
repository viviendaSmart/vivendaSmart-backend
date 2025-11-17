package com.firststudent.platform.viviendasmartbackend.config.domain.services;

import com.firststudent.platform.viviendasmartbackend.config.domain.model.aggregates.Config;
import com.firststudent.platform.viviendasmartbackend.config.domain.model.valueobjects.Exchange;
import com.firststudent.platform.viviendasmartbackend.config.domain.model.valueobjects.RateType;
import com.firststudent.platform.viviendasmartbackend.config.domain.model.valueobjects.TermType;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

public interface ConfigCommandService {
    Config create(BigDecimal rate, RateType rateType, Exchange exchange, TermType termtype, Integer term, Long userId);

    Optional<Config> update(Long configId, BigDecimal rate, RateType rateType, Exchange exchange, TermType termtype, Integer term);

    void delete(Long configId);
}
