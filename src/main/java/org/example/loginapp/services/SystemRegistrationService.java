package org.example.loginapp.services;

import lombok.RequiredArgsConstructor;
import org.example.loginapp.domain.SystemEntity;
import org.example.loginapp.dto.SystemRegistrationRequest;
import org.example.loginapp.repositories.SystemEntityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SystemRegistrationService {

    private final SystemEntityRepository systemEntityRepository;

    @Transactional
    public SystemEntity ensureRegistered(
            String systemCode,
            SystemRegistrationRequest request
    ) {
        return systemEntityRepository.findBySystemCode(systemCode)
                .orElseGet(() -> {
                    SystemEntity system = new SystemEntity();
                    system.setSystemCode(systemCode);
                    system.setSystemName(request.systemName());
                    system.setCategory(request.category());
                    system.setUrl(request.url());
                    system.setIconPath(request.iconPath());

                    return systemEntityRepository.save(system);
                });
    }
}