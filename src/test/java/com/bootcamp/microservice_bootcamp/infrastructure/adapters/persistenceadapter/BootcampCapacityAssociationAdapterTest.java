package com.bootcamp.microservice_bootcamp.infrastructure.adapters.persistenceadapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BootcampCapacityAssociationAdapterTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WebClient webClient;

    private BootcampCapacityAssociationAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new BootcampCapacityAssociationAdapter(webClient);
    }

    @Test
    void associateCapacityToBootcamp_shouldReturnTrueOnSuccess() {
        Long bootcampId = 1L;
        List<Long> capacityIds = List.of(10L, 20L);

        WebClient.RequestBodyUriSpec uriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec bodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec<?> headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri("http://localhost:8081/capacity/bootcamp/associate")).thenReturn(bodySpec);
        when(bodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(bodySpec);
        when(bodySpec.bodyValue(Map.of("bootcampId", bootcampId, "capacityIds", capacityIds)))
                .thenReturn((WebClient.RequestHeadersSpec) headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("ok"));

        StepVerifier.create(adapter.associateCapacityToBootcamp(bootcampId, capacityIds))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void associateCapacityToBootcamp_shouldReturnFalseOnError() {
        Long bootcampId = 1L;
        List<Long> capacityIds = List.of(10L);

        WebClient.RequestBodyUriSpec uriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec bodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec<?> headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri("http://localhost:8081/capacity/bootcamp/associate")).thenReturn(bodySpec);
        when(bodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(bodySpec);
        when(bodySpec.bodyValue(Map.of("bootcampId", bootcampId, "capacityIds", capacityIds)))
                .thenReturn((WebClient.RequestHeadersSpec) headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(new RuntimeException("Error")));

        StepVerifier.create(adapter.associateCapacityToBootcamp(bootcampId, capacityIds))
                .expectNext(false)
                .verifyComplete();
    }

    @SuppressWarnings("rawtypes")
    @Test
    void deleteCapacitiesByBootcampId_shouldCompleteSuccessfully() {
        Long bootcampId = 1L;

        WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.delete()).thenReturn(uriSpec);
        when(uriSpec.uri("http://localhost:8081/capacity/bootcamp/{bootcampId}/exclusive-delete", bootcampId))
                .thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.deleteCapacitiesByBootcampId(bootcampId))
                .verifyComplete();
    }
}