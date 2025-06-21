package com.bootcamp.microservice_bootcamp.infrastructure.adapters.persistenceadapter;


import com.bootcamp.microservice_bootcamp.domain.spi.IBootcampCapacityAssociationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class BootcampCapacityAssociationAdapter implements IBootcampCapacityAssociationPort {

    private final WebClient webClient;


    @Override
    public Mono<Boolean> associateCapacityToBootcamp(Long capacityId, List<Long> technologyIds) {
        Map<String, Object> body = Map.of(
                "bootcampId", capacityId,
                "capacityIds", technologyIds
        );
        return webClient.post()
                .uri( "http://localhost:8081/capacity/bootcamp/associate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> true)
                .onErrorResume(e -> Mono.just(false));
    }

    @Override
    public Mono<Void> deleteCapacitiesByBootcampId(Long bootcampId) {
        return webClient.delete()
                .uri("http://localhost:8081/capacity/bootcamp/{bootcampId}/exclusive-delete", bootcampId)
                .retrieve()
                .bodyToMono(Void.class);
    }
}