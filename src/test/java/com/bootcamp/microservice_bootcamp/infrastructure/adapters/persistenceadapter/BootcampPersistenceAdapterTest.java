package com.bootcamp.microservice_bootcamp.infrastructure.adapters.persistenceadapter;

import com.bootcamp.microservice_bootcamp.domain.model.Bootcamp;
import com.bootcamp.microservice_bootcamp.infrastructure.adapters.persistenceadapter.entity.BootcampEntity;
import com.bootcamp.microservice_bootcamp.infrastructure.adapters.persistenceadapter.mapper.IBootcampEntityMapper;
import com.bootcamp.microservice_bootcamp.infrastructure.adapters.persistenceadapter.repository.IBootcampRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BootcampPersistenceAdapterTest {

    @Mock
    private IBootcampRepository bootcampRepository;

    @Mock
    private IBootcampEntityMapper entityMapper;

    private BootcampPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new BootcampPersistenceAdapter(bootcampRepository, entityMapper);
    }

    @Test
    void save_shouldReturnSavedModel() {
        Bootcamp model = new Bootcamp(1L, "Test", "Desc", null, 10);
        BootcampEntity entity = new BootcampEntity();

        when(entityMapper.toEntity(model)).thenReturn(entity);
        when(bootcampRepository.save(entity)).thenReturn(Mono.just(entity));
        when(entityMapper.toModel(entity)).thenReturn(model);

        StepVerifier.create(adapter.save(model))
                .expectNext(model)
                .verifyComplete();

        verify(bootcampRepository).save(entity);
    }

    @Test
    void existsByName_shouldReturnTrueIfExists() {
        String name = "Test";
        BootcampEntity entity = new BootcampEntity();
        Bootcamp model = new Bootcamp(1L, name, "Desc", null, 10);

        when(bootcampRepository.findByName(name)).thenReturn(Mono.just(entity));
        when(entityMapper.toModel(entity)).thenReturn(model);

        StepVerifier.create(adapter.existsByName(name))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void existsByName_shouldReturnFalseIfNotExists() {
        String name = "Test";
        when(bootcampRepository.findByName(name)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.existsByName(name))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void deleteById_shouldCallRepository() {
        Long id = 1L;
        when(bootcampRepository.deleteById(id)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.deleteById(id))
                .verifyComplete();

        verify(bootcampRepository).deleteById(id);
    }

    @Test
    void existsById_shouldReturnRepositoryValue() {
        Long id = 1L;
        when(bootcampRepository.existsById(id)).thenReturn(Mono.just(true));

        StepVerifier.create(adapter.existsById(id))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void findById_shouldReturnModelIfExists() {
        Long id = 1L;
        BootcampEntity entity = new BootcampEntity();
        Bootcamp model = new Bootcamp(id, "Test", "Desc", null, 10);

        when(bootcampRepository.findById(id)).thenReturn(Mono.just(entity));
        when(entityMapper.toModel(entity)).thenReturn(model);

        StepVerifier.create(adapter.findById(id))
                .expectNext(model)
                .verifyComplete();
    }

    @Test
    void findById_shouldReturnEmptyIfNotExists() {
        Long id = 1L;
        when(bootcampRepository.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findById(id))
                .verifyComplete();
    }
}
