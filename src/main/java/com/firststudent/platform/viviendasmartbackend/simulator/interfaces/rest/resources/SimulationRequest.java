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
    private Long bonusId; // opcional: puede venir null si no se elige bono

    // Parámetros del crédito (derivados de config + lo que ajuste el usuario)
    private BigDecimal initialPayment;   // cuota inicial en %
    private Integer termMonths;          // plazo de pago en meses
    private BigDecimal rate;             // valor de la tasa
    private String rateType;             // "TEA", "TNA", etc.
    private String exchange;             // "PEN", "USD", etc.
    private String graceType;            // Tipo de periodo de gracia
    private String term;                 // Días de gracia
    private String bonusType;               // "AVN", "CSP", "MV" o null si no aplica

    // Lista de costos de esta simulación
    private List<CostItem> costs;

    @Getter
    @Setter
    public static class CostItem {
        private CostType type;        // INITIAL o PERIODIC
        private BigDecimal amount;    // monto del costo
        private Integer periodNumber; // null si aplica a todos los periodos (para PERIODIC)
    }
}
