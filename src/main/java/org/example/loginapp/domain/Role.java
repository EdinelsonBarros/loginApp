package org.example.loginapp.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Role extends BaseEntity{

    @NotNull
    @Column(nullable = false)
    private String roleName;

    private String description;

    @ManyToOne
    @JoinColumn(name = "system_id")
    private SystemEntity systemEntity;

    @JoinColumn(name = "company_id")
    @ManyToOne
    private Company company;

    private String rolePosition; // cargo

}
