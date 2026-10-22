package org.example.loginapp.repositories;

import org.example.loginapp.domain.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, String> {
    boolean existsByCnpj(String cnpj);
}
