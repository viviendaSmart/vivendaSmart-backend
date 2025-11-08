package com.firststudent.platform.viviendasmartbackend.credit.infrastructure.persistence.jpa.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.firststudent.platform.viviendasmartbackend.credit.domain.model.aggregates.Credit;

public interface CreditRepository extends JpaRepository<Credit, Long> {
    List<Credit> findByBonusId(Long bonusId);
}

