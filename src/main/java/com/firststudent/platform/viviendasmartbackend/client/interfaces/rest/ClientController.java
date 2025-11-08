package com.firststudent.platform.viviendasmartbackend.client.interfaces.rest;

import static com.firststudent.platform.viviendasmartbackend.client.interfaces.rest.transform.ClientResourceFromEntityAssembler.toResourceFromEntity;

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

import com.firststudent.platform.viviendasmartbackend.client.domain.model.aggregates.Client;
import com.firststudent.platform.viviendasmartbackend.client.domain.services.ClientCommandService;
import com.firststudent.platform.viviendasmartbackend.client.domain.services.ClientQueryService;
import com.firststudent.platform.viviendasmartbackend.client.interfaces.rest.resources.CreateClientResource;
import com.firststudent.platform.viviendasmartbackend.client.interfaces.rest.resources.ClientResource;

/**
 * Controlador REST para operaciones con Clientes
 */
@RestController
@RequestMapping("/api/v1/clients")
public class ClientController {

    private final ClientCommandService commandService;
    private final ClientQueryService queryService;

    public ClientController(ClientCommandService commandService, ClientQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping
    public ResponseEntity<ClientResource> create(@RequestBody CreateClientResource resource) {
        Client created = commandService.create(
            resource.dni(), 
            resource.monthlyIncome(), 
            resource.address(), 
            resource.maritalStatus(), 
            resource.phoneNumber(), 
            resource.userId()
        );
        return ResponseEntity.ok(toResourceFromEntity(created));
    }

    @GetMapping
    public ResponseEntity<List<ClientResource>> getAll() {
        List<Client> clients = queryService.getAll();
        return ResponseEntity.ok(clients.stream().map(client -> toResourceFromEntity(client)).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResource> getById(@PathVariable Long id) {
        return queryService.getById(id)
                .map(entity -> ResponseEntity.ok(toResourceFromEntity(entity)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-dni")
    public ResponseEntity<ClientResource> getByDni(@RequestParam String dni) {
        return queryService.getByDni(dni)
                .map(entity -> ResponseEntity.ok(toResourceFromEntity(entity)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-user-id")
    public ResponseEntity<ClientResource> getByUserId(@RequestParam Long userId) {
        return queryService.getByUserId(userId)
                .map(entity -> ResponseEntity.ok(toResourceFromEntity(entity)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientResource> update(@PathVariable Long id, @RequestBody CreateClientResource resource) {
        return commandService.update(id, resource.monthlyIncome(), resource.address(), 
                                     resource.maritalStatus(), resource.phoneNumber())
                .map(entity -> ResponseEntity.ok(toResourceFromEntity(entity)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        commandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

