package org.example.loginapp.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.loginapp.domain.Company;
import org.example.loginapp.domain.CompanyManaging;
import org.example.loginapp.domain.UserEntity;
import org.example.loginapp.dto.CompanyRegistrationRequest;
import org.example.loginapp.repositories.CompanyManagingRepository;
import org.example.loginapp.repositories.CompanyRepository;
import org.example.loginapp.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final CompanyManagingRepository companyManagingRepository;

    @GetMapping("/companyRegister")
    public String companyRegister(
            org.springframework.ui.Model model
    ) {
        model.addAttribute(
                "companyForm",
                new CompanyRegistrationRequest(
                        "", "", "", "", "", "", "", ""
                )
        );

        return "companyRegister";
    }

    @PostMapping("/companyRegister")
    public String registerCompany(
            @Valid @ModelAttribute("companyForm")
            CompanyRegistrationRequest form,
            BindingResult bindingResult,
            Authentication authentication
    ) {
        if (companyRepository.existsByCnpj(form.cnpj())) {
            bindingResult.rejectValue(
                    "cnpj",
                    "company.cnpj.duplicate",
                    "Já existe uma empresa cadastrada com este CNPJ."
            );
        }

        if (bindingResult.hasErrors()) {
            return "companyRegister";
        }

        UserEntity user = userRepository.findByUserEmail(authentication.getName())
                .orElseThrow();

        Company company = new Company();
        company.setNomeFantasia(form.nomeFantasia());
        company.setCompanySocialName(form.companySocialName());
        company.setCnpj(form.cnpj());
        company.setEmail(form.email());
        company.setPostalCode(form.postalCode());
        company.setAddress(form.address());
        company.setAddressNumber(form.addressNumber());
        company.setCity(form.city());

        companyRepository.save(company);

        CompanyManaging companyManaging = new CompanyManaging();
        companyManaging.setCompany(company);
        companyManaging.setUserEntity(user);

        companyManagingRepository.save(companyManaging);

        return "redirect:/companyRegister?registered";
    }
}