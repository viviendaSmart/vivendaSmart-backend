package com.firststudent.platform.viviendasmartbackend.bonus.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.firststudent.platform.viviendasmartbackend.bonus.domain.model.aggregates.Bonus;

public interface BonusRepository extends JpaRepository<Bonus, Long> {
}

