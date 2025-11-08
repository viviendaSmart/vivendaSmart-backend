package com.firststudent.platform.viviendasmartbackend.iam.domain.services;

import com.firststudent.platform.viviendasmartbackend.iam.domain.model.commands.SignInCommand;
import com.firststudent.platform.viviendasmartbackend.iam.domain.model.commands.SignUpCommand;

public interface UserCommandService {
    void handle(SignUpCommand command);

    void handle(SignInCommand command);
}
