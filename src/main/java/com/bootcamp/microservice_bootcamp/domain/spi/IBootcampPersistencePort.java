package com.bootcamp.microservice_bootcamp.domain.spi;

import com.bootcamp.microservice_bootcamp.domain.model.Bootcamp;
import reactor.core.publisher.Mono;

public interface IBootcampPersistencePort {
    Mono<Bootcamp> save(Bootcamp bootcamp);
    Mono<Boolean> existsByName(String name);
    Mono<Void> deleteById(Long id);
    Mono<Boolean> existsById(Long id);
    Mono<Bootcamp> findById(Long id);
}
