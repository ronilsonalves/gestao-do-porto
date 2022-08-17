package com.ronilsonalves.gestaoporto.data.repository;

import com.ronilsonalves.gestaoporto.data.entity.GenericEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@NoRepositoryBean
public interface GenericEntityRepository<T extends GenericEntity> extends JpaRepository<T, UUID> {
}