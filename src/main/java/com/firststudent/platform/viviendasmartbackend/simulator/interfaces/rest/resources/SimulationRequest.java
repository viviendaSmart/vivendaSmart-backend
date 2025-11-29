package com.firststudent.platform.viviendasmartbackend.simulator.interfaces.rest.resources;

import com.firststudent.platform.viviendasmartbackend.cost.domain.model.valueobjects.CostType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class SimulationRequest {

    private Long clientId;
    private Long propertyId;
    private Long userId;

    // Parámetros variables del crédito
    private BigDecimal initialPayment;   // % inicial
    private Integer termYears;           // años de plazo
    private Integer frequency;          // días (30, 60, 90)

    // COK (no está en Config, así que lo dejas aquí)
    private BigDecimal cokRate;
    private String cokRateType;

    private String bonusType;           // AVN/CSP/MV

    private List<CostItem> costs;

    public enum CostCalcMode { FIXED_AMOUNT, PERCENTAGE }

    @Getter @Setter
    public static class CostItem {
        private CostType type;
        private String code;
        private CostCalcMode calcMode;
        private BigDecimal amount;
        private Integer periodNumber;
    }
}

