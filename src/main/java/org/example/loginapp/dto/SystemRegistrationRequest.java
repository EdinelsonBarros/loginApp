package org.example.loginapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SystemRegistrationRequest(

        @NotBlank(message = "Informe o nome do sistema")
        @Size(max = 255)
        String systemName,

        @NotBlank(message = "Informe a categoria do sistema")
        @Size(max = 255)
        String category,

        @NotBlank(message = "Informe a URL do sistema")
        @Size(max = 255)
        String url,

        @NotBlank(message = "Informe o caminho do ícone")
        @Size(max = 255)
        String iconPath
) {
}