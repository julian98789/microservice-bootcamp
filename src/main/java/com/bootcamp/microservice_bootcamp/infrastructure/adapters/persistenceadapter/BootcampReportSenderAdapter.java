package com.bootcamp.microservice_bootcamp.infrastructure.adapters.persistenceadapter;

import com.bootcamp.microservice_bootcamp.domain.model.BootcampReportData;
import com.bootcamp.microservice_bootcamp.domain.spi.IBootcampReportSenderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class BootcampReportSenderAdapter implements IBootcampReportSenderPort {

    private final WebClient webClient;

    @Value("${capacity.service.url:http://localhost:8081}")
    private String capacityServiceUrl;

    @Value("${person.service.url:http://localhost:8083}")
    private String personServiceUrl;

    @Value("${report.service.url:http://localhost:8084}")
    private String reportServiceUrl;

    @Override
    public Mono<Void> sendBootcampReport(BootcampReportData reportData) {
        Mono<Integer> capacityCountMono = webClient.get()
                .uri(capacityServiceUrl + "/capacity/bootcamp/summary?bootcampId={id}", reportData.bootcampId())
                .retrieve()
                .bodyToMono(CapacitySummaryResponse.class)
                .map(CapacitySummaryResponse::capacityCount)
                .onErrorResume(e -> Mono.just(0));

        Mono<Integer> technologyCountMono = webClient.get()
                .uri(capacityServiceUrl + "/capacity/bootcamp/summary?bootcampId={id}", reportData.bootcampId())
                .retrieve()
                .bodyToMono(CapacitySummaryResponse.class)
                .map(CapacitySummaryResponse::totalTechnologyCount)
                .onErrorResume(e -> Mono.just(0));

        Mono<Integer> personCountMono = webClient.get()
                .uri(personServiceUrl + "/person/bootcamp/{id}/count", reportData.bootcampId())
                .retrieve()
                .bodyToMono(PersonCountResponse.class)
                .map(PersonCountResponse::personCount)
                .onErrorResume(e -> Mono.just(0));

        return Mono.zip(capacityCountMono, technologyCountMono, personCountMono)
                .flatMap(tuple -> {
                    int capacityCount = tuple.getT1();
                    int techCount = tuple.getT2();
                    int personCount = tuple.getT3();

                    BootcampReportData complete = BootcampReportData.builder()
                            .bootcampId(reportData.bootcampId())
                            .name(reportData.name())
                            .description(reportData.description())
                            .releaseDate(reportData.releaseDate())
                            .duration(reportData.duration())
                            .registeredPersonCount(personCount)
                            .capacityCount(capacityCount)
                            .totalTechnologyCount(techCount)
                            .build();

                    return webClient.post()
                            .uri(reportServiceUrl + "/report/bootcamp")
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(complete)
                            .retrieve()
                            .bodyToMono(Void.class)
                            .onErrorResume(e -> Mono.empty());
                });
    }

    private record CapacitySummaryResponse(int capacityCount, int totalTechnologyCount) {}
    private record PersonCountResponse(int personCount) {}
}