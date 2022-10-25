package com.ronilsonalves.gestaoporto.data.repository;

import com.ronilsonalves.gestaoporto.data.entity.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends GenericEntityRepository<User> {

        User findByUsername(String username);
}
