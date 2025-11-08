package com.firststudent.platform.viviendasmartbackend.property.interfaces.rest;

import static com.firststudent.platform.viviendasmartbackend.property.interfaces.rest.transform.PropertyResourceFromEntityAssembler.toResourceFromEntity;

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

import com.firststudent.platform.viviendasmartbackend.property.domain.model.aggregates.Property;
import com.firststudent.platform.viviendasmartbackend.property.domain.services.PropertyCommandService;
import com.firststudent.platform.viviendasmartbackend.property.domain.services.PropertyQueryService;
import com.firststudent.platform.viviendasmartbackend.property.interfaces.rest.resources.CreatePropertyResource;
import com.firststudent.platform.viviendasmartbackend.property.interfaces.rest.resources.PropertyResource;

@RestController
@RequestMapping("/api/v1/properties")
public class PropertyController {

    private final PropertyCommandService commandService;
    private final PropertyQueryService queryService;

    public PropertyController(PropertyCommandService commandService, PropertyQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping
    public ResponseEntity<PropertyResource> create(@RequestBody CreatePropertyResource resource) {
        Property created = commandService.create(resource.address(), resource.price(), resource.size(), resource.photo(), resource.ownerId());
        return ResponseEntity.ok(toResourceFromEntity(created));
    }

    @GetMapping
    public ResponseEntity<List<PropertyResource>> getAll(@RequestParam(value = "ownerId", required = false) Long ownerId) {
        List<Property> properties = ownerId == null ? queryService.getAll() : queryService.getByOwnerId(ownerId);
        return ResponseEntity.ok(properties.stream().map(prop -> toResourceFromEntity(prop)).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyResource> getById(@PathVariable Long id) {
        return queryService.getById(id)
                .map(entity -> ResponseEntity.ok(toResourceFromEntity(entity)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PropertyResource> update(@PathVariable Long id, @RequestBody CreatePropertyResource resource) {
        return commandService.update(id, resource.address(), resource.price(), resource.size(), resource.photo())
                .map(entity -> ResponseEntity.ok(toResourceFromEntity(entity)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        commandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}


