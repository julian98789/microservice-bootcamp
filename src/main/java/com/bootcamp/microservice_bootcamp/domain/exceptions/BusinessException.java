package com.bootcamp.microservice_bootcamp.domain.exceptions;

import com.bootcamp.microservice_bootcamp.domain.enums.TechnicalMessage;
import lombok.Getter;

@Getter
public class BusinessException extends ProcessorException {

    public BusinessException(TechnicalMessage technicalMessage) {
        super(technicalMessage.getMessage(), technicalMessage);
    }
}
