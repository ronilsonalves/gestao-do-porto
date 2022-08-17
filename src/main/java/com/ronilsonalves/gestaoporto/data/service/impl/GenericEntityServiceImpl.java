package com.ronilsonalves.gestaoporto.data.service.impl;

import com.ronilsonalves.gestaoporto.data.entity.GenericEntity;
import com.ronilsonalves.gestaoporto.data.repository.GenericEntityRepository;
import com.ronilsonalves.gestaoporto.data.service.GenericEntityService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

@Service
public abstract class GenericEntityServiceImpl implements GenericEntityService {


    private final GenericEntityRepository repository;

    protected GenericEntityServiceImpl(GenericEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public GenericEntity save(GenericEntity entity) {
        return (GenericEntity) repository.save(entity);
    }

    @Override
    public GenericEntity update(GenericEntity entity) {
        return (GenericEntity) repository.save(entity);
    }

    @Override
    public Optional<GenericEntity> get(UUID id) {
        return repository.findById(id);
    }

    @Override
    public Page<GenericEntity> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public void delete(UUID id) throws DataIntegrityViolationException {
        repository.deleteById(id);
    }
}