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
public class Company extends BaseEntity{

    @NotNull
    @Column(nullable = false)
    private String nomeFantasia; //NOME FANTASIA

    @NotNull
    @Column(nullable = false)
    private String companySocialName;

    @NotNull
    @Column(nullable = false, unique = true)
    private String cnpj; //id_company

    private String email;

    private String postalCode; // CEP

    private String address;

    private String addressNumber;

    private String city;


}
