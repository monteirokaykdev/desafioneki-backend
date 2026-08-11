package com.projetoneki.backend.dto.Request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class ForgotPasswordRequest {

    @NotBlank
    @Email
    String email;
}
