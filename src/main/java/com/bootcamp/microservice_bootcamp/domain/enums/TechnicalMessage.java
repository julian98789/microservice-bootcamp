package com.bootcamp.microservice_bootcamp.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TechnicalMessage {

    INTERNAL_ERROR("500","Something went wrong, please try again", ""),
    INVALID_CAPACITY_NAME("400", "Invalid Capacity name. Must not be empty, unique, and max 50 chars.", "name"),
    INVALID_CAPACITY_DESCRIPTION("400", "Invalid Capacity description. Must not be empty and max 90 chars.", "description"),
    BOOTCAMP_ALREADY_EXISTS("409", "Bootcamp name already exists.", "name"),
    BOOTCAMP_CREATED("201", "Bootcamp created successfully", ""),
    BOOTCAMP_ASSOCIATION_FAILED("400", "Failed to associate bootcamp with capacity", ""),
    INVALID_CAPACITY_LIST("400", "Invalid capacity list. Must contain between 1 and 4 unique capacity IDs.", ""),
    BOOTCAMP_NOT_FOUND("404", "Bootcamp does not exist", "bootcampId"),
    DUPLICATE_CAPACITY_ID("400", "Duplicate capacity in request", "capacityIds"),
    CAPACITY_ALREADY_ASSOCIATED("409", "The capacity is already associated ", "capacityId"),
    CAPABILITY_CAPACITY_LIMIT("400", "Cannot associate: capability would exceed 4 capacity associations", "capabilityId"),
    SAVED_ASSOCIATION("200", "Associations saved successfully", ""),
    BOOTCAMP_DUPLICATE_DATE_DURATION ("400", "Bootcamp with the same release date and duration already exists", "");

    private final String code;
    private final String message;
    private final String param;
}