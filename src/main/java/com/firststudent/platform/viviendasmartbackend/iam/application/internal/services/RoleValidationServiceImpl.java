package com.firststudent.platform.viviendasmartbackend.iam.application.internal.services;

import com.firststudent.platform.viviendasmartbackend.iam.domain.model.valueobjects.Roles;
import com.firststudent.platform.viviendasmartbackend.iam.domain.services.RoleValidationService;
import org.springframework.stereotype.Service;

/**
 * Role Validation Service Implementation
 * <p>
 * This service implements business logic for role validation operations.
 * Currently, all roles can be requested during registration, but this can be
 * extended with more complex business rules in the future.
 * </p>
 */
@Service
public class RoleValidationServiceImpl implements RoleValidationService {

    @Override
    public boolean canRequestRole(Roles role) {
        return role != null;
    }

    @Override
    public Roles[] getAvailableRolesForRegistration() {
        // Return all available roles for registration
        return Roles.values();
    }
}

