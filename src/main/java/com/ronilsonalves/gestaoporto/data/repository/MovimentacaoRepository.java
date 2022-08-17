package com.ronilsonalves.gestaoporto.data.repository;

import com.ronilsonalves.gestaoporto.data.IReportModel;
import com.ronilsonalves.gestaoporto.data.entity.Movimentacao;
import com.ronilsonalves.gestaoporto.data.enums.Categoria;
import com.ronilsonalves.gestaoporto.data.enums.TipoMovimentacao;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimentacaoRepository extends GenericEntityRepository<Movimentacao> {

    List<Movimentacao> findAllByContainer_Cliente (String nomeDoCliente);

    List<Movimentacao> findAllByTipoDeMovimentacao (TipoMovimentacao tipoMovimentacao);

    Long countAllByContainer_Categoria(Categoria categoria);

    @Query("SELECT m.tipoDeMovimentacao AS tipoDeMovimentacao, m.container.cliente AS containerCliente, COUNT(m.id) AS total "
    + "FROM Movimentacao AS m GROUP BY m.container.cliente, m.tipoDeMovimentacao ORDER BY COUNT(m.id), m.container.cliente,m.tipoDeMovimentacao")
    List<IReportModel> countMovimentacaosByTipoDeMovimentacaoAndContainer_Cliente();

}