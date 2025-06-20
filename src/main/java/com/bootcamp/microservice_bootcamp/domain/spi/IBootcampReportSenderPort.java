package com.bootcamp.microservice_bootcamp.domain.spi;

import com.bootcamp.microservice_bootcamp.domain.model.BootcampReportData;
import reactor.core.publisher.Mono;

public interface IBootcampReportSenderPort {
    Mono<Void> sendBootcampReport(BootcampReportData reportData);
}