package com.firststudent.platform.viviendasmartbackend.config.domain.model.aggregates;

import com.firststudent.platform.viviendasmartbackend.config.domain.model.valueobjects.Exchange;
import com.firststudent.platform.viviendasmartbackend.config.domain.model.valueobjects.RateType;
import com.firststudent.platform.viviendasmartbackend.config.domain.model.valueobjects.GraceType;
import com.firststudent.platform.viviendasmartbackend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
public class Config extends AuditableAbstractAggregateRoot<Config> {
    @Column
    private BigDecimal rate;
    @Column
    @Enumerated(EnumType.STRING)
    private RateType rateType;
    @Column
    @Enumerated(EnumType.STRING)
    private Exchange exchange;
    @Column
    @Enumerated(EnumType.STRING)
    private GraceType graceType;
    @Column
    private Integer term;
    @NotNull
    @Column(nullable = false)
    private Long userId;

    protected Config() {}
    public Config(BigDecimal rate, RateType rateType, Exchange exchange, GraceType graceType, Integer term, Long userId) {
        this.rate = rate;
        this.rateType = rateType;
        this.exchange = exchange;
        this.graceType = graceType;
        this.term = term;
        this.userId = userId;
    }
    public void updateDetails(BigDecimal rate, RateType rateType, Exchange exchange, GraceType termtype, Integer term) {
        if(rate.compareTo(BigDecimal.ZERO) > 0) {
            this.rate = rate;
        }
        if (rateType != null) {
            this.rateType = rateType;
        }
        if (exchange != null) {
            this.exchange = exchange;
        }
        if (termtype != null) {
            this.graceType = termtype;
        }
        if (term.compareTo(0) > 0) {
            this.term = term;
        }
    }
}
