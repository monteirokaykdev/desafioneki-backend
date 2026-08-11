package com.projetoneki.backend.services;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.projetoneki.backend.dto.Request.LoginRequest;
import com.projetoneki.backend.dto.Response.LoginResponse;
import com.projetoneki.backend.exception.InvalidCredentialsException;
import com.projetoneki.backend.model.Admin;
import com.projetoneki.backend.repository.AdminRepository;
import com.projetoneki.backend.security.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final AdminRepository adminRepository;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public record LoginResult(String token, LoginResponse body) {}

    // Login
    public LoginResult login(LoginRequest request) {

        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );

        Admin admin = adminRepository.findByEmail(request.getEmail())
            .orElseThrow(() ->
                new InvalidCredentialsException(
                    "Email ou senha incorretos"
                )
            );

        String token = jwtUtil.generateToken(
            admin.getId(),
            admin.getEmail(),
            request.isRemember()
        );

        LoginResponse body = LoginResponse.builder()
            .type("Bearer")
            .adminId(admin.getId())
            .name(admin.getName())
            .email(admin.getEmail())
            .build();

        return new LoginResult(token, body);
    }

    // Esqueci minha senha
    public void forgotPassword(String email) {

        Optional<Admin> adminOptional =
            adminRepository.findByEmail(email);

        if (adminOptional.isEmpty()) {
            return;
        }

        Admin admin = adminOptional.get();

        String token = UUID.randomUUID().toString();

        admin.setResetToken(token);

        admin.setResetTokenExpiration(
            LocalDateTime.now().plusMinutes(30)
        );

        adminRepository.save(admin);

        emailService.sendPasswordResetEmail(
            admin.getEmail(),
            token
        );
    }

    // Redefinir senha
    public void resetPassword(String token, String newPassword) {

        Admin admin = adminRepository
            .findByResetToken(token)
            .orElseThrow(() ->
                new RuntimeException("Token inválido.")
            );

        if (
            admin.getResetTokenExpiration() == null ||
            admin.getResetTokenExpiration()
                .isBefore(LocalDateTime.now())
        ) {
            throw new RuntimeException("Token expirado.");
        }

        admin.setPassword(
            passwordEncoder.encode(newPassword)
        );

        // Invalida o token depois de utilizá-lo
        admin.setResetToken(null);
        admin.setResetTokenExpiration(null);

        adminRepository.save(admin);
    }
}