package com.bootcamp.microservice_bootcamp.infrastructure.entrypoints.mapper;

import com.bootcamp.microservice_bootcamp.domain.model.BootcampWithCapacitiesAndTechnologies;
import com.bootcamp.microservice_bootcamp.infrastructure.entrypoints.dto.BootcampWithCapacitiesAndTechnologiesDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IBootcampWithCapacitiesAndTechnologiesMapper {
    BootcampWithCapacitiesAndTechnologiesDTO toDTO(BootcampWithCapacitiesAndTechnologies model);

}