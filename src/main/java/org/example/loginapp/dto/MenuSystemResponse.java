package org.example.loginapp.dto;

public record MenuSystemResponse(
        String systemCode,
        String systemName,
        String category,
        String url,
        String iconPath
) {}
