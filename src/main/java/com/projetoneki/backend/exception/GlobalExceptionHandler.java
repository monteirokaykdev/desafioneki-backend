package com.projetoneki.backend.exception;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage(), null);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiErrorResponse> handleForbidden(ForbiddenException ex) {
        return build(HttpStatus.FORBIDDEN, "Acesso negado", ex.getMessage(), null);
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<ApiErrorResponse> handleEmailDuplicado(EmailAlreadyRegisteredException ex) {
        return build(HttpStatus.CONFLICT, "Email já cadastrado", ex.getMessage(), null);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleRequisicaoInvalida(InvalidRequestException ex) {
        return build(HttpStatus.BAD_REQUEST, "Requisição inválida", ex.getMessage(), null);
    }

    @ExceptionHandler(InvalidImageException.class)
    public ResponseEntity<ApiErrorResponse> handleImagemInvalida(InvalidImageException ex) {
        return build(HttpStatus.BAD_REQUEST, "Imagem inválida", ex.getMessage(), null);
    }

    @ExceptionHandler({InvalidCredentialsException.class, BadCredentialsException.class})
    public ResponseEntity<ApiErrorResponse> handleCredenciaisInvalidas(RuntimeException ex) {
        return build(HttpStatus.UNAUTHORIZED, "Credenciais inválidas", "Email ou senha incorretos", null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidacao(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Erro de validação", "Verifique os campos enviados", details);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenerico(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", "Ocorreu um erro inesperado", null);
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String error, String message, List<String> details) {
        ApiErrorResponse body = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(message)
                .details(details)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
