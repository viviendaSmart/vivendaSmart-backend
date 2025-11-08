package com.firststudent.platform.viviendasmartbackend.iam.interfaces.rest.transform;

import com.firststudent.platform.viviendasmartbackend.iam.domain.model.commands.SignInCommand;
import com.firststudent.platform.viviendasmartbackend.iam.interfaces.rest.resources.SignInResource;

public class SignInCommandFromResourceAssembler {
    public static SignInCommand toCommandfromResource(SignInResource resource) {
        return new SignInCommand(resource.email(), resource.password());
    }
}
