package com.bootcamp.microservice_bootcamp.infrastructure.entrypoints.dto;

import lombok.Data;

@Data
public class PersonSummaryDTO {
    private String name;
    private String email;
}