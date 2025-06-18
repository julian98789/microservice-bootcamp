package com.bootcamp.microservice_bootcamp.infrastructure.entrypoints.util;

import com.bootcamp.microservice_bootcamp.infrastructure.entrypoints.dto.BootcampDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class APIResponse {
    private String code;
    private String message;
    private String identifier;
    private String date;
    private BootcampDTO data;
    private List<ErrorDTO> errors;
}
