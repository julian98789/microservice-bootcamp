package com.bootcamp.microservice_bootcamp.domain.usecase;

import com.bootcamp.microservice_bootcamp.domain.enums.TechnicalMessage;
import com.bootcamp.microservice_bootcamp.domain.exceptions.BusinessException;
import com.bootcamp.microservice_bootcamp.domain.model.Bootcamp;
import com.bootcamp.microservice_bootcamp.domain.model.BootcampWithCapacitiesAndTechnologies;
import com.bootcamp.microservice_bootcamp.domain.spi.IBootcampCapacityAssociationPort;
import com.bootcamp.microservice_bootcamp.domain.spi.IBootcampPersistencePort;
import com.bootcamp.microservice_bootcamp.domain.spi.IBootcampQueryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BootcampUseCaseTest {

    @Mock
    private IBootcampPersistencePort persistencePort;
    @Mock
    private IBootcampCapacityAssociationPort associationPort;
    @Mock
    private IBootcampQueryPort queryPort;

    private BootcampUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new BootcampUseCase(persistencePort, associationPort, queryPort);
    }

    @Test
    void registerBootcampWithCapacities_success() {
        Bootcamp bootcamp = new Bootcamp(null, "Bootcamp", "Descripción", LocalDate.now(), 10);
        List<Long> capacityIds = List.of(1L, 2L);

        when(persistencePort.existsByName("Bootcamp")).thenReturn(Mono.just(false));
        when(persistencePort.save(bootcamp)).thenReturn(Mono.just(new Bootcamp(1L, "Bootcamp",
                "Descripción", LocalDate.now(), 10)));
        when(associationPort.associateCapacityToBootcamp(1L, capacityIds)).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.registerBootcampWithCapacities(bootcamp, capacityIds))
                .expectNext(TechnicalMessage.BOOTCAMP_CREATED.name())
                .verifyComplete();
    }

    @Test
    void registerBootcampWithCapacities_duplicateCapacities() {
        Bootcamp bootcamp = new Bootcamp(null, "Bootcamp", "Descripción", LocalDate.now(), 10);
        List<Long> capacityIds = List.of(1L, 1L);

        StepVerifier.create(useCase.registerBootcampWithCapacities(bootcamp, capacityIds))
                .expectErrorSatisfies(e -> {
                    assert e instanceof BusinessException;
                    assert ((BusinessException) e).getTechnicalMessage() == TechnicalMessage.DUPLICATE_CAPACITY_ID;
                })
                .verify();
    }

    @Test
    void registerBootcampWithCapacities_invalidName() {
        Bootcamp bootcamp = new Bootcamp(null, "", "Descripción", LocalDate.now(), 10);

        StepVerifier.create(useCase.registerBootcampWithCapacities(bootcamp, List.of(1L)))
                .expectErrorSatisfies(e -> {
                    assert e instanceof BusinessException;
                    assert ((BusinessException) e).getTechnicalMessage() == TechnicalMessage.INVALID_CAPACITY_NAME;
                })
                .verify();
    }

    @Test
    void registerBootcampWithCapacities_invalidDescription() {
        Bootcamp bootcamp = new Bootcamp(null, "Bootcamp", "", LocalDate.now(), 10);

        StepVerifier.create(useCase.registerBootcampWithCapacities(bootcamp, List.of(1L)))
                .expectErrorSatisfies(e -> {
                    assert e instanceof BusinessException;
                    assert ((BusinessException) e).getTechnicalMessage() == TechnicalMessage.INVALID_CAPACITY_DESCRIPTION;
                })
                .verify();
    }

    @Test
    void registerBootcampWithCapacities_bootcampAlreadyExists() {
        Bootcamp bootcamp = new Bootcamp(null, "Bootcamp", "Descripción", LocalDate.now(), 10);
        List<Long> capacityIds = List.of(1L);

        when(persistencePort.existsByName("Bootcamp")).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.registerBootcampWithCapacities(bootcamp, capacityIds))
                .expectErrorSatisfies(e -> {
                    assert e instanceof BusinessException;
                    assert ((BusinessException) e).getTechnicalMessage() == TechnicalMessage.BOOTCAMP_ALREADY_EXISTS;
                })
                .verify();
    }

    @Test
    void registerBootcampWithCapacities_associationFailed() {
        Bootcamp bootcamp = new Bootcamp(null, "Bootcamp", "Descripción", LocalDate.now(), 10);
        Bootcamp saved = new Bootcamp(1L, "Bootcamp", "Descripción", LocalDate.now(), 10);
        List<Long> capacityIds = List.of(1L);

        when(persistencePort.existsByName("Bootcamp")).thenReturn(Mono.just(false));
        when(persistencePort.save(bootcamp)).thenReturn(Mono.just(saved));
        when(associationPort.associateCapacityToBootcamp(1L, capacityIds)).thenReturn(Mono.just(false));
        when(persistencePort.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.registerBootcampWithCapacities(bootcamp, capacityIds))
                .expectErrorSatisfies(e -> {
                    assert e instanceof BusinessException;
                    assert ((BusinessException) e).getTechnicalMessage() == TechnicalMessage.BOOTCAMP_ASSOCIATION_FAILED;
                })
                .verify();
    }

    @Test
    void deleteBootcampAndCascade_success() {
        when(associationPort.deleteCapacitiesByBootcampId(1L)).thenReturn(Mono.empty());
        when(persistencePort.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.deleteBootcampAndCascade(1L))
                .verifyComplete();
    }

    @Test
    void listBootcampsPagedAndSorted_success() {
        BootcampWithCapacitiesAndTechnologies mock = mock(BootcampWithCapacitiesAndTechnologies.class);
        when(queryPort.listBootcampsPagedAndSorted(0, 10, "name", "asc"))
                .thenReturn(Flux.just(mock));

        StepVerifier.create(useCase.listBootcampsPagedAndSorted(0, 10, "name", "asc"))
                .expectNext(mock)
                .verifyComplete();
    }
}