package com.projetoneki.backend.dto.Response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {

    private Long id;

    private String name;

    private LocalDateTime date;

    private String location;

    private boolean hasImage;
}
