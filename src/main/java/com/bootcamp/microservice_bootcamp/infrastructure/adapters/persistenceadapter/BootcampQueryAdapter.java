package com.bootcamp.microservice_bootcamp.infrastructure.adapters.persistenceadapter;

import com.bootcamp.microservice_bootcamp.domain.model.*;
import com.bootcamp.microservice_bootcamp.domain.spi.IBootcampQueryPort;
import com.bootcamp.microservice_bootcamp.infrastructure.adapters.persistenceadapter.entity.BootcampEntity;
import com.bootcamp.microservice_bootcamp.infrastructure.adapters.persistenceadapter.mapper.IBootcampEntityMapper;
import com.bootcamp.microservice_bootcamp.infrastructure.adapters.persistenceadapter.repository.IBootcampRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@Component
@RequiredArgsConstructor
public class BootcampQueryAdapter implements IBootcampQueryPort {

    private final IBootcampRepository bootcampRepository;
    private final IBootcampEntityMapper bootcampEntityMapper;
    private final WebClient webClient;

    @Value("${capacity.service.url:http://localhost:8081}")
    private String capacityServiceUrl;

    @Value("${person.service.url:http://localhost:8083}")
    private String personServiceUrl;

    @Override
    public Flux<BootcampWithCapacitiesAndTechnologies> listBootcampsPagedAndSorted(
            int page, int size, String sortBy, String direction) {

        if ("capacityCount".equalsIgnoreCase(sortBy)) {
            return getBootcampRelationCounts()
                    .flatMapMany(bootcampIdToCount -> findAndSortBootcampsByRelationCount(bootcampIdToCount, page, size, direction)
                            .flatMap(this::enrichBootcampWithCapacitiesAndTechnologies));
        } else {
            return bootcampRepository.findAll()
                    .collectList()
                    .flatMapMany(entities -> sortAndPaginateBootcamps(entities, page, size, direction))
                    .flatMap(this::enrichBootcampWithCapacitiesAndTechnologies);
        }
    }

    @Override
    public Mono<BootcampWithCapacitiesAndPersons> findBootcampWithMostPersons() {
        return bootcampRepository.findAll()
                .collectList()
                .flatMap(this::findBootcampWithMostPersonsFromList);
    }

    private Mono<BootcampWithCapacitiesAndPersons> findBootcampWithMostPersonsFromList(List<BootcampEntity> entities) {
        return Flux.fromIterable(entities)
                .flatMap(this::mapToBootcampWithPersonsAndCapacities)
                .collectList()
                .map(list -> list.stream()
                        .max(Comparator.comparing(b -> b.registeredPersons().size()))
                        .orElseThrow(() -> new RuntimeException("No bootcamps found")));
    }

    private Mono<BootcampWithCapacitiesAndPersons> mapToBootcampWithPersonsAndCapacities(BootcampEntity entity) {
        Mono<List<Person>> personsMono = fetchRegisteredPersons(entity.getId());
        Mono<List<CapacityWithTechnologies>> capacitiesMono = fetchCapacitiesWithTechnologies(entity.getId());

        return Mono.zip(personsMono, capacitiesMono)
                .map(tuple -> new BootcampWithCapacitiesAndPersons(
                        entity.getId(),
                        entity.getName(),
                        entity.getDescription(),
                        entity.getReleaseDate(),
                        entity.getDuration(),
                        tuple.getT1(),
                        tuple.getT2()
                ));
    }

    private Mono<List<Person>> fetchRegisteredPersons(Long bootcampId) {
        return webClient.get()
                .uri(personServiceUrl + "/person/bootcamp/{id}/info", bootcampId)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(map -> new Person(map.get("name").toString(), map.get("email").toString()))
                .collectList()
                .onErrorResume(e -> Mono.just(List.of()));
    }

    private Mono<List<CapacityWithTechnologies>> fetchCapacitiesWithTechnologies(Long bootcampId) {
        return webClient.get()
                .uri(capacityServiceUrl + "/capacity/bootcamp/capacities-technologies?bootcampId={id}", bootcampId)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<Map<String, Object>>() {})
                .collectList()
                .map(this::mapToCapacitiesWithTechnologies);
    }

    private Mono<Map<Long, Integer>> getBootcampRelationCounts() {
        return webClient.get()
                .uri(capacityServiceUrl + "/capacity/bootcamp/relation-counts")
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<Map<String, Object>>() {})
                .collectList()
                .map(countsList -> {
                    Map<Long, Integer> bootcampIdToCount = new HashMap<>();
                    for (Map<String, Object> m : countsList) {
                        Long bootcampId = Long.valueOf(m.get("bootcampId").toString());
                        Integer relationCount = Integer.valueOf(m.get("relationCount").toString());
                        bootcampIdToCount.put(bootcampId, relationCount);
                    }
                    return bootcampIdToCount;
                });
    }

    private Flux<BootcampEntity> findAndSortBootcampsByRelationCount(Map<Long, Integer> bootcampIdToCount, int page, int size, String direction) {
        List<Long> bootcampIds = new ArrayList<>(bootcampIdToCount.keySet());
        return bootcampRepository.findAllById(bootcampIds)
                .collectList()
                .flatMapMany(entities -> {
                    entities.sort((a, b) -> {
                        int cmp = Integer.compare(
                                bootcampIdToCount.getOrDefault(a.getId(), 0),
                                bootcampIdToCount.getOrDefault(b.getId(), 0)
                        );
                        if ("desc".equalsIgnoreCase(direction)) cmp = -cmp;
                        return cmp;
                    });
                    return Flux.fromIterable(paginateList(entities, page, size));
                });
    }

    private Flux<BootcampEntity> sortAndPaginateBootcamps(List<BootcampEntity> entities, int page, int size, String direction) {
        entities.sort((a, b) -> {
            int cmp = a.getName().compareToIgnoreCase(b.getName());
            if ("desc".equalsIgnoreCase(direction)) cmp = -cmp;
            return cmp;
        });
        return Flux.fromIterable(paginateList(entities, page, size));
    }

    private List<BootcampEntity> paginateList(List<BootcampEntity> entities, int page, int size) {
        int from = Math.min(page * size, entities.size());
        int to = Math.min(from + size, entities.size());
        return entities.subList(from, to);
    }

    private Mono<BootcampWithCapacitiesAndTechnologies> enrichBootcampWithCapacitiesAndTechnologies(BootcampEntity entity) {
        return fetchCapacitiesWithTechnologies(entity.getId())
                .map(capacities -> new BootcampWithCapacitiesAndTechnologies(
                        entity.getId(),
                        entity.getName(),
                        entity.getDescription(),
                        entity.getReleaseDate(),
                        entity.getDuration(),
                        capacities
                ));
    }

    private List<CapacityWithTechnologies> mapToCapacitiesWithTechnologies(List<Map<String, Object>> capacityList) {
        return capacityList.stream()
                .map(x -> {
                    Long capacityId = Long.valueOf(x.get("id").toString());
                    String capacityName = (String) x.get("name");

                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> techs = (List<Map<String, Object>>) x.getOrDefault("technologies", Collections.emptyList());

                    List<TechnologySummary> technologies = techs.stream()
                            .map(t -> new TechnologySummary(
                                    Long.valueOf(t.get("id").toString()),
                                    t.get("name").toString()))
                            .toList();

                    return new CapacityWithTechnologies(capacityId, capacityName, technologies);
                })
                .toList();
    }
}
