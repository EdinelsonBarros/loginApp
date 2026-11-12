package org.example.loginapp.repositories;

import org.example.loginapp.domain.CompanyManaging;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyManagingRepository extends JpaRepository<CompanyManaging, String> {
}
