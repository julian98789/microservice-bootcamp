package com.bootcamp.microservice_bootcamp.infrastructure.entrypoints;

import com.bootcamp.microservice_bootcamp.infrastructure.entrypoints.handler.BootcampHandlerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class RouterRestTest {

    @Mock
    private BootcampHandlerImpl bootcampHandler;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        RouterRest routerRest = new RouterRest();
        webTestClient = WebTestClient.bindToRouterFunction(
                routerRest.routerFunction(bootcampHandler)
        ).build();

        lenient().when(bootcampHandler.createBootcamp(any())).thenReturn(Mono.just(ServerResponse.created(null).build().block()));
        lenient().when(bootcampHandler.listBootcamps(any())).thenReturn(Mono.just(ServerResponse.ok().build().block()));
        lenient().when(bootcampHandler.deleteBootcamp(any())).thenReturn(Mono.just(ServerResponse.noContent().build().block()));
        lenient().when(bootcampHandler.validateBootcampIds(any())).thenReturn(Mono.just(ServerResponse.ok().build().block()));
    }

    @Test
    void testCreateBootcampRoute() {
        webTestClient.post().uri("/bootcamp")
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void testListBootcampsRoute() {
        webTestClient.get().uri("/bootcamp/list")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testDeleteBootcampRoute() {
        webTestClient.delete().uri("/bootcamp/1")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void testValidateBootcampIdsRoute() {
        webTestClient.post().uri("/bootcamp/validate-list")
                .exchange()
                .expectStatus().isOk();
    }
}