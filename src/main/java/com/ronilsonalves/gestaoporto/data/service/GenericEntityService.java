package com.ronilsonalves.gestaoporto.data.service;

import com.ronilsonalves.gestaoporto.data.entity.GenericEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public interface GenericEntityService {

    Optional<GenericEntity> get(UUID id);

    GenericEntity save(GenericEntity entity);

    GenericEntity update(GenericEntity entity);

    Page<GenericEntity> list(Pageable pageable);

    void delete(UUID id) throws SQLException;

    int count();

}
