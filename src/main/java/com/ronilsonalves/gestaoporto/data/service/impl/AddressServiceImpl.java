package com.ronilsonalves.gestaoporto.data.service.impl;

import com.ronilsonalves.gestaoporto.data.repository.AddressRepository;
import org.springframework.stereotype.Service;

@Service
public class AddressServiceImpl extends GenericEntityServiceImpl {

    private final AddressRepository repository;

    public AddressServiceImpl (AddressRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    public int count() {
        return 0;
    }
}
