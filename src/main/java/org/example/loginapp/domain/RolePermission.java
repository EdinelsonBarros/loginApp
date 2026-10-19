package org.example.loginapp.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RolePermission extends BaseEntity{

    @JoinColumn(name = "role_id")
    @ManyToOne
    private Role role;

    @JoinColumn(name = "permission_id")
    @ManyToOne
    private Permission permission;

}
