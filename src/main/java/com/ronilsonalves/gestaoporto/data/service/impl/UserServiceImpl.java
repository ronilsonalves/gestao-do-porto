package com.ronilsonalves.gestaoporto.data.service.impl;

import com.ronilsonalves.gestaoporto.data.entity.User;
import com.ronilsonalves.gestaoporto.data.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends GenericEntityServiceImpl {

    private final UserRepository repository;

    public UserServiceImpl(UserRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public User findByUsername(String username) {
        return repository.findByUsername(username);
    }
}
