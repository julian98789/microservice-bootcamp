package com.bootcamp.microservice_bootcamp.infrastructure.entrypoints.mapper;

import com.bootcamp.microservice_bootcamp.domain.model.Bootcamp;
import com.bootcamp.microservice_bootcamp.infrastructure.entrypoints.dto.BootcampDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IBootcampMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "releaseDate", target = "releaseDate")
    @Mapping(source = "duration", target = "duration")
    Bootcamp bootcampDTOToBootcamp(BootcampDTO bootcampDTO);

    BootcampDTO bootcampToDTO(Bootcamp bootcamp);
}