package com.ronilsonalves.gestaoporto.data;


import com.ronilsonalves.gestaoporto.data.entity.Client;
import com.ronilsonalves.gestaoporto.data.enums.TipoMovimentacao;

public interface IReportModel {

    TipoMovimentacao getTipoDeMovimentacao();

    Client getContainerCliente();

    Long getTotal();
}
