package com.projetoneki.backend.dto.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Atualização parcial: data e localização são opcionais, conforme o
 * desafio ("atualizar a data OU localização"). Pelo menos um dos dois
 * deve vir preenchido (validado no EventService).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventUpdateRequest {

    private String date;

    private String location;
}
