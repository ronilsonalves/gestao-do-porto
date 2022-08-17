package com.ronilsonalves.gestaoporto.data.generator;

import com.ronilsonalves.gestaoporto.data.entity.Container;
import com.ronilsonalves.gestaoporto.data.entity.Movimentacao;
import com.ronilsonalves.gestaoporto.data.enums.Categoria;
import com.ronilsonalves.gestaoporto.data.enums.Status;
import com.ronilsonalves.gestaoporto.data.enums.Tipo;
import com.ronilsonalves.gestaoporto.data.enums.TipoMovimentacao;
import com.ronilsonalves.gestaoporto.data.repository.ContainerRepository;
import com.ronilsonalves.gestaoporto.data.repository.MovimentacaoRepository;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@SpringComponent
public class DataGenerator {

    @Bean
    public CommandLineRunner loadData(ContainerRepository containerRepository, MovimentacaoRepository MRepository) {
        return args -> {
            Logger logger = LoggerFactory.getLogger(getClass());
            if (containerRepository.count() != 0L && MRepository.count() !=0L) {
                logger.info("Using existing database");
                return;
            }
            List<Container> containersToSave = new ArrayList<>();

            logger.info("Generating demo data");

            logger.info("... generating 10 Containers and Movimentações...");
            for (int i = 0; i < 10 ; i++){
                Container container = new Container("Fulano","ABCD123456"+i,Tipo.QUARENTA, Status.CHEIO, Categoria.IMPORTAÇÃO);
                containersToSave.add(container);
            }
            containerRepository.saveAll(containersToSave).forEach(container -> {
                Movimentacao movimentacao = new Movimentacao(TipoMovimentacao.PESAGEM,LocalDateTime.now().minus(360,ChronoUnit.MINUTES),LocalDateTime.now(),container);
                MRepository.save(movimentacao);
            });

            logger.info("Generated demo data");
        };
    }

}