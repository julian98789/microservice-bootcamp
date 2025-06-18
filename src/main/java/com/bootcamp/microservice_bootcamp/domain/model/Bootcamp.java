package com.bootcamp.microservice_bootcamp.domain.model;

import java.time.LocalDate;

public record Bootcamp(
        Long id,
        String name,
        String description,
        LocalDate releaseDate,
        Integer duration
) {}
