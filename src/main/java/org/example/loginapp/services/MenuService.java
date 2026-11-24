package org.example.loginapp.services;

import org.example.loginapp.domain.CompanySystem;
import org.example.loginapp.domain.UserEntity;
import org.example.loginapp.dto.MenuSystemResponse;
import org.example.loginapp.repositories.CompanySystemRepository;
import org.example.loginapp.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MenuService {

    private final UserRepository userRepository;
    private final CompanySystemRepository companySystemRepository;

    public MenuService(
            UserRepository userRepository,
            CompanySystemRepository companySystemRepository
    ) {
        this.userRepository = userRepository;
        this.companySystemRepository = companySystemRepository;
    }

    @Transactional(readOnly = true)
    public List<MenuSystemResponse> findSystemsForUser(String email) {
        UserEntity user = userRepository.findByUserEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuário não encontrado: " + email
                ));

        if (user.getCompany() == null) {
            return List.of();
        }

        return companySystemRepository.findByCompany(user.getCompany())
                .stream()
                .filter(CompanySystem::isActive)
                .map(CompanySystem::getSystemEntity)
                .filter(system -> system != null && system.isActive())
                .map(system -> new MenuSystemResponse(
                        system.getSystemCode(),
                        system.getSystemName(),
                        system.getCategory(),
                        system.getUrl(),
                        system.getIconPath()
                ))
                .toList();
    }
}