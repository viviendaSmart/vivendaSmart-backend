package com.firststudent.platform.viviendasmartbackend.iam.infrastructure.hashing.bcrypt;

import com.firststudent.platform.viviendasmartbackend.iam.application.internal.outboundservices.hashing.HashingService;
import org.springframework.security.crypto.password.PasswordEncoder;

public interface BCryptHashingService extends HashingService, PasswordEncoder {
}
