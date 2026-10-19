package org.example.loginapp.repositories;

import org.example.loginapp.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository  extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByUserEmail(String email);
    boolean existsByUserCpf(String userCpf);
}
