package com.firststudent.platform.viviendasmartbackend.config.interfaces.rest;

import com.firststudent.platform.viviendasmartbackend.config.domain.model.aggregates.Config;
import com.firststudent.platform.viviendasmartbackend.config.domain.services.ConfigCommandService;
import com.firststudent.platform.viviendasmartbackend.config.domain.services.ConfigQueryService;
import com.firststudent.platform.viviendasmartbackend.config.interfaces.rest.resources.ConfigResource;
import com.firststudent.platform.viviendasmartbackend.config.interfaces.rest.resources.CreateConfigResource;
import com.firststudent.platform.viviendasmartbackend.config.interfaces.rest.transform.ConfigResourceFromEntityAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.firststudent.platform.viviendasmartbackend.config.interfaces.rest.transform.ConfigResourceFromEntityAssembler.toResourceFromEntity;


@RestController
@RequestMapping("/api/v1/config")
public class ConfigController {
    private final ConfigCommandService commandService;
    private final ConfigQueryService queryService;
    public ConfigController(ConfigCommandService commandService, ConfigQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping
    public ResponseEntity<ConfigResource> create(@RequestBody CreateConfigResource resource) {
        Config created = commandService.create(resource.rate(), resource.rateType(), resource.exchange(), resource.termtype(), resource.term(), resource.userId());
        return ResponseEntity.ok(toResourceFromEntity(created));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ConfigResource> getById(@PathVariable Long userId) {
        return queryService.getByUserId(userId)
                .map(entity -> ResponseEntity.ok(toResourceFromEntity(entity)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConfigResource> update(@PathVariable Long id, @RequestBody CreateConfigResource resource) {
        return commandService.update(id, resource.rate(), resource.rateType(), resource.exchange(), resource.termtype(), resource.term())
                .map(entity -> ResponseEntity.ok(ConfigResourceFromEntityAssembler.toResourceFromEntity(entity)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        commandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
