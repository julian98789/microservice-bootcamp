package com.bootcamp.microservice_bootcamp.infrastructure.entrypoints.handler;

import com.bootcamp.microservice_bootcamp.domain.api.IBootcampServicePort;
import com.bootcamp.microservice_bootcamp.domain.enums.TechnicalMessage;
import com.bootcamp.microservice_bootcamp.domain.exceptions.BusinessException;
import com.bootcamp.microservice_bootcamp.domain.exceptions.TechnicalException;
import com.bootcamp.microservice_bootcamp.domain.model.Bootcamp;
import com.bootcamp.microservice_bootcamp.infrastructure.entrypoints.dto.BootcampDTO;
import com.bootcamp.microservice_bootcamp.infrastructure.entrypoints.mapper.IBootcampMapper;
import com.bootcamp.microservice_bootcamp.infrastructure.entrypoints.mapper.IBootcampWithCapacitiesAndTechnologiesMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class BootcampHandlerImplTest {

    @Mock
    private IBootcampServicePort bootcampServicePort;
    @Mock
    private IBootcampMapper bootcampMapper;
    @Mock
    private IBootcampWithCapacitiesAndTechnologiesMapper bootcampWithCapacitiesAndTechnologiesMapper;

    private BootcampHandlerImpl handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new BootcampHandlerImpl(bootcampServicePort,
                bootcampMapper,
                bootcampWithCapacitiesAndTechnologiesMapper);
    }

    @Test
    void createBootcamp_success() {
        ServerRequest request = mock(ServerRequest.class);

        BootcampDTO dto = new BootcampDTO(
                1L,
                "Bootcamp Java",
                "Intro Spring Boot",
                List.of(1L, 2L),
                LocalDate.of(2025, 6, 19),
                10
        );

        Bootcamp mappedBootcamp = new Bootcamp(
                null,
                dto.getName(),
                dto.getDescription(),
                dto.getReleaseDate(),
                dto.getDuration()
        );

        when(request.bodyToMono(BootcampDTO.class)).thenReturn(Mono.just(dto));
        when(bootcampMapper.bootcampDTOToBootcamp(dto)).thenReturn(mappedBootcamp);
        when(bootcampServicePort.registerBootcampWithCapacities(mappedBootcamp, dto.getCapacityIds()))
                .thenReturn(Mono.just("Bootcamp creado exitosamente"));

        ServerResponse response = handler.createBootcamp(request).block();
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.statusCode());
    }

    @Test
    void createBootcamp_businessException() {
        ServerRequest request = mock(ServerRequest.class);
        BootcampDTO dto = new BootcampDTO(
                1L,
                "Backend Bootcamp",
                "Spring Boot and Java",
                List.of(1L, 2L),
                LocalDate.of(2025, 6, 1),
                8
        );

        Bootcamp mappedBootcamp = new Bootcamp(null, dto.getName(), dto.getDescription(), dto.getReleaseDate(), dto.getDuration());

        when(request.bodyToMono(BootcampDTO.class)).thenReturn(Mono.just(dto));
        when(bootcampMapper.bootcampDTOToBootcamp(dto)).thenReturn(mappedBootcamp);
        when(bootcampServicePort.registerBootcampWithCapacities(mappedBootcamp, dto.getCapacityIds()))
                .thenReturn(Mono.error(new BusinessException(TechnicalMessage.INTERNAL_ERROR)));

        ServerResponse response = handler.createBootcamp(request).block();
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode());
    }

    @Test
    void createBootcamp_technicalException() {
        ServerRequest request = mock(ServerRequest.class);
        BootcampDTO dto = new BootcampDTO(
                1L,
                "Backend Bootcamp",
                "Spring Boot and Java",
                List.of(1L, 2L),
                LocalDate.of(2025, 6, 1),
                8
        );

        Bootcamp mappedBootcamp = new Bootcamp(null, dto.getName(), dto.getDescription(), dto.getReleaseDate(),
                dto.getDuration());

        when(request.bodyToMono(BootcampDTO.class)).thenReturn(Mono.just(dto));
        when(bootcampMapper.bootcampDTOToBootcamp(dto)).thenReturn(mappedBootcamp);
        when(bootcampServicePort.registerBootcampWithCapacities(mappedBootcamp, dto.getCapacityIds()))
                .thenReturn(Mono.error(new TechnicalException(TechnicalMessage.INTERNAL_ERROR)));

        ServerResponse response = handler.createBootcamp(request).block();
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode());
    }

    @Test
    void createBootcamp_unexpectedException() {
        ServerRequest request = mock(ServerRequest.class);
        BootcampDTO dto = new BootcampDTO(
                1L,
                "Backend Bootcamp",
                "Spring Boot and Java",
                List.of(1L, 2L),
                LocalDate.of(2025, 6, 1),
                8
        );

        Bootcamp mappedBootcamp = new Bootcamp(null, dto.getName(), dto.getDescription(), dto.getReleaseDate(), dto.getDuration());

        when(request.bodyToMono(BootcampDTO.class)).thenReturn(Mono.just(dto));
        when(bootcampMapper.bootcampDTOToBootcamp(dto)).thenReturn(mappedBootcamp);
        when(bootcampServicePort.registerBootcampWithCapacities(mappedBootcamp,
                dto.getCapacityIds())).thenReturn(Mono.error(new RuntimeException("Unexpected")));

        ServerResponse response = handler.createBootcamp(request).block();
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode());
    }

    @Test
    void listBootcamps_success() {
        ServerRequest request = mock(ServerRequest.class);
        when(request.queryParam("page")).thenReturn(java.util.Optional.of("0"));
        when(request.queryParam("size")).thenReturn(java.util.Optional.of("10"));
        when(request.queryParam("sortBy")).thenReturn(java.util.Optional.of("name"));
        when(request.queryParam("direction")).thenReturn(java.util.Optional.of("asc"));

        when(bootcampServicePort.listBootcampsPagedAndSorted(0, 10, "name", "asc"))
                .thenReturn(Flux.empty());

        ServerResponse response = handler.listBootcamps(request).block();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.statusCode());
    }

    @Test
    void deleteBootcamp_success() {
        ServerRequest request = mock(ServerRequest.class);
        when(request.pathVariable("bootcampId")).thenReturn("1");

        when(bootcampServicePort.deleteBootcampAndCascade(1L))
                .thenReturn(Mono.empty());

        ServerResponse response = handler.deleteBootcamp(request).block();
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.statusCode());
    }

    @Test
    void deleteBootcamp_unexpectedError() {
        ServerRequest request = mock(ServerRequest.class);
        when(request.pathVariable("bootcampId")).thenReturn("1");

        when(bootcampServicePort.deleteBootcampAndCascade(1L))
                .thenReturn(Mono.error(new RuntimeException("Unexpected failure")));

        ServerResponse response = handler.deleteBootcamp(request).block();
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode());
    }

    @Test
    void validateBootcampIds_success() {
        ServerRequest request = mock(ServerRequest.class);
        List<Long> ids = List.of(1L, 2L);
        when(request.bodyToMono(List.class)).thenReturn(Mono.just(List.of(1, 2)));
        when(bootcampServicePort.validateAndReturnIds(ids)).thenReturn(Mono.just(ids));

        ServerResponse response = handler.validateBootcampIds(request).block();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.statusCode());
    }

    @Test
    void validateBootcampIds_businessException() {
        ServerRequest request = mock(ServerRequest.class);
        List<Long> ids = List.of(1L, 2L);
        BusinessException exception = new BusinessException(TechnicalMessage.BOOTCAMP_NOT_FOUND);

        when(request.bodyToMono(List.class)).thenReturn(Mono.just(List.of(1, 2)));
        when(bootcampServicePort.validateAndReturnIds(ids)).thenReturn(Mono.error(exception));

        ServerResponse response = handler.validateBootcampIds(request).block();
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode());
    }

    @Test
    void validateBootcampIds_technicalException() {
        ServerRequest request = mock(ServerRequest.class);
        List<Long> ids = List.of(1L, 2L);
        TechnicalException exception = new TechnicalException(TechnicalMessage.INTERNAL_ERROR);

        when(request.bodyToMono(List.class)).thenReturn(Mono.just(List.of(1, 2)));
        when(bootcampServicePort.validateAndReturnIds(ids)).thenReturn(Mono.error(exception));

        ServerResponse response = handler.validateBootcampIds(request).block();
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode());
    }

    @Test
    void validateBootcampIds_unexpectedException() {
        ServerRequest request = mock(ServerRequest.class);
        List<Long> ids = List.of(1L, 2L);
        RuntimeException exception = new RuntimeException("Unexpected error");

        when(request.bodyToMono(List.class)).thenReturn(Mono.just(List.of(1, 2)));
        when(bootcampServicePort.validateAndReturnIds(ids)).thenReturn(Mono.error(exception));

        ServerResponse response = handler.validateBootcampIds(request).block();
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode());
    }
}