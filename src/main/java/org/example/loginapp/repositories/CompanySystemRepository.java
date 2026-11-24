package org.example.loginapp.repositories;

import org.example.loginapp.domain.Company;
import org.example.loginapp.domain.CompanySystem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanySystemRepository
        extends JpaRepository<CompanySystem, String> {

    List<CompanySystem> findByCompany(Company company);
}