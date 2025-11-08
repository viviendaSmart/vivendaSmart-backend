package com.firststudent.platform.viviendasmartbackend.credit.interfaces.rest;

import static com.firststudent.platform.viviendasmartbackend.credit.interfaces.rest.transform.CreditResourceFromEntityAssembler.toResourceFromEntity;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.firststudent.platform.viviendasmartbackend.credit.domain.model.aggregates.Credit;
import com.firststudent.platform.viviendasmartbackend.credit.domain.services.CreditCommandService;
import com.firststudent.platform.viviendasmartbackend.credit.domain.services.CreditQueryService;
import com.firststudent.platform.viviendasmartbackend.credit.interfaces.rest.resources.CreateCreditResource;
import com.firststudent.platform.viviendasmartbackend.credit.interfaces.rest.resources.CreditResource;

/**
 * Controlador REST para operaciones con Créditos
 */
@RestController
@RequestMapping("/api/v1/credits")
public class CreditController {

    private final CreditCommandService commandService;
    private final CreditQueryService queryService;

    public CreditController(CreditCommandService commandService, CreditQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping
    public ResponseEntity<CreditResource> create(@RequestBody CreateCreditResource resource) {
        Credit created = commandService.create(
            resource.fundedAmount(),
            resource.currency(),
            resource.rateType(),
            resource.annualRate(),
            resource.monthlyRate(),
            resource.termMonths(),
            resource.graceType(),
            resource.graceMonths(),
            resource.bonusId(),
            resource.fixedInstallment(),
            resource.totalInterest(),
            resource.totalAmountPaid(),
            resource.VAN(),
            resource.TIR()
        );
        return ResponseEntity.ok(toResourceFromEntity(created));
    }

    @GetMapping
    public ResponseEntity<List<CreditResource>> getAll(@RequestParam(value = "bonusId", required = false) Long bonusId) {
        List<Credit> credits = bonusId == null ? queryService.getAll() : queryService.getByBonusId(bonusId);
        return ResponseEntity.ok(credits.stream().map(credit -> toResourceFromEntity(credit)).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreditResource> getById(@PathVariable Long id) {
        return queryService.getById(id)
                .map(entity -> ResponseEntity.ok(toResourceFromEntity(entity)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/calculations")
    public ResponseEntity<CreditResource> updateCalculations(@PathVariable Long id, @RequestBody CreateCreditResource resource) {
        return commandService.updateCalculations(id, resource.fixedInstallment(), resource.totalInterest(),
                                                 resource.totalAmountPaid(), resource.VAN(), resource.TIR())
                .map(entity -> ResponseEntity.ok(toResourceFromEntity(entity)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        commandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

