package com.bootcamp.microservice_bootcamp.infrastructure.entrypoints.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BootcampWithPersonsAndCapacitiesDTO {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private List<PersonSummaryDTO> registeredPersons;
    private List<CapacityWithTechnologiesDTO> capacities;
}
