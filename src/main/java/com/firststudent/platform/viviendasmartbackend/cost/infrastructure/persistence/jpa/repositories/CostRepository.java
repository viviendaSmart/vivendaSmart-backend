package com.firststudent.platform.viviendasmartbackend.cost.infrastructure.persistence.jpa.repositories;

import com.firststudent.platform.viviendasmartbackend.bonus.domain.model.aggregates.Bonus;
import com.firststudent.platform.viviendasmartbackend.cost.domain.model.aggregates.Cost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CostRepository extends JpaRepository<Cost, Long> {
}
