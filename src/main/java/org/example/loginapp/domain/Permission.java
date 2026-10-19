package org.example.loginapp.domain;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Permission extends BaseEntity{

    private String permissionName;
    private String permissionDescription;

    @ManyToOne
    @JoinColumn(name = "system_id")
    private SystemEntity systemEntity;

}
