package org.example.loginapp.repositories;

import org.example.loginapp.domain.UserEntity;
import org.example.loginapp.domain.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, String> {
    List<UserRole> findByUserEntity(UserEntity userEntity);
}
