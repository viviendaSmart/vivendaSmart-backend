package com.firststudent.platform.viviendasmartbackend.simulator.domain.model.aggregates;

import com.firststudent.platform.viviendasmartbackend.simulator.domain.model.valueobjects.CostType;
import com.firststudent.platform.viviendasmartbackend.simulator.domain.model.valueobjects.CostCalcMode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Embeddable
@Getter
@Setter
public class SimulationCostItem {

    @Enumerated(EnumType.STRING)
    private CostType type;         // INITIAL o PERIODIC

    private String code;           // código interno del costo

    @Enumerated(EnumType.STRING)
    private CostCalcMode calcMode; // FIXED_AMOUNT o PERCENTAGE

    private BigDecimal amount;     // monto o porcentaje

    /** null si aplica a todos los periodos (para PERIODIC) */
    private Integer periodNumber;
}
