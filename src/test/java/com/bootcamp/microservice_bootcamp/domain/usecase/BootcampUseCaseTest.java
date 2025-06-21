package com.bootcamp.microservice_bootcamp.domain.usecase;

import com.bootcamp.microservice_bootcamp.domain.enums.TechnicalMessage;
import com.bootcamp.microservice_bootcamp.domain.exceptions.BusinessException;
import com.bootcamp.microservice_bootcamp.domain.model.Bootcamp;
import com.bootcamp.microservice_bootcamp.domain.model.BootcampWithCapacitiesAndPersons;
import com.bootcamp.microservice_bootcamp.domain.model.BootcampWithCapacitiesAndTechnologies;
import com.bootcamp.microservice_bootcamp.domain.spi.IBootcampCapacityAssociationPort;
import com.bootcamp.microservice_bootcamp.domain.spi.IBootcampPersistencePort;
import com.bootcamp.microservice_bootcamp.domain.spi.IBootcampQueryPort;
import com.bootcamp.microservice_bootcamp.domain.spi.IBootcampReportSenderPort;
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

    @Mock
    private IBootcampReportSenderPort reportSenderPort;

    private BootcampUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new BootcampUseCase(persistencePort, associationPort, queryPort,reportSenderPort);
    }

    @Test
    void registerBootcampWithCapacities_success() {
        Bootcamp bootcamp = new Bootcamp(null, "Bootcamp", "Descripción", LocalDate.now(), 10);
        List<Long> capacityIds = List.of(1L, 2L);

        Bootcamp saved = new Bootcamp(1L, "Bootcamp", "Descripción", LocalDate.now(), 10);

        when(persistencePort.existsByName("Bootcamp")).thenReturn(Mono.just(false));
        when(persistencePort.save(bootcamp)).thenReturn(Mono.just(saved));
        when(associationPort.associateCapacityToBootcamp(1L, capacityIds)).thenReturn(Mono.just(true));
        when(reportSenderPort.sendBootcampReport(any())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.registerBootcampWithCapacities(bootcamp, capacityIds))
                .expectNext(TechnicalMessage.BOOTCAMP_CREATED.name())
                .verifyComplete();
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

    @Test
    void validateAndReturnIds_success() {
        Long id1 = 1L;
        Long id2 = 2L;

        Bootcamp b1 = new Bootcamp(id1, "Bootcamp1", "Desc1", LocalDate.of(2024, 1, 1), 10);
        Bootcamp b2 = new Bootcamp(id2, "Bootcamp2", "Desc2", LocalDate.of(2024, 2, 1), 12);

        when(persistencePort.existsById(id1)).thenReturn(Mono.just(true));
        when(persistencePort.existsById(id2)).thenReturn(Mono.just(true));
        when(persistencePort.findById(id1)).thenReturn(Mono.just(b1));
        when(persistencePort.findById(id2)).thenReturn(Mono.just(b2));

        StepVerifier.create(useCase.validateAndReturnIds(List.of(id1, id2)))
                .expectNext(List.of(id1, id2))
                .verifyComplete();
    }

    @Test
    void validateAndReturnIds_bootcampNotFound() {
        Long id1 = 1L;
        when(persistencePort.existsById(id1)).thenReturn(Mono.just(false));

        StepVerifier.create(useCase.validateAndReturnIds(List.of(id1)))
                .expectErrorSatisfies(e -> {
                    assert e instanceof BusinessException;
                    assert ((BusinessException) e).getTechnicalMessage() == TechnicalMessage.BOOTCAMP_NOT_FOUND;
                })
                .verify();
    }

    @Test
    void validateAndReturnIds_duplicateDateOrDuration() {
        Long id1 = 1L;
        Long id2 = 2L;

        Bootcamp b1 = new Bootcamp(id1, "Bootcamp1", "Desc1", LocalDate.of(2024, 1, 1), 10);
        Bootcamp b2 = new Bootcamp(id2, "Bootcamp2", "Desc2", LocalDate.of(2024, 1, 1), 15); // misma fecha

        when(persistencePort.existsById(id1)).thenReturn(Mono.just(true));
        when(persistencePort.existsById(id2)).thenReturn(Mono.just(true));
        when(persistencePort.findById(id1)).thenReturn(Mono.just(b1));
        when(persistencePort.findById(id2)).thenReturn(Mono.just(b2));

        StepVerifier.create(useCase.validateAndReturnIds(List.of(id1, id2)))
                .expectErrorSatisfies(e -> {
                    assert e instanceof BusinessException;
                    assert ((BusinessException) e).getTechnicalMessage() == TechnicalMessage.BOOTCAMP_DUPLICATE_DATE_DURATION;
                })
                .verify();
    }

    @Test
    void findBootcampWithMostPersons_success() {
        BootcampWithCapacitiesAndPersons mockResponse = mock(BootcampWithCapacitiesAndPersons.class);
        when(queryPort.findBootcampWithMostPersons()).thenReturn(Mono.just(mockResponse));

        StepVerifier.create(useCase.findBootcampWithMostPersons())
                .expectNext(mockResponse)
                .verifyComplete();
    }
}