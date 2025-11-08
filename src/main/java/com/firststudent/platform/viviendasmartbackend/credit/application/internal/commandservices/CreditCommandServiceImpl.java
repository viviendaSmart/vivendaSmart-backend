package com.firststudent.platform.viviendasmartbackend.credit.application.internal.commandservices;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.firststudent.platform.viviendasmartbackend.credit.domain.model.aggregates.Credit;
import com.firststudent.platform.viviendasmartbackend.credit.domain.model.valueobjects.GraceType;
import com.firststudent.platform.viviendasmartbackend.credit.domain.model.valueobjects.RateType;
import com.firststudent.platform.viviendasmartbackend.credit.domain.services.CreditCommandService;
import com.firststudent.platform.viviendasmartbackend.credit.infrastructure.persistence.jpa.repositories.CreditRepository;

@Service
public class CreditCommandServiceImpl implements CreditCommandService {

    private final CreditRepository creditRepository;

    public CreditCommandServiceImpl(CreditRepository creditRepository) {
        this.creditRepository = creditRepository;
    }

    @Override
    @Transactional
    public Credit create(BigDecimal fundedAmount, String currency, RateType rateType,
                         BigDecimal annualRate, BigDecimal monthlyRate, Integer termMonths,
                         GraceType graceType, Integer graceMonths, Long bonusId,
                         BigDecimal fixedInstallment, BigDecimal totalInterest,
                         BigDecimal totalAmountPaid, BigDecimal VAN, BigDecimal TIR) {
        Credit credit = new Credit(fundedAmount, currency, rateType, annualRate, monthlyRate,
                                  termMonths, graceType, graceMonths, bonusId,
                                  fixedInstallment, totalInterest, totalAmountPaid, VAN, TIR);
        return creditRepository.save(credit);
    }

    @Override
    @Transactional
    public Optional<Credit> updateCalculations(Long creditId, BigDecimal fixedInstallment,
                                               BigDecimal totalInterest, BigDecimal totalAmountPaid,
                                               BigDecimal VAN, BigDecimal TIR) {
        return creditRepository.findById(creditId).map(existing -> {
            existing.updateCalculations(fixedInstallment, totalInterest, totalAmountPaid, VAN, TIR);
            return creditRepository.save(existing);
        });
    }

    @Override
    @Transactional
    public void delete(Long creditId) {
        creditRepository.deleteById(creditId);
    }
}

