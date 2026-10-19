package org.example.loginapp.config;

import lombok.RequiredArgsConstructor;
import org.example.loginapp.domain.Role;
import org.example.loginapp.domain.UserRole;
import org.example.loginapp.domain.UserEntity;
import org.example.loginapp.repositories.RolePermissionRepository;
import org.example.loginapp.repositories.UserRepository;
import org.example.loginapp.repositories.UserRoleRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.util.*;

@Configuration
@RequiredArgsConstructor
public class JwtCustomizerConfig {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
        return context -> {
            if (!context.getTokenType().equals(OAuth2TokenType.ACCESS_TOKEN)) {
                return;
            }

            String email = context.getPrincipal().getName();
            UserEntity user = userRepository.findByUserEmail(email).orElseThrow();

            Map<String, List<String>> authoritiesBySystem = new HashMap<>();

            for (UserRole userRole : userRoleRepository.findByUserEntity(user)) {
                Role role = userRole.getRole();
                if (role.getSystemEntity() == null) continue;

                String systemName = role.getSystemEntity().getSystemName();

                List<String> permissions = rolePermissionRepository.findByRole(role).stream()
                        .map(rp -> rp.getPermission().getPermissionName())
                        .toList();

                authoritiesBySystem
                        .computeIfAbsent(systemName, k -> new ArrayList<>())
                        .addAll(permissions);
            }

            context.getClaims()
                    .claim("authorities", authoritiesBySystem)
                    .claim("name", user.getUserName());
        };
    }
}