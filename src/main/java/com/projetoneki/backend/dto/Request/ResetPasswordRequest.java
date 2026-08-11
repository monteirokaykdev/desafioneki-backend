package com.projetoneki.backend.dto.Request;

import lombok.Data;

@Data
public class ResetPasswordRequest {

    private String token;
    private String newPassword;
}