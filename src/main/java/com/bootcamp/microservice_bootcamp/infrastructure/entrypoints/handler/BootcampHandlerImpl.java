package com.bootcamp.microservice_bootcamp.infrastructure.entrypoints.handler;



import com.bootcamp.microservice_bootcamp.domain.api.IBootcampServicePort;
import com.bootcamp.microservice_bootcamp.domain.enums.TechnicalMessage;
import com.bootcamp.microservice_bootcamp.domain.exceptions.BusinessException;
import com.bootcamp.microservice_bootcamp.domain.exceptions.TechnicalException;
import com.bootcamp.microservice_bootcamp.infrastructure.entrypoints.dto.BootcampDTO;
import com.bootcamp.microservice_bootcamp.infrastructure.entrypoints.mapper.IBootcampMapper;
import com.bootcamp.microservice_bootcamp.infrastructure.entrypoints.mapper.IBootcampWithCapacitiesAndTechnologiesMapper;
import com.bootcamp.microservice_bootcamp.infrastructure.entrypoints.util.APIResponse;
import com.bootcamp.microservice_bootcamp.infrastructure.entrypoints.util.ErrorDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

import static com.bootcamp.microservice_bootcamp.infrastructure.entrypoints.util.Constants.BOOTCAMP_ERROR;


@Component
@RequiredArgsConstructor
@Slf4j
public class BootcampHandlerImpl {

    private final IBootcampServicePort bootcampServicePort;
    private final IBootcampMapper bootcampMapper;
    private final IBootcampWithCapacitiesAndTechnologiesMapper bootcampWithCapTechMapper;


    public Mono<ServerResponse> createBootcamp(ServerRequest request) {
        return request.bodyToMono(BootcampDTO.class)
                .flatMap(dto -> {
                    return bootcampServicePort.registerBootcampWithCapacities(
                            bootcampMapper.bootcampDTOToBootcamp(dto),
                            dto.getCapacityIds());
                })
                .flatMap(msg -> ServerResponse
                        .status(HttpStatus.CREATED)
                        .bodyValue(msg))
                .doOnError(ex -> log.error(BOOTCAMP_ERROR, ex))
                .onErrorResume(BusinessException.class, ex -> buildErrorResponse(
                        HttpStatus.BAD_REQUEST,
                        ex.getTechnicalMessage(),
                        List.of(ErrorDTO.builder()
                                .code(ex.getTechnicalMessage().getCode())
                                .message(ex.getTechnicalMessage().getMessage())
                                .param(ex.getTechnicalMessage().getParam())
                                .build())))
                .onErrorResume(TechnicalException.class, ex -> buildErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        TechnicalMessage.INTERNAL_ERROR,
                        List.of(ErrorDTO.builder()
                                .code(ex.getTechnicalMessage().getCode())
                                .message(ex.getTechnicalMessage().getMessage())
                                .param(ex.getTechnicalMessage().getParam())
                                .build())))
                .onErrorResume(ex -> {
                    log.error("Unexpected error occurred", ex);
                    return buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            TechnicalMessage.INTERNAL_ERROR,
                            List.of(ErrorDTO.builder()
                                    .code(TechnicalMessage.INTERNAL_ERROR.getCode())
                                    .message(TechnicalMessage.INTERNAL_ERROR.getMessage())
                                    .build()));
                });
    }

    public Mono<ServerResponse> listBootcamps(ServerRequest request) {
        int page = Integer.parseInt(request.queryParam("page").orElse("0"));
        int size = Integer.parseInt(request.queryParam("size").orElse("10"));
        String sortBy = request.queryParam("sortBy").orElse("name");
        String direction = request.queryParam("direction").orElse("asc");

        return bootcampServicePort
                .listBootcampsPagedAndSorted(page, size, sortBy, direction)
                .map(bootcampWithCapTechMapper::toDTO)
                .collectList()
                .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }

    public Mono<ServerResponse> deleteBootcamp(ServerRequest request) {
        Long bootcampId = Long.parseLong(request.pathVariable("bootcampId"));
        return bootcampServicePort.deleteBootcampAndCascade(bootcampId)
                .then(ServerResponse.noContent().build())
                .onErrorResume(ex -> {
                    log.error("Unexpected error occurred", ex);
                    return buildErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            TechnicalMessage.INTERNAL_ERROR,
                            List.of(ErrorDTO.builder()
                                    .code(TechnicalMessage.INTERNAL_ERROR.getCode())
                                    .message("Error deleting bootcamp: " + ex.getMessage())
                                    .build())
                    );
                });
    }

    public Mono<ServerResponse> validateBootcampIds(ServerRequest request) {
        return request.bodyToMono(List.class)
                .flatMap(idsRaw -> {
                    List<Long> ids = ((List<?>) idsRaw).stream()
                            .map(Object::toString)
                            .map(Long::valueOf)
                            .toList();
                    return bootcampServicePort.validateAndReturnIds(ids);
                })
                .flatMap(validIds -> ServerResponse.ok().bodyValue(validIds))
                .onErrorResume(BusinessException.class, ex -> buildErrorResponse(
                        HttpStatus.BAD_REQUEST,
                        ex.getTechnicalMessage(),
                        List.of(ErrorDTO.builder()
                                .code(ex.getTechnicalMessage().getCode())
                                .message(ex.getMessage())
                                .param(ex.getTechnicalMessage().getParam())
                                .build())))
                .onErrorResume(TechnicalException.class, ex -> buildErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        TechnicalMessage.INTERNAL_ERROR,
                        List.of(ErrorDTO.builder()
                                .code(ex.getTechnicalMessage().getCode())
                                .message(ex.getMessage())
                                .param(ex.getTechnicalMessage().getParam())
                                .build())))
                .onErrorResume(ex -> buildErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        TechnicalMessage.INTERNAL_ERROR,
                        List.of(ErrorDTO.builder()
                                .code(TechnicalMessage.INTERNAL_ERROR.getCode())
                                .message(ex.getMessage())
                                .build())));
    }



    private Mono<ServerResponse> buildErrorResponse(HttpStatus httpStatus,  TechnicalMessage error,
                                                    List<ErrorDTO> errors) {
        return Mono.defer(() -> {
            APIResponse apiErrorResponse = APIResponse
                    .builder()
                    .code(error.getCode())
                    .message(error.getMessage())
                    .date(Instant.now().toString())
                    .errors(errors)
                    .build();
            return ServerResponse.status(httpStatus)
                    .bodyValue(apiErrorResponse);
        });
    }
}
