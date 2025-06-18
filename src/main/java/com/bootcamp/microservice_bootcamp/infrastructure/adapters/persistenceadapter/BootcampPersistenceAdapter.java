package com.bootcamp.microservice_bootcamp.infrastructure.adapters.persistenceadapter;

import com.bootcamp.microservice_bootcamp.domain.model.Bootcamp;
import com.bootcamp.microservice_bootcamp.domain.spi.IBootcampPersistencePort;
import com.bootcamp.microservice_bootcamp.infrastructure.adapters.persistenceadapter.entity.BootcampEntity;
import com.bootcamp.microservice_bootcamp.infrastructure.adapters.persistenceadapter.mapper.IBootcampEntityMapper;
import com.bootcamp.microservice_bootcamp.infrastructure.adapters.persistenceadapter.repository.IBootcampRepository;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class BootcampPersistenceAdapter implements IBootcampPersistencePort {
    private final IBootcampRepository bootcampRepository;
    private final IBootcampEntityMapper bootcampEntityMapper;

    @Override
    public Mono<Bootcamp> save(Bootcamp capacity) {
        BootcampEntity entity = bootcampEntityMapper.toEntity(capacity);
        return bootcampRepository.save(entity)
                .map(bootcampEntityMapper::toModel);
    }

    @Override
    public Mono<Boolean> existsByName(String name) {
        return bootcampRepository.findByName(name)
                .map(bootcampEntityMapper::toModel)
                .map(tech -> true)
                .defaultIfEmpty(false);
    }

    @Override
    @Transactional
    public Mono<Void> deleteById(Long id) {
        return bootcampRepository.deleteById(id);
    }

    @Override
    public Mono<Boolean> existsById(Long id) {
        return bootcampRepository.existsById(id);
    }


}