package com.firststudent.platform.viviendasmartbackend.client.domain.model.aggregates;

import java.math.BigDecimal;

import com.firststudent.platform.viviendasmartbackend.client.domain.model.valueobjects.MaritalStatus;
import com.firststudent.platform.viviendasmartbackend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

/**
 * Agregado Cliente
 * Representa la información del cliente en el sistema.
 * Mantiene una referencia por ID al User (IAM) para mantener la separación de bounded contexts.
 */
@Getter
@Entity
public class Client extends AuditableAbstractAggregateRoot<Client> {

    @NotBlank
    @Size(min = 8, max = 8)
    @Column(nullable = false, unique = true, length = 8)
    private String dni;

    @NotNull
    @Column(nullable = false)
    private BigDecimal monthlyIncome;

    @NotNull
    @Column(nullable = false)
    private String ocupation;

    @NotNull
    @Column(nullable = false)
    private String name;

    @NotNull
    @Column(nullable = false)
    private String surname;

    @NotNull
    @Column(nullable = false)
    private String business;

    @NotNull
    @Column(nullable = false)
    private String earningtype;

    @NotNull
    @Column(nullable = false)
    private Boolean credithistory;


    @NotNull
    @Column(nullable = false)
    private Boolean support;


    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String address;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MaritalStatus maritalStatus;

    @NotBlank
    @Column(nullable = false, length = 15)
    private String phoneNumber;

    // Referencia al User por ID para mantener la separación de bounded contexts
    @NotNull
    @Column(nullable = false)
    private Long userId;

    protected Client() {
    }

    /**
     * Constructor para crear un nuevo cliente
     * @param dni el DNI del cliente (8 caracteres)
     * @param monthlyIncome el ingreso mensual
     * @param ocupation su ocupacion
     * @param address la dirección
     * @param maritalStatus el estado civil
     * @param phoneNumber el número de teléfono
     * @param userId el ID del usuario asociado (del bounded context IAM)
     */
    public Client(String dni, BigDecimal monthlyIncome, String ocupation, String name, String surname, String address,
                  String business, String earningtype, Boolean credithistory, Boolean support ,MaritalStatus maritalStatus, String phoneNumber, Long userId) {
        this.dni = dni;
        this.monthlyIncome = monthlyIncome;
        this.ocupation = ocupation;
        this.name = name;
        this.surname = surname;
        this.address = address;
        this.business = business;
        this.earningtype = earningtype;
        this.credithistory = credithistory;
        this.support = support;
        this.maritalStatus = maritalStatus;
        this.phoneNumber = phoneNumber;
        this.userId = userId;
    }

    /**
     * Actualiza los detalles del cliente
     */
    public void updateDetails(BigDecimal monthlyIncome, String ocupation, String name, String surname,
                              String address, String business, String earningtype, Boolean credithistory, Boolean support,
                              MaritalStatus maritalStatus, String phoneNumber) {
        if (monthlyIncome != null && monthlyIncome.compareTo(BigDecimal.ZERO) > 0) {
            this.monthlyIncome = monthlyIncome;
        }
        if (ocupation != null) {
            this.ocupation = ocupation;
        }
        if (name != null) {
            this.name = name;
        }
        if (surname != null) {
            this.surname = surname;
        }
        if (address != null && !address.isBlank()) {
            this.address = address;
        }
        if (business != null) {
            this.business = business;
        }
        if (maritalStatus != null) {
            this.maritalStatus = maritalStatus;
        }
        if (earningtype != null) {
            this.earningtype = earningtype;
        }
        if (credithistory != null) {
            this.credithistory = credithistory;
        }
        if (support != null) {
            this.support = support;
        }
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            this.phoneNumber = phoneNumber;
        }
    }
}

