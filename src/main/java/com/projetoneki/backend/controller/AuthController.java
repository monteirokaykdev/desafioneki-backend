package com.projetoneki.backend.controller;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projetoneki.backend.dto.Request.ForgotPasswordRequest;
import com.projetoneki.backend.dto.Request.LoginRequest;
import com.projetoneki.backend.dto.Request.ResetPasswordRequest;
import com.projetoneki.backend.dto.Response.LoginResponse;
import com.projetoneki.backend.dto.Response.LoginResponseMobile;
import com.projetoneki.backend.security.JwtUtil;
import com.projetoneki.backend.services.AuthService;
import com.projetoneki.backend.services.AuthService.LoginResult;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Api(tags = "Autenticação")
public class AuthController {

    private static final String COOKIE_NAME = "token";

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    // Login do administrador: autentica email + senha, gera token JWT e seta num cookie HttpOnly
    @PostMapping("/login")
    @ApiOperation("Autentica o administrador (email + senha) e seta o token num cookie HttpOnly")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse httpResponse) {

        LoginResult result = authService.login(request);

        long maxAgeSeconds = (request.isRemember()
                ? jwtUtil.getRememberExpirationMs()
                : jwtUtil.getDefaultExpirationMs()) / 1000;

        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, result.token())
                .httpOnly(true)
                .secure(true) // exige HTTPS em produção; navegadores tratam localhost como contexto seguro em dev
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite("Lax")
                .build();

        httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(result.body());
    }

    //Endpoint para login no mobile, token vai no corpo da resposta
    @PostMapping("/login/mobile")
    @ApiOperation("Autentica o administrador (email + senha) e retorna o token no corpo, para uso em apps mobile")
    public ResponseEntity<LoginResponseMobile> loginMobile(@Valid @RequestBody LoginRequest request) {

        LoginResult result = authService.login(request);

        LoginResponseMobile body = LoginResponseMobile.builder()
                .token(result.token())
                .type(result.body().getType())
                .adminId(result.body().getAdminId())
                .name(result.body().getName())
                .email(result.body().getEmail())
                .token(result.token())
                .build();

        return ResponseEntity.ok(body);
    }

    @PostMapping("/esqueci-senha")
    public ResponseEntity<?> forgotPassword(
            @RequestBody ForgotPasswordRequest request) {

        authService.forgotPassword(request.getEmail());

        return ResponseEntity.ok(
                Map.of(
                    "message",
                    "Se o e-mail estiver cadastrado, você receberá as instruções."
                )
        );
    }

    // Reset da senha: valida token de reset, altera senha e invalida token
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestBody ResetPasswordRequest request) {

        authService.resetPassword(
            request.getToken(),
            request.getNewPassword()
        );

        return ResponseEntity.ok(
            Map.of(
                "message",
                "Senha alterada com sucesso."
            )
        );
    }

    // Invalida o cookie de sessão do administrador, efetivamente "deslogando" o usuário
    @PostMapping("/logout")
    @ApiOperation("Invalida o cookie de sessão do administrador")
    public ResponseEntity<Void> logout(HttpServletResponse httpResponse) {

        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.noContent().build();
    }
}