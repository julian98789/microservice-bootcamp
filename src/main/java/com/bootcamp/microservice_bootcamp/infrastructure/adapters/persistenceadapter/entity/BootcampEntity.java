package com.bootcamp.microservice_bootcamp.infrastructure.adapters.persistenceadapter.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Table(name = "bootcamp")
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class BootcampEntity {
    @Id
    private Long id;
    private String name;
    private String description;

    @Column("release_date")
    private LocalDate releaseDate;
    private Integer duration;
}