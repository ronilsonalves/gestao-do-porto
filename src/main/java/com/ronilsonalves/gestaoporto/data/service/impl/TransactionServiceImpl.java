package com.ronilsonalves.gestaoporto.data.service.impl;

import com.ronilsonalves.gestaoporto.data.IReportModel;
import com.ronilsonalves.gestaoporto.data.entity.Transaction;
import com.ronilsonalves.gestaoporto.data.enums.Category;
import com.ronilsonalves.gestaoporto.data.enums.TransactionType;
import com.ronilsonalves.gestaoporto.data.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionServiceImpl extends GenericEntityServiceImpl {

    private final TransactionRepository repository;

    public TransactionServiceImpl(TransactionRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public List<Transaction> getMovimentacoesByClient(String client) {
        return repository.findAllByContainer_Client(client);
    }

    public List<Transaction> getMovimentacoesByTipo(TransactionType transactionType) {
        return repository.findAllByTransactionType(transactionType);
    }

    public long totalByCatImportacao() {
        return repository.countAllByContainer_Category(Category.IMPORTAÇÃO);
    }

    public long totalByCatExportacao() {
        return repository.countAllByContainer_Category(Category.EXPORTAÇÃO);
    }

    public List<IReportModel> aggruped() {
        return repository.countTransactionByTransactionTypeAndContainer_Client();
    }

    @Override
    public int count() {
        return (int) repository.count();
    }
}
