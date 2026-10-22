package org.example.loginapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;


public record CompanyRegistrationRequest(
        @NotBlank(message = "Informe o nome fantasia")
        String nomeFantasia,

        @NotBlank(message = "Informe a razão social")
        String companySocialName,

        @NotBlank(message = "Informe o CNPJ")
        @Pattern(regexp = "\\d{14}", message = "Informe os 14 números do CNPJ")
        String cnpj,

        @NotBlank(message = "Informe o e-mail")
        @Email(message = "Informe um e-mail válido")
        String email,

        String postalCode,
        String address,
        String addressNumber,
        String city
) {
}
