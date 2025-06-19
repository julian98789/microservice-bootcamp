package com.bootcamp.microservice_bootcamp.infrastructure.entrypoints;

import com.bootcamp.microservice_bootcamp.infrastructure.entrypoints.dto.BootcampDTO;
import com.bootcamp.microservice_bootcamp.infrastructure.entrypoints.dto.BootcampWithCapacitiesAndTechnologiesDTO;
import com.bootcamp.microservice_bootcamp.infrastructure.entrypoints.handler.BootcampHandlerImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
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
    @RouterOperations({
            @RouterOperation(
                    path = "/bootcamp",
                    method = RequestMethod.POST,
                    beanClass = BootcampHandlerImpl.class,
                    beanMethod = "createBootcamp",
                    operation = @Operation(
                            operationId = "createBootcamp",
                            summary = "Creates a new bootcamp with associated capacities",
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(
                                            schema = @Schema(implementation = BootcampDTO.class),
                                            examples = @ExampleObject(value = """
                                                {
                                                  "name": "Backend Bootcamp",
                                                  "description": "Spring Boot & Java Fundamentals",
                                                  "capacityIds": [1, 2, 3]
                                                }
                                                """)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "201",
                                            description = "Bootcamp created successfully",
                                            content = @Content(schema = @Schema(implementation = String.class),
                                                    examples = @ExampleObject(value = "Bootcamp successfully registered"))
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Invalid request"),
                                    @ApiResponse(responseCode = "500", description = "Internal server error")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/bootcamp/list",
                    method = RequestMethod.GET,
                    beanClass = BootcampHandlerImpl.class,
                    beanMethod = "listBootcamps",
                    operation = @Operation(
                            operationId = "listBootcamps",
                            summary = "Returns a paginated and sorted list of bootcamps with their capacities and technologies",
                            parameters = {
                                    @Parameter(name = "page", in = ParameterIn.QUERY, example = "0"),
                                    @Parameter(name = "size", in = ParameterIn.QUERY, example = "10"),
                                    @Parameter(name = "sortBy", in = ParameterIn.QUERY, example = "name"),
                                    @Parameter(name = "direction", in = ParameterIn.QUERY, example = "asc")
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "List of bootcamps",
                                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = BootcampWithCapacitiesAndTechnologiesDTO.class)))
                                    ),
                                    @ApiResponse(responseCode = "500", description = "Internal server error")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/bootcamp/{bootcampId}",
                    method = RequestMethod.DELETE,
                    beanClass = BootcampHandlerImpl.class,
                    beanMethod = "deleteBootcamp",
                    operation = @Operation(
                            operationId = "deleteBootcamp",
                            summary = "Deletes a bootcamp by ID (with cascading)",
                            parameters = {
                                    @Parameter(name = "bootcampId", in = ParameterIn.PATH, required = true, example = "1")
                            },
                            responses = {
                                    @ApiResponse(responseCode = "204", description = "Bootcamp deleted"),
                                    @ApiResponse(responseCode = "500", description = "Error while deleting bootcamp")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/bootcamp/validate-list",
                    method = RequestMethod.POST,
                    beanClass = BootcampHandlerImpl.class,
                    beanMethod = "validateBootcampIds",
                    operation = @Operation(
                            operationId = "validateBootcampIds",
                            summary = "Validates a list of bootcamp IDs and returns the valid ones",
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(
                                            array = @ArraySchema(schema = @Schema(type = "integer", format = "int64")),
                                            examples = @ExampleObject(value = "[1, 2, 999]")
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "List of valid bootcamp IDs",
                                            content = @Content(array = @ArraySchema(schema = @Schema(type = "integer", format = "int64")),
                                                    examples = @ExampleObject(value = "[1, 2]"))
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Invalid bootcamp IDs"),
                                    @ApiResponse(responseCode = "500", description = "Internal server error")
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(BootcampHandlerImpl bootcampHandler) {
        return route(POST("/bootcamp"), bootcampHandler::createBootcamp)
                .andRoute(GET("/bootcamp/list"), bootcampHandler::listBootcamps)
                .andRoute(DELETE("/bootcamp/{bootcampId}"), bootcampHandler::deleteBootcamp)
                .andRoute(POST("/bootcamp/validate-list"), bootcampHandler::validateBootcampIds);

    }
}