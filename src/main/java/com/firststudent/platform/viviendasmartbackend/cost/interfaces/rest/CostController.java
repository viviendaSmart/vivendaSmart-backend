package com.firststudent.platform.viviendasmartbackend.cost.interfaces.rest;



import com.firststudent.platform.viviendasmartbackend.cost.domain.model.aggregates.Cost;
import com.firststudent.platform.viviendasmartbackend.cost.domain.services.CostCommandService;
import com.firststudent.platform.viviendasmartbackend.cost.domain.services.CostQueryService;
import com.firststudent.platform.viviendasmartbackend.cost.interfaces.rest.resources.CostResource;
import com.firststudent.platform.viviendasmartbackend.cost.interfaces.rest.resources.CreateCostResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.firststudent.platform.viviendasmartbackend.cost.interfaces.rest.transform.CostResourceFromEntityAssembler.toResourceFromEntity;


@RestController
@RequestMapping("/api/v1/costs")
public class CostController {
    private final CostCommandService commandService;
    private final CostQueryService queryService;
    public CostController(CostCommandService commandService, CostQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping
    public ResponseEntity<CostResource> create(@RequestBody CreateCostResource resource) {
        Cost created = commandService.create(resource.costType(), resource.amount(), resource.periodNumber(), resource.creditId());
        return ResponseEntity.ok(toResourceFromEntity(created));
    }

    @GetMapping
    public ResponseEntity<List<CostResource>> getAll() {
        List<Cost> costs = queryService.getAll();
        return ResponseEntity.ok(costs.stream().map(cost -> toResourceFromEntity(cost)).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CostResource> getById(@PathVariable Long id) {
        return queryService.getById(id)
                .map(entity -> ResponseEntity.ok(toResourceFromEntity(entity)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CostResource> update(@PathVariable Long id, @RequestBody CreateCostResource resource) {
        return commandService.update(id, resource.costType(), resource.amount(), resource.periodNumber())
                .map(entity -> ResponseEntity.ok(toResourceFromEntity(entity)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        commandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
