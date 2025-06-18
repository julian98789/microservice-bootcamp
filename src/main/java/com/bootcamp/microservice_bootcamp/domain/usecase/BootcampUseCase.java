package com.bootcamp.microservice_bootcamp.domain.usecase;


import com.bootcamp.microservice_bootcamp.domain.api.IBootcampServicePort;
import com.bootcamp.microservice_bootcamp.domain.enums.TechnicalMessage;
import com.bootcamp.microservice_bootcamp.domain.exceptions.BusinessException;
import com.bootcamp.microservice_bootcamp.domain.model.Bootcamp;
import com.bootcamp.microservice_bootcamp.domain.model.BootcampWithCapacitiesAndTechnologies;
import com.bootcamp.microservice_bootcamp.domain.spi.IBootcampCapacityAssociationPort;
import com.bootcamp.microservice_bootcamp.domain.spi.IBootcampPersistencePort;
import com.bootcamp.microservice_bootcamp.domain.spi.IBootcampQueryPort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BootcampUseCase implements IBootcampServicePort {

    private final IBootcampPersistencePort bootcampPersistencePort;
    private final IBootcampCapacityAssociationPort bootcampCapacityAssociationPort;
    private final IBootcampQueryPort bootcampQueryPort;

    public BootcampUseCase(
            IBootcampPersistencePort bootcampPersistencePort,
            IBootcampCapacityAssociationPort bootcampCapacityAssociationPort, IBootcampQueryPort bootcampQueryPort
    ) {
        this.bootcampPersistencePort = bootcampPersistencePort;
        this.bootcampCapacityAssociationPort = bootcampCapacityAssociationPort;
        this.bootcampQueryPort = bootcampQueryPort;
    }

    public Mono<String> registerBootcampWithCapacities(Bootcamp bootcamp, List<Long> capacityIds) {
        return validateCapacity(bootcamp)
                .then(validateTechnologyIds(capacityIds))
                .then(bootcampPersistencePort.existsByName(bootcamp.name()))
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new BusinessException(TechnicalMessage.BOOTCAMP_ALREADY_EXISTS));
                    }
                    return bootcampPersistencePort.save(bootcamp)
                            .flatMap(savedCapacity -> {
                                return bootcampCapacityAssociationPort.associateCapacityToBootcamp(savedCapacity.id(), capacityIds)
                                        .flatMap(success -> {
                                            if (Boolean.TRUE.equals(success)) {
                                                return Mono.just(TechnicalMessage.BOOTCAMP_CREATED.name());
                                            } else {
                                                return bootcampPersistencePort.deleteById(savedCapacity.id())
                                                        .then(Mono.error(new BusinessException(TechnicalMessage.BOOTCAMP_ASSOCIATION_FAILED)));
                                            }
                                        });
                            });
                });
    }

    @Override
    public Flux<BootcampWithCapacitiesAndTechnologies> listBootcampsPagedAndSorted(
            int page, int size, String sortBy, String direction) {
        return bootcampQueryPort.listBootcampsPagedAndSorted(page, size, sortBy, direction);
    }

    @Override
    public Mono<Void> deleteBootcampAndCascade(Long bootcampId) {
        return bootcampCapacityAssociationPort.deleteCapacitiesByBootcampId(bootcampId)
                .then(bootcampPersistencePort.deleteById(bootcampId));
    }



    private Mono<Void> validateCapacity(Bootcamp bootcamp) {
        if (bootcamp.name() == null || bootcamp.name().isBlank() || bootcamp.name().length() > 50) {
            return Mono.error(new BusinessException(TechnicalMessage.INVALID_CAPACITY_NAME));
        }
        if (bootcamp.description() == null || bootcamp.description().isBlank() || bootcamp.description().length() > 90) {
            return Mono.error(new BusinessException(TechnicalMessage.INVALID_CAPACITY_DESCRIPTION));
        }
        return Mono.empty();
    }

    private Mono<Void> validateTechnologyIds(List<Long> capacityIds) {
        if (capacityIds == null || capacityIds.isEmpty()) {
            return Mono.error(new BusinessException(TechnicalMessage.INVALID_CAPACITY_LIST));
        }
        if (capacityIds.size() > 4) {
            return Mono.error(new BusinessException(TechnicalMessage.INVALID_CAPACITY_LIST));
        }
        Set<Long> uniqueIds = new HashSet<>(capacityIds);
        if (uniqueIds.size() != capacityIds.size()) {
            return Mono.error(new BusinessException(TechnicalMessage.DUPLICATE_CAPACITY_ID));
        }
        return Mono.empty();
    }

}

