package com.bootcamp.microservice_bootcamp.domain.api;



import com.bootcamp.microservice_bootcamp.domain.model.Bootcamp;
import com.bootcamp.microservice_bootcamp.domain.model.BootcampWithCapacitiesAndTechnologies;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IBootcampServicePort {
    Mono<String> registerBootcampWithCapacities(Bootcamp capacity, List<Long> capacityIds);
    Flux<BootcampWithCapacitiesAndTechnologies> listBootcampsPagedAndSorted(
            int page,
            int size,
            String sortBy,
            String direction
    );
    Mono<Void> deleteBootcampAndCascade(Long bootcampId);
}
