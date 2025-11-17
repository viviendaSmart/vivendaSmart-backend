package com.firststudent.platform.viviendasmartbackend.config.domain.services;

import com.firststudent.platform.viviendasmartbackend.config.domain.model.aggregates.Config;

import java.util.List;
import java.util.Optional;

public interface ConfigQueryService {
    Optional<Config> getByUserId(Long userId);
}
