package com.bootcamp.microservice_bootcamp.domain.usecase;


import com.bootcamp.microservice_bootcamp.domain.api.IBootcampServicePort;
import com.bootcamp.microservice_bootcamp.domain.enums.TechnicalMessage;
import com.bootcamp.microservice_bootcamp.domain.exceptions.BusinessException;
import com.bootcamp.microservice_bootcamp.domain.model.Bootcamp;
import com.bootcamp.microservice_bootcamp.domain.model.BootcampReportData;
import com.bootcamp.microservice_bootcamp.domain.model.BootcampWithCapacitiesAndPersons;
import com.bootcamp.microservice_bootcamp.domain.model.BootcampWithCapacitiesAndTechnologies;
import com.bootcamp.microservice_bootcamp.domain.spi.IBootcampCapacityAssociationPort;
import com.bootcamp.microservice_bootcamp.domain.spi.IBootcampPersistencePort;
import com.bootcamp.microservice_bootcamp.domain.spi.IBootcampQueryPort;
import com.bootcamp.microservice_bootcamp.domain.spi.IBootcampReportSenderPort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

public class BootcampUseCase implements IBootcampServicePort {

    private final IBootcampPersistencePort bootcampPersistencePort;
    private final IBootcampCapacityAssociationPort bootcampCapacityAssociationPort;
    private final IBootcampQueryPort bootcampQueryPort;
    private final IBootcampReportSenderPort reportSenderPort;

    public BootcampUseCase(
            IBootcampPersistencePort bootcampPersistencePort,
            IBootcampCapacityAssociationPort bootcampCapacityAssociationPort,
            IBootcampQueryPort bootcampQueryPort,
            IBootcampReportSenderPort reportSenderPort
    ) {
        this.bootcampPersistencePort = bootcampPersistencePort;
        this.bootcampCapacityAssociationPort = bootcampCapacityAssociationPort;
        this.bootcampQueryPort = bootcampQueryPort;
        this.reportSenderPort = reportSenderPort;
    }

    @Override
    public Mono<String> registerBootcampWithCapacities(Bootcamp bootcamp, List<Long> capacityIds) {
        return validateCapacity(bootcamp)
                .then(validateTechnologyIds(capacityIds))
                .then(checkIfBootcampExists(bootcamp.name()))
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new BusinessException(TechnicalMessage.BOOTCAMP_ALREADY_EXISTS));
                    }
                    return saveBootcampAndAssociate(bootcamp, capacityIds);
                });
    }

    private Mono<Boolean> checkIfBootcampExists(String name) {
        return bootcampPersistencePort.existsByName(name);
    }

    private Mono<String> saveBootcampAndAssociate(Bootcamp bootcamp, List<Long> capacityIds) {
        return bootcampPersistencePort.save(bootcamp)
                .flatMap(savedBootcamp ->
                        bootcampCapacityAssociationPort.associateCapacityToBootcamp(savedBootcamp.id(), capacityIds)
                                .flatMap(success -> {
                                    if (Boolean.TRUE.equals(success)) {
                                        return sendReportSilently(savedBootcamp)
                                                .thenReturn(TechnicalMessage.BOOTCAMP_CREATED.name());
                                    } else {
                                        return bootcampPersistencePort.deleteById(savedBootcamp.id())
                                                .then(Mono.error(new BusinessException(TechnicalMessage.BOOTCAMP_ASSOCIATION_FAILED)));
                                    }
                                }));
    }

    private Mono<Void> sendReportSilently(Bootcamp bootcamp) {
        BootcampReportData report = BootcampReportData.builder()
                .bootcampId(bootcamp.id())
                .name(bootcamp.name())
                .description(bootcamp.description())
                .releaseDate(bootcamp.releaseDate())
                .duration(bootcamp.duration())
                .build();

        return reportSenderPort.sendBootcampReport(report)
                .onErrorResume(e -> Mono.empty());
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

    @Override
    public Mono<List<Long>> validateAndReturnIds(List<Long> ids) {
        return Flux.fromIterable(ids)
                .flatMap(id ->
                        bootcampPersistencePort.existsById(id)
                                .flatMap(exists -> {
                                    if (Boolean.FALSE.equals(exists)) {
                                        return Mono.error(new BusinessException(
                                                TechnicalMessage.BOOTCAMP_NOT_FOUND
                                        ));
                                    }
                                    return bootcampPersistencePort.findById(id);
                                })
                )
                .collectList()
                .flatMap(bootcamps -> {
                    for (int i = 0; i < bootcamps.size(); i++) {
                        for (int j = i + 1; j < bootcamps.size(); j++) {
                            Bootcamp b1 = bootcamps.get(i);
                            Bootcamp b2 = bootcamps.get(j);
                            if (Objects.equals(b1.releaseDate(), b2.releaseDate())
                                    || Objects.equals(b1.duration(), b2.duration())) {
                                return Mono.error(new BusinessException(
                                        TechnicalMessage.BOOTCAMP_DUPLICATE_DATE_DURATION
                                        ));
                            }
                        }
                    }
                    return Mono.just(ids);
                });
    }

    @Override
    public Mono<BootcampWithCapacitiesAndPersons> findBootcampWithMostPersons() {
        return bootcampQueryPort.findBootcampWithMostPersons();
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

