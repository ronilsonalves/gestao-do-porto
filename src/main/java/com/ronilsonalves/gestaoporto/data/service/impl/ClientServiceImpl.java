package com.ronilsonalves.gestaoporto.data.service.impl;

import com.ronilsonalves.gestaoporto.data.entity.Client;
import com.ronilsonalves.gestaoporto.data.entity.Address;
import com.ronilsonalves.gestaoporto.data.entity.GenericEntity;
import com.ronilsonalves.gestaoporto.data.repository.ClientRepository;
import org.springframework.stereotype.Service;

@Service
public class ClientServiceImpl extends GenericEntityServiceImpl {

    private final ClientRepository repository;
    private final AddressServiceImpl addressService;

    public ClientServiceImpl(ClientRepository repository, AddressServiceImpl addressService) {
        super(repository);
        this.repository = repository;
        this.addressService = addressService;
    }


    public GenericEntity save(Client client) {
        Address endClientUnsaved = new Address();
        if (client.getAddress().getId() == null) {
            endClientUnsaved = (Address) addressService.save(client.getAddress());
            System.out.println(endClientUnsaved.getId());
            client.setAddress(endClientUnsaved);
        }
        return repository.save(client);
    }

    @Override
    public int count() {
        return 0;
    }
}
