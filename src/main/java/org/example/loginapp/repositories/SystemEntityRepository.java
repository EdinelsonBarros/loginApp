package org.example.loginapp.repositories;

import org.example.loginapp.domain.SystemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SystemEntityRepository
        extends JpaRepository<SystemEntity, String> {

    Optional<SystemEntity> findBySystemCode(String systemCode);
}