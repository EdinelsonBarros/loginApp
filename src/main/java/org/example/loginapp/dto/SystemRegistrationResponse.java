package org.example.loginapp.dto;

import org.example.loginapp.domain.SystemEntity;

public record SystemRegistrationResponse(
        String systemCode,
        String systemName,
        String category,
        String url,
        String iconPath
) {
    public static SystemRegistrationResponse from(SystemEntity system) {
        return new SystemRegistrationResponse(
                system.getSystemCode(),
                system.getSystemName(),
                system.getCategory(),
                system.getUrl(),
                system.getIconPath()
        );
    }
}