package com.firststudent.platform.viviendasmartbackend.simulator.interfaces.rest.resources;

import com.firststudent.platform.viviendasmartbackend.cost.domain.model.valueobjects.CostType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class SimulationRequest {

    // IDs básicos
    private Long clientId;
    private Long propertyId;

    // Parámetros del crédito (derivados de config + lo que ajuste el usuario)
    private BigDecimal initialPayment;   // cuota inicial en %
    private Integer termMonths;          // plazo de pago en meses

    // Tasa del préstamo
    private BigDecimal rate;             // valor de la tasa (ej. 0.095 para 9.5%)
    private String rateType;             // "TEA", "TNA", etc.

    // Tasa de descuento (COK)
    private BigDecimal cokRate;          // valor del COK (ej. 0.05 para 5%)
    private String cokRateType;          // tipo de COK: "TEA", "TNA", "TEM", etc.

    private String exchange;             // "PEN", "USD", etc.
    private String graceType;            // Tipo de periodo de gracia
    private String term;                 // Días de gracia
    private String bonusType;            // "AVN", "CSP", "MV" o null si no aplica

    // Lista de costos de esta simulación
    private List<CostItem> costs;

    public enum CostCalcMode { FIXED_AMOUNT, PERCENTAGE }

    @Getter
    @Setter
    public static class CostItem {
        private CostType type;           // INITIAL o PERIODIC
        private String code;             // "NOTARIAL", "REGISTRAL", "DESGRAVAMEN", etc.
        private CostCalcMode calcMode;   // FIXED_AMOUNT o PERCENTAGE
        private BigDecimal amount;       // soles o decimal (%), según calcMode
        private Integer periodNumber;    // null = todos los periodos (para PERIODIC)
    }
}
