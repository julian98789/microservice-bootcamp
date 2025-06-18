package com.bootcamp.microservice_bootcamp.infrastructure.entrypoints.dto;

import lombok.Data;

import java.util.List;

@Data
public class CapacityWithTechnologiesDTO {
    private Long id;
    private String name;
    private List<TechnologySummaryDTO> technologies;
}