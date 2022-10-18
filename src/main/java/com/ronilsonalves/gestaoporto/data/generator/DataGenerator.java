package com.ronilsonalves.gestaoporto.data.generator;

import com.ronilsonalves.gestaoporto.data.entity.Client;
import com.ronilsonalves.gestaoporto.data.entity.Container;
import com.ronilsonalves.gestaoporto.data.entity.Address;
import com.ronilsonalves.gestaoporto.data.entity.Transaction;
import com.ronilsonalves.gestaoporto.data.enums.*;
import com.ronilsonalves.gestaoporto.data.repository.ClientRepository;
import com.ronilsonalves.gestaoporto.data.repository.ContainerRepository;
import com.ronilsonalves.gestaoporto.data.repository.GenericEntityRepository;
import com.ronilsonalves.gestaoporto.data.repository.TransactionRepository;
import com.vaadin.exampledata.DataType;
import com.vaadin.exampledata.ExampleDataGenerator;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SpringComponent
public class DataGenerator {

    @Bean
    public CommandLineRunner loadData(ContainerRepository containerRepository, TransactionRepository transactionRepository, ClientRepository clientRepository, GenericEntityRepository<Address> ERepository) {
        return args -> {
            Logger logger = LoggerFactory.getLogger(getClass());
            if (containerRepository.count() != 0L && transactionRepository.count() !=0L && clientRepository.count() != 0L) {
                logger.info("Using existing database");
                return;
            }

            logger.info("Generating demo data");

            Address addressSP = ERepository.save(new Address("Rua 1","234","12345000","Centro","São Paulo", State.SP));
            Address addressMA = ERepository.save(new Address("Rua 2","567","65000000","Centro","São Luís", State.MA));
            Address addressCE = ERepository.save(new Address("Rua 3","890","60000000","Centro","Fortaleza", State.CE));
            Address addressRJ = ERepository.save(new Address("Rua 4","123","20000000","Centro","Rio de Janeiro", State.RJ));
            Address addressMG = ERepository.save(new Address("Rua 5","456","30000000","Centro","Belo Horizonte", State.MG));
            Address addressPI = ERepository.save(new Address("Rua 6","789","64000000","Centro","Teresina", State.PI));
            Address addressPB = ERepository.save(new Address("Rua 7","012","58000000","Centro","João Pessoa", State.PB));
            Address addressPE = ERepository.save(new Address("Rua 8","345","56000000","Centro","Recife", State.PE));
            Address addressAL = ERepository.save(new Address("Rua 9","678","57000000","Centro","Maceió", State.AL));
            Address addressRN = ERepository.save(new Address("Rua 10","901","59000000","Centro","Natal", State.RN));

            List<Container> containersToSave = new ArrayList<>();
            logger.info("... genarating some people");

            clientRepository.save(new Client("Jordan", "Mendes", "12345678900", LocalDate.now().minusYears(32),
                    "jordan.mendes@mfake.com", "11923456789", addressSP, null));
            clientRepository.save(new Client("Larissa", "Silva", "12345678901", LocalDate.now().minusYears(25),
                    "larissa.silva@mailfk.com", "98981234567", addressMA, null));
            clientRepository.save(new Client("Josué", "Santos", "12345678902", LocalDate.now().minusYears(28),
                    "josue.santos@omail.fk", "85991234567", addressCE, null));
            clientRepository.save(new Client("Luana", "Figueiredo", "12345678903", LocalDate.now().minusYears(25),
                    "lu.figueiredo@mailfk.com", "21991234567", addressRJ, null));
            clientRepository.save(new Client("Julia", "Oliveira", "12345678904", LocalDate.now().minusYears(30),
                    "julia.ol@fakemail.com", "31991234567", addressMG, null));
            clientRepository.save(new Client("Luan", "Pereira", "12345678905", LocalDate.now().minusYears(27),
                    "luan.pereira.2022@fakeml.com", "85991234567", addressPI, null));
            clientRepository.save(new Client("Marcos", "Almeida", "12345678906", LocalDate.now().minusYears(29),
                    "marcos.almeida@fakemail.com", "83991234567", addressPB, null));
            clientRepository.save(new Client("Rafaela", "Souza", "12345678907", LocalDate.now().minusYears(26),
                    "rafaela.souz@amail.com", "81991234567", addressPE, null));
            clientRepository.save(new Client("Marina", "dos Santos", "12345678908", LocalDate.now().minusYears(24),
                    "marina.dossantos@mailfk.com", "82991234567", addressAL, null));
            clientRepository.save(new Client("Roberto", "Fernandes", "12345678909", LocalDate.now().minusYears(31),
                    "rob.fernandes@amail.com", "84991234567", addressRN, null));

            List<Client> clients = clientRepository.findAll();

            logger.info("... generating and saving 10 Containers...");

            for (int index = 0; index < 10; index++) {
                containersToSave.add(
                        new Container(clients.get(index),
                                "CONT12345"+index+"7",ContainerType.QUARENTA,Status.CHEIO,Category.EXPORTAÇÃO)
                );
            }

            for (int index = 0; index < containersToSave.size(); index++) {
                if(index%2 == 0){
                    containersToSave.get(index).setCategory(Category.IMPORTAÇÃO);
                }
                if(index%3 == 0){
                    containersToSave.get(index).setStatus(Status.VAZIO);
                }
            }

            logger.info("... generating and saving 10 transactions...");

            List<Transaction> transactionsToSave = new ArrayList<>();

            containerRepository.saveAll(containersToSave).forEach(container -> {
                Transaction transaction = new Transaction(
                        TransactionType.GATE_IN,
                        LocalDateTime.now().minus(1, ChronoUnit.DAYS),
                        LocalDateTime.now(),
                        container);
                transactionsToSave.add(transaction);
            });

            transactionsToSave.forEach(transaction -> {
                if(transaction.getContainer().getCategory() == Category.EXPORTAÇÃO){
                    transaction.setTransactionType(TransactionType.EMBARQUE);
                } else if (transaction.getContainer().getStatus() == Status.CHEIO){
                    transaction.setTransactionType(TransactionType.PESAGEM);
                } else {
                    transaction.setTransactionType(TransactionType.REPOSICIONAMENTO);
                }
            });

            transactionRepository.saveAll(transactionsToSave);

            logger.info("Generated demo data");
        };
    }

}