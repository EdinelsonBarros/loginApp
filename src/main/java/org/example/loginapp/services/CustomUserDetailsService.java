package org.example.loginapp.services;

import lombok.RequiredArgsConstructor;
import org.example.loginapp.domain.UserEntity;
import org.example.loginapp.repositories.UserRepository;
import org.example.loginapp.repositories.UserRoleRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("Tentando autenticar: " + email);
        UserEntity user = userRepository.findByUserEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        System.out.println("Usuário encontrado, hash: " + user.getHashPassword());

        List<GrantedAuthority> authorities = userRoleRepository.findByUserEntity(user).stream()
                .map(ur -> new SimpleGrantedAuthority("ROLE_" + ur.getRole().getRoleName()))
                .collect(Collectors.toList());

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUserEmail())
                .password(user.getHashPassword())
                .authorities(authorities)
                .disabled(!user.isActive())
                .build();
    }
}
