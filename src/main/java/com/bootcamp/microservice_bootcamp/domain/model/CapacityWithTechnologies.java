package com.bootcamp.microservice_bootcamp.domain.model;

import java.util.List;

public record CapacityWithTechnologies(
        Long id,
        String name,
        List<TechnologySummary> technologies
) {}