package com.bootcamp.microservice_bootcamp.domain.spi;

import reactor.core.publisher.Mono;

import java.util.List;

public interface IBootcampCapacityAssociationPort {
    Mono<Boolean> associateCapacityToBootcamp(Long bootcampId, List<Long> capacityIds);
    Mono<Void> deleteCapacitiesByBootcampId(Long bootcampId);
}