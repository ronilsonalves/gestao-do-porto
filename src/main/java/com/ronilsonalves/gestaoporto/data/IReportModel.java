package com.ronilsonalves.gestaoporto.data;


import com.ronilsonalves.gestaoporto.data.entity.Client;
import com.ronilsonalves.gestaoporto.data.enums.TransactionType;

public interface IReportModel {

    TransactionType getTransactionType();

    Client getContainerClient();

    Long getTotal();
}
