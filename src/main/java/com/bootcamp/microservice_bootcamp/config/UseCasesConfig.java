package com.bootcamp.microservice_bootcamp.config;


import com.bootcamp.microservice_bootcamp.domain.api.IBootcampServicePort;
import com.bootcamp.microservice_bootcamp.domain.spi.IBootcampCapacityAssociationPort;
import com.bootcamp.microservice_bootcamp.domain.spi.IBootcampPersistencePort;
import com.bootcamp.microservice_bootcamp.domain.spi.IBootcampQueryPort;
import com.bootcamp.microservice_bootcamp.domain.usecase.BootcampUseCase;
import com.bootcamp.microservice_bootcamp.infrastructure.adapters.persistenceadapter.BootcampPersistenceAdapter;
import com.bootcamp.microservice_bootcamp.infrastructure.adapters.persistenceadapter.mapper.IBootcampEntityMapper;
import com.bootcamp.microservice_bootcamp.infrastructure.adapters.persistenceadapter.repository.IBootcampRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class UseCasesConfig {
        private final IBootcampRepository bootcampRepository;
        private final IBootcampEntityMapper bootcampEntityMapper;


        @Bean
        public IBootcampPersistencePort bootcampPersistencePort() {
                return new BootcampPersistenceAdapter(bootcampRepository, bootcampEntityMapper);
        }

        @Bean
        public IBootcampServicePort bootcampServicePort(
                IBootcampPersistencePort bootcampPersistencePort,
                IBootcampCapacityAssociationPort bootcampCapacityAssociationPort,
                IBootcampQueryPort bootcampQueryPort

        ) {
                return new BootcampUseCase(bootcampPersistencePort, bootcampCapacityAssociationPort, bootcampQueryPort);
        }




}