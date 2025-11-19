package com.firststudent.platform.viviendasmartbackend.bonus.interfaces.rest;

import static com.firststudent.platform.viviendasmartbackend.bonus.interfaces.rest.transform.BonusResourceFromEntityAssembler.toResourceFromEntity;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.firststudent.platform.viviendasmartbackend.bonus.domain.model.aggregates.Bonus;
import com.firststudent.platform.viviendasmartbackend.bonus.domain.services.BonusCommandService;
import com.firststudent.platform.viviendasmartbackend.bonus.domain.services.BonusQueryService;
import com.firststudent.platform.viviendasmartbackend.bonus.interfaces.rest.resources.CreateBonusResource;
import com.firststudent.platform.viviendasmartbackend.bonus.interfaces.rest.resources.BonusResource;

/**
 * Controlador REST para operaciones con Bonos
 */
@RestController
@RequestMapping("/api/v1/bonuses")
public class BonusController {

    private final BonusCommandService commandService;
    private final BonusQueryService queryService;

    public BonusController(BonusCommandService commandService, BonusQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping
    public ResponseEntity<BonusResource> create(@RequestBody CreateBonusResource resource) {
        Bonus created = commandService.create(resource.bonusType(), resource.amount(), resource.creditId());
        return ResponseEntity.ok(toResourceFromEntity(created));
    }

    @GetMapping
    public ResponseEntity<List<BonusResource>> getAll() {
        List<Bonus> bonuses = queryService.getAll();
        return ResponseEntity.ok(bonuses.stream().map(bonus -> toResourceFromEntity(bonus)).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BonusResource> getById(@PathVariable Long id) {
        return queryService.getById(id)
                .map(entity -> ResponseEntity.ok(toResourceFromEntity(entity)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<BonusResource> update(@PathVariable Long id, @RequestBody CreateBonusResource resource) {
        return commandService.update(id, resource.bonusType(), resource.amount())
                .map(entity -> ResponseEntity.ok(toResourceFromEntity(entity)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        commandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

