package com.firststudent.platform.viviendasmartbackend.property.domain.model.aggregates;

import java.math.BigDecimal;

import com.firststudent.platform.viviendasmartbackend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
@Entity
public class Property extends AuditableAbstractAggregateRoot<Property> {

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String address;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @NotNull
    @Column(nullable = false)
    private BigDecimal size;

    @NotBlank
    @Size(max = 500)
    @Column(nullable = false, length = 50)
    private String photo;


    // We reference the owner by id to keep the aggregate boundary between IAM and Property contexts
    @NotNull
    @Column(nullable = false)
    private Long ownerId;

    protected Property() {
    }

    public Property(String address, BigDecimal price, BigDecimal size, String photo, Long ownerId) {
        this.address = address;
        this.price = price;
        this.size = size;
        this.photo = photo;
        this.ownerId = ownerId;
    }

    public void updateDetails(String address, BigDecimal price, BigDecimal size, String photo) {
        if (address != null && !address.isBlank()) {
            this.address = address;
        }
        if (price != null) {
            this.price = price;
        }
        if (size != null) {
            this.size = size;
        }
        if (photo != null && !photo.isBlank()) {
            this.photo = photo;
        }
    }
}
