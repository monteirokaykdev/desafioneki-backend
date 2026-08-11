package com.projetoneki.backend.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendPasswordResetEmail(String email, String token) {

        String resetLink =
                "http://localhost:3000/esqueci-minha-senha?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("noreply@eventhub.com");
        message.setTo(email);
        message.setSubject("Recuperação de senha - EVENTHUB");

        message.setText(
            "Olá!\n\n" +
            "Recebemos uma solicitação para redefinir sua senha.\n\n" +
            "Clique no link abaixo para criar uma nova senha:\n\n" +
            resetLink +
            "\n\n" +
            "Este link é válido por 30 minutos.\n\n" +
            "Se você não solicitou essa alteração, ignore este e-mail."
        );

        mailSender.send(message);
    }
}
