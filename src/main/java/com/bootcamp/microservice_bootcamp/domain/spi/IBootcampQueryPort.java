package com.bootcamp.microservice_bootcamp.domain.spi;

import com.bootcamp.microservice_bootcamp.domain.model.BootcampWithCapacitiesAndTechnologies;
import reactor.core.publisher.Flux;

public interface IBootcampQueryPort {
    Flux<BootcampWithCapacitiesAndTechnologies> listBootcampsPagedAndSorted(
            int page,
            int size,
            String sortBy,
            String direction
    );
}