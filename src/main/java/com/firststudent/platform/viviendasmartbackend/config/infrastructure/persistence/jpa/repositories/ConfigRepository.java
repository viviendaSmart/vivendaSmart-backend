package com.firststudent.platform.viviendasmartbackend.config.infrastructure.persistence.jpa.repositories;

import com.firststudent.platform.viviendasmartbackend.config.domain.model.aggregates.Config;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConfigRepository extends JpaRepository<Config, Long> {
    Optional<Config> findByUserId(Long userId);
}
