package com.bootcamp.microservice_bootcamp.infrastructure.entrypoints.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class BootcampDTO {
    private Long id;
    private String name;
    private String description;
    private List<Long> capacityIds;
    private LocalDate releaseDate;
    private Integer duration;
}