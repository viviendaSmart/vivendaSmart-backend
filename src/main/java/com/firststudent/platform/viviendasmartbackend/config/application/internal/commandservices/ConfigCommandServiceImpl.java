package com.firststudent.platform.viviendasmartbackend.config.application.internal.commandservices;

import com.firststudent.platform.viviendasmartbackend.config.domain.model.aggregates.Config;
import com.firststudent.platform.viviendasmartbackend.config.domain.model.valueobjects.Exchange;
import com.firststudent.platform.viviendasmartbackend.config.domain.model.valueobjects.RateType;
import com.firststudent.platform.viviendasmartbackend.config.domain.model.valueobjects.TermType;
import com.firststudent.platform.viviendasmartbackend.config.domain.services.ConfigCommandService;
import com.firststudent.platform.viviendasmartbackend.config.infrastructure.persistence.jpa.repositories.ConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class ConfigCommandServiceImpl implements ConfigCommandService {
    private final ConfigRepository configRepository;

    public ConfigCommandServiceImpl(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    @Override
    @Transactional
    public Config create(BigDecimal rate, RateType rateType, Exchange exchange, TermType termtype, Integer term, Long userId){
        Config config = new Config(rate, rateType, exchange, termtype, term, userId);
        return configRepository.save(config);
    }
    @Override
    @Transactional
    public Optional<Config> update(Long configId, BigDecimal rate, RateType rateType, Exchange exchange, TermType termtype, Integer term) {
        return configRepository.findById(configId).map(existing -> {
            existing.updateDetails(rate, rateType, exchange, termtype, term);
            return configRepository.save(existing);
        });
    }

    @Override
    @Transactional
    public void delete(Long configId) {
        configRepository.deleteById(configId);
    }
}