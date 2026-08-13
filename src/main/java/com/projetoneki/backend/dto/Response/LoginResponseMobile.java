package com.projetoneki.backend.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseMobile {

    private String token;
    private String type;
    private Long adminId;
    private String name;
    private String email;
}
