package com.ronilsonalves.gestaoporto.data.repository;

import com.ronilsonalves.gestaoporto.data.entity.Container;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContainerRepository extends GenericEntityRepository<Container> {

    Optional<Container> findContainerByNumber(String containerNum);
}