package org.example.loginapp.repositories;

import org.example.loginapp.domain.Role;
import org.example.loginapp.domain.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermission, String> {
    List<RolePermission> findByRole(Role role);
}