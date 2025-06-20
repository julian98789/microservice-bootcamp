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
        Mono<Integer> capacityCountMono = fetchCapacityCount(reportData.bootcampId());
        Mono<Integer> technologyCountMono = fetchTechnologyCount(reportData.bootcampId());
        Mono<Integer> personCountMono = fetchPersonCount(reportData.bootcampId());

        return Mono.zip(capacityCountMono, technologyCountMono, personCountMono)
                .flatMap(tuple -> {
                    BootcampReportData complete = enrichReportData(reportData,
                            tuple.getT1(), tuple.getT2(), tuple.getT3());
                    return sendReportToService(complete);
                });
    }

    private Mono<Integer> fetchCapacityCount(Long bootcampId) {
        return webClient.get()
                .uri(capacityServiceUrl + "/capacity/bootcamp/summary?bootcampId={id}", bootcampId)
                .retrieve()
                .bodyToMono(CapacitySummaryResponse.class)
                .map(CapacitySummaryResponse::capacityCount)
                .onErrorResume(e -> Mono.just(0));
    }

    private Mono<Integer> fetchTechnologyCount(Long bootcampId) {
        return webClient.get()
                .uri(capacityServiceUrl + "/capacity/bootcamp/summary?bootcampId={id}", bootcampId)
                .retrieve()
                .bodyToMono(CapacitySummaryResponse.class)
                .map(CapacitySummaryResponse::totalTechnologyCount)
                .onErrorResume(e -> Mono.just(0));
    }

    private Mono<Integer> fetchPersonCount(Long bootcampId) {
        return webClient.get()
                .uri(personServiceUrl + "/person/bootcamp/{id}/count", bootcampId)
                .retrieve()
                .bodyToMono(PersonCountResponse.class)
                .map(PersonCountResponse::personCount)
                .onErrorResume(e -> Mono.just(0));
    }

    private BootcampReportData enrichReportData(BootcampReportData baseData,
                                                int capacityCount,
                                                int technologyCount,
                                                int personCount) {
        return BootcampReportData.builder()
                .bootcampId(baseData.bootcampId())
                .name(baseData.name())
                .description(baseData.description())
                .releaseDate(baseData.releaseDate())
                .duration(baseData.duration())
                .registeredPersonCount(personCount)
                .capacityCount(capacityCount)
                .totalTechnologyCount(technologyCount)
                .build();
    }

    private Mono<Void> sendReportToService(BootcampReportData reportData) {
        return webClient.post()
                .uri(reportServiceUrl + "/report/bootcamp")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(reportData)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(e -> Mono.empty());
    }

    private record CapacitySummaryResponse(int capacityCount, int totalTechnologyCount) {}
    private record PersonCountResponse(int personCount) {}
}