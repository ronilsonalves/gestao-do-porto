package com.ronilsonalves.gestaoporto.data.service.impl;

import com.ronilsonalves.gestaoporto.data.entity.Container;
import com.ronilsonalves.gestaoporto.data.repository.ContainerRepository;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

@Service
public class ContainerServiceImpl extends GenericEntityServiceImpl {

    private final ContainerRepository repository;

    public ContainerServiceImpl(ContainerRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public Optional<Container> getContainerByNumber(String containerNum) {
        return repository.findContainerByNumber(containerNum);
    }

    @Override
    public int count() {
        return 0;
    }
}
