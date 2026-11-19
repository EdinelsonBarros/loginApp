package org.example.loginapp.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.loginapp.domain.SystemEntity;
import org.example.loginapp.dto.SystemRegistrationRequest;
import org.example.loginapp.dto.SystemRegistrationResponse;
import org.example.loginapp.services.SystemRegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/registry/systems")
@RequiredArgsConstructor
public class SystemEntityController {

    private final SystemRegistrationService systemRegistrationService;

    @PutMapping("/{systemCode}")
    public ResponseEntity<SystemRegistrationResponse> ensureRegistered(
            @PathVariable String systemCode,
            @Valid @RequestBody SystemRegistrationRequest request
    ) {
        SystemEntity system = systemRegistrationService.ensureRegistered(
                systemCode,
                request
        );

        return ResponseEntity.ok(SystemRegistrationResponse.from(system));
    }
}