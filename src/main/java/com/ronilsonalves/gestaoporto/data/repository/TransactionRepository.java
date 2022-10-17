package com.ronilsonalves.gestaoporto.data.repository;

import com.ronilsonalves.gestaoporto.data.IReportModel;
import com.ronilsonalves.gestaoporto.data.entity.Transaction;
import com.ronilsonalves.gestaoporto.data.enums.Category;
import com.ronilsonalves.gestaoporto.data.enums.TransactionType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends GenericEntityRepository<Transaction> {

    List<Transaction> findAllByContainer_Client (String nomeDoCliente);

    List<Transaction> findAllByTransactionType (TransactionType transactionType);

    Long countAllByContainer_Category(Category category);

    @Query("SELECT t.transactionType AS transactionType, t.container.client AS containerClient, COUNT(t.id) AS total "
    + "FROM Transaction AS t GROUP BY t.container.client, t.transactionType ORDER BY COUNT(t.id), t.container.client, t.transactionType")
    List<IReportModel> countTransactionByTransactionTypeAndContainer_Client();

}