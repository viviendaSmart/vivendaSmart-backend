package com.firststudent.platform.viviendasmartbackend.client.interfaces.rest.transform;

import com.firststudent.platform.viviendasmartbackend.client.domain.model.aggregates.Client;
import com.firststudent.platform.viviendasmartbackend.client.interfaces.rest.resources.ClientResource;

/**
 * Assembler para convertir entidades Client a recursos REST
 */
public class ClientResourceFromEntityAssembler {
    public static ClientResource toResourceFromEntity(Client entity) {
        return new ClientResource(
            entity.getId(),
            entity.getDni(),
            entity.getMonthlyIncome(),
            entity.getOcupation(),
            entity.getName(),
            entity.getSurname(),
            entity.getAddress(),
            entity.getBusiness(),
            entity.getEarningtype(),
            entity.getCredithistory(),
            entity.getSupport(),
            entity.getMaritalStatus(),
            entity.getPhoneNumber(),
            entity.getUserId(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}

