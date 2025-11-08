package com.firststudent.platform.viviendasmartbackend.iam.domain.model.entities;

import com.firststudent.platform.viviendasmartbackend.iam.domain.model.valueobjects.Roles;
import com.firststudent.platform.viviendasmartbackend.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Role extends AuditableModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, unique = true,nullable = false)
    private Roles name;

    protected Role() { }

    public Role(Roles name) { this.name = name; }

    public static Role from(String name) {
        return new Role(Roles.valueOf(name.toUpperCase()));
    }

}
