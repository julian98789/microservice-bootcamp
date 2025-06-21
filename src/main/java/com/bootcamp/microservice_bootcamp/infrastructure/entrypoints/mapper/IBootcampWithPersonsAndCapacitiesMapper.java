package com.bootcamp.microservice_bootcamp.infrastructure.entrypoints.mapper;

import com.bootcamp.microservice_bootcamp.domain.model.BootcampWithCapacitiesAndPersons;
import com.bootcamp.microservice_bootcamp.domain.model.Person;
import com.bootcamp.microservice_bootcamp.infrastructure.entrypoints.dto.BootcampWithPersonsAndCapacitiesDTO;
import com.bootcamp.microservice_bootcamp.infrastructure.entrypoints.dto.PersonSummaryDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IBootcampWithPersonsAndCapacitiesMapper {
    BootcampWithPersonsAndCapacitiesDTO toDTO(BootcampWithCapacitiesAndPersons model);
    List<PersonSummaryDTO> toPersonDTOList(List<Person> persons);
}