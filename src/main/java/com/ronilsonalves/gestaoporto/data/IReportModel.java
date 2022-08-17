package com.ronilsonalves.gestaoporto.data;


import com.ronilsonalves.gestaoporto.data.enums.TipoMovimentacao;

public interface IReportModel {

    TipoMovimentacao getTipoDeMovimentacao();

    String getContainerCliente();

    Long getTotal();
}
