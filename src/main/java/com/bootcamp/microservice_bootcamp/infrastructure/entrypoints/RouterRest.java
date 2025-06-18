package com.bootcamp.microservice_bootcamp.infrastructure.entrypoints;

import com.bootcamp.microservice_bootcamp.infrastructure.entrypoints.dto.BootcampDTO;
import com.bootcamp.microservice_bootcamp.infrastructure.entrypoints.handler.BootcampHandlerImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {

    @Bean
    @RouterOperation(
            path = "/bootcamp",
            method = {RequestMethod.POST},
            beanClass = BootcampHandlerImpl.class,
            beanMethod = "createBootcamp",
            operation = @Operation(
                    operationId = "createBootcamp",
                    summary = "Creates a new bootcamp",
                    requestBody = @RequestBody(
                            required = true,
                            content = @Content(schema = @Schema(implementation = BootcampDTO.class))
                    ),
                    responses = {
                            @ApiResponse(
                                    responseCode = "201",
                                    description = "Bootcamp created successfully",
                                    content = @Content(schema = @Schema(implementation = BootcampDTO.class))
                            ),
                            @ApiResponse(
                                    responseCode = "400",
                                    description = "Invalid request"
                            )
                    }
            )
    )
    public RouterFunction<ServerResponse> routerFunction(BootcampHandlerImpl bootcampHandler) {
        return route(POST("/bootcamp"), bootcampHandler::createBootcamp)
                .andRoute(GET("/bootcamp/list"), bootcampHandler::listBootcamps)
                .andRoute(DELETE("/bootcamp/{bootcampId}"), bootcampHandler::deleteBootcamp);

    }
}