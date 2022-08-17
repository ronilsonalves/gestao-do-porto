package com.ronilsonalves.gestaoporto.data.service.impl;

import com.ronilsonalves.gestaoporto.data.IReportModel;
import com.ronilsonalves.gestaoporto.data.entity.Movimentacao;
import com.ronilsonalves.gestaoporto.data.enums.Categoria;
import com.ronilsonalves.gestaoporto.data.enums.TipoMovimentacao;
import com.ronilsonalves.gestaoporto.data.repository.MovimentacaoRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovimentacaoServiceImpl extends GenericEntityServiceImpl {

    private final MovimentacaoRepository repository;

    public MovimentacaoServiceImpl(MovimentacaoRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public List<Movimentacao> getMovimentacoesByClient(String client) {
        return repository.findAllByContainer_Cliente(client);
    }

    public List<Movimentacao> getMovimentacoesByTipo(TipoMovimentacao tipoMovimentacao) {
        return repository.findAllByTipoDeMovimentacao(tipoMovimentacao);
    }

    public long totalByCatImportacao() {
        return repository.countAllByContainer_Categoria(Categoria.IMPORTAÇÃO);
    }

    public long totalByCatExportacao() {
        return repository.countAllByContainer_Categoria(Categoria.EXPORTAÇÃO);
    }

    public List<IReportModel> aggruped() {
        return repository.countMovimentacaosByTipoDeMovimentacaoAndContainer_Cliente();
    }

    @Override
    public int count() {
        return (int) repository.count();
    }
}
