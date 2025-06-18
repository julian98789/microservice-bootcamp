package com.bootcamp.microservice_bootcamp.domain.model;

import java.time.LocalDate;
import java.util.List;

public record BootcampWithCapacitiesAndTechnologies(
        Long id,
        String name,
        String description,
        LocalDate releaseDate,
        Integer duration,
        List<CapacityWithTechnologies> capacities
) {}