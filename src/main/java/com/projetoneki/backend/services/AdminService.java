package com.projetoneki.backend.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.projetoneki.backend.dto.Request.AdminRequest;
import com.projetoneki.backend.dto.Response.AdminResponse;
import com.projetoneki.backend.exception.EmailAlreadyRegisteredException;
import com.projetoneki.backend.model.Admin;
import com.projetoneki.backend.repository.AdminRepository;
import org.springframework.security.core.Authentication;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminResponse create(AdminRequest request) {
        // Verifica se o email já está registrado
        if (adminRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyRegisteredException("Email já cadastrado");
        }

        // Cria o objeto Admin a partir do request
        Admin admin = Admin.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // Senha criptografada(BCrypt)
                .build();

        Admin saved = adminRepository.save(admin);

        // Montando a respota manualmente
        return AdminResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .email(saved.getEmail())
                .build();
    }

    public AdminResponse getMe(Authentication authentication) {

        String email = authentication.getName();

        Admin admin = adminRepository.findByEmail(email)
            .orElseThrow(() ->
                new RuntimeException("Administrador não encontrado")
            );

        return AdminResponse.builder()
            .id(admin.getId())
            .name(admin.getName())
            .email(admin.getEmail())
            .build();
    }
}