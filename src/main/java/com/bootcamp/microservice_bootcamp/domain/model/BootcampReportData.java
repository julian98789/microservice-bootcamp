package com.bootcamp.microservice_bootcamp.domain.model;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record BootcampReportData(
        Long bootcampId,
        String name,
        String description,
        LocalDate releaseDate,
        Integer duration,
        Integer registeredPersonCount,
        Integer capacityCount,
        Integer totalTechnologyCount
) {}