package com.firststudent.platform.viviendasmartbackend.config.application.internal.queryservices;

import com.firststudent.platform.viviendasmartbackend.config.domain.model.aggregates.Config;
import com.firststudent.platform.viviendasmartbackend.config.domain.services.ConfigQueryService;
import com.firststudent.platform.viviendasmartbackend.config.infrastructure.persistence.jpa.repositories.ConfigRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ConfigQueryServiceImpl implements ConfigQueryService {

    private final ConfigRepository configRepository;
    public ConfigQueryServiceImpl(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    @Override
    public Optional<Config> getByUserId(Long userId) { return configRepository.findByUserId(userId);}
}
