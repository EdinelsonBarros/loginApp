package org.example.loginapp.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity extends BaseEntity{

    @NotNull
    @Column(nullable = false)
    private String userName;

    @NotNull
    @Column(nullable = false, unique = true)
    private String userEmail;

    @NotNull
    @Column(nullable = false)
    private String hashPassword;

    @Column(nullable = false, unique = true)
    private String userCpf;

    @JoinColumn(name = "company_id")
    @ManyToOne
    private Company company;



}

