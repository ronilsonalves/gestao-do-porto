package com.ronilsonalves.gestaoporto.views.relatorio;

import com.ronilsonalves.gestaoporto.data.IReportModel;
import com.ronilsonalves.gestaoporto.data.enums.TipoMovimentacao;
import com.ronilsonalves.gestaoporto.data.repository.ContainerRepository;
import com.ronilsonalves.gestaoporto.data.repository.MovimentacaoRepository;
import com.ronilsonalves.gestaoporto.data.service.impl.ContainerServiceImpl;
import com.ronilsonalves.gestaoporto.data.service.impl.MovimentacaoServiceImpl;
import com.ronilsonalves.gestaoporto.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.gridpro.GridPro;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.Arrays;
import java.util.List;

@PageTitle("Relatório de movimentações - Gestão do Porto")
@Route(value = "report", layout = MainLayout.class)
public class RelatorioView extends Main {

    private final Board report = new Board();
    private GridPro<IReportModel> grid;

    private Grid.Column<IReportModel> movimentationType;
    private Grid.Column<IReportModel> containerCliente;
    private Grid.Column<IReportModel> movTotal;
    private ContainerServiceImpl containerService;
    private MovimentacaoServiceImpl movimentacaoService;

    public RelatorioView(ContainerRepository containerRepository, MovimentacaoRepository movimentacaoRepository) {
        this.containerService = new ContainerServiceImpl(containerRepository);
        this.movimentacaoService = new MovimentacaoServiceImpl(movimentacaoRepository);

        addClassName("report-view");
        setupReport();

        add(report);

    }
    private void setupReport() {
        //Adiciona o sumário das movimentaçoes
        report.addRow(createSumary("Total de movimentações",String.valueOf(movimentacaoService.count())),
                createSumary("Importação",String.valueOf(getTotalMovByImp())),
                createSumary("Exportação",String.valueOf(getTotalMovByExp())));
        report.addRow(setupGrid(),setupGraph()).setHeightFull();
        add(report);
    }

    private Component setupGrid() {
        HorizontalLayout header = createHeader("Movimentações agrupadas","Por cliente e tipo de movimentação.");

        grid = new GridPro<>();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setItems(getAgrupedMovimentacoes());
        addTotalMovColumn();
        addContainerClienteColumn();
        addMovimentacaoTypeColumn();
        return new VerticalLayout(header,grid);
    }

    private void addMovimentacaoTypeColumn() {
        movimentationType = grid.addColumn(new ComponentRenderer<>(movimentacao -> {
            HorizontalLayout horizontalLayout = new HorizontalLayout();
            horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            Span span = new Span();
            span.setClassName("movimentationType");
            span.setText(movimentacao.getTipoDeMovimentacao().getValue());
            horizontalLayout.add(span);
            return horizontalLayout;
        })).setSortable(false).setComparator(IReportModel::getTipoDeMovimentacao).setHeader("Tipo de Movimentação").setTextAlign(ColumnTextAlign.CENTER);
    }

    private void addContainerClienteColumn() {
        containerCliente = grid.addColumn(new ComponentRenderer<>(movimentacao -> {
            HorizontalLayout horizontalLayout = new HorizontalLayout();
            horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            Span span = new Span();
            span.setClassName("containerClient");
            span.setText(movimentacao.getContainerCliente());
            horizontalLayout.add(span);
            return horizontalLayout;
        })).setComparator(IReportModel::getContainerCliente).setHeader("Cliente").setTextAlign(ColumnTextAlign.CENTER);
    }

    private void addTotalMovColumn() {
        movTotal = grid.addColumn(new ComponentRenderer<>(movimentacao -> {
            HorizontalLayout horizontalLayout = new HorizontalLayout();
            horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            Span span = new Span();
            span.setClassName("movTotal");
            span.setText(String.valueOf(movimentacao.getTotal()));
            horizontalLayout.add(span);
            return horizontalLayout;
        })).setComparator(IReportModel::getTotal).setHeader("Total").setTextAlign(ColumnTextAlign.CENTER);
    }

    private Component createSumary(String title, String value) {

        H2 titleH = new H2(title);
        titleH.addClassNames("font-normal","m-0","text-secondary","text-s");

        Span valueSpan = new Span(value);
        valueSpan.addClassNames("font-semibold","text-3xl");

        VerticalLayout cardSumary = new VerticalLayout(titleH,valueSpan);
        cardSumary.addClassName("p-l");
        cardSumary.setAlignItems(FlexComponent.Alignment.CENTER);
        return cardSumary;
    }

    private Component setupGraph() {
        HorizontalLayout header = createHeader("Movimentação por tipo","Quantidade de movimentação por tipo de movimentação");

        Chart graficoPorTipo = new Chart(ChartType.PIE);
        Configuration configuration = graficoPorTipo.getConfiguration();
        configuration.getChart().setStyledMode(true);

        DataSeries dataSeries = new DataSeries();

        Arrays.stream(TipoMovimentacao.values()).forEach(tipo -> {
            dataSeries.add(new DataSeriesItem(tipo.getValue(),movimentacaoService.getMovimentacoesByTipo(tipo).size()));
        });
        configuration.addSeries(dataSeries);

        VerticalLayout movType = new VerticalLayout(header,graficoPorTipo);
        movType.addClassName("p-l");
        movType.setPadding(false);
        movType.setSpacing(false);
        movType.getElement().getThemeList().add("spacing-l");
        return movType;
    }

    private HorizontalLayout createHeader(String title, String subtitle) {
        H2 h2 = new H2(title);
        h2.addClassNames("text-xl", "m-0");

        Span span = new Span(subtitle);
        span.addClassNames("text-secondary", "text-xs");

        VerticalLayout column = new VerticalLayout(h2, span);
        column.setPadding(false);
        column.setSpacing(false);

        HorizontalLayout header = new HorizontalLayout(column);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setSpacing(false);
        header.setWidthFull();
        return header;
    }

    private List<IReportModel> getAgrupedMovimentacoes() {
        return movimentacaoService.aggruped();
    }

    private long getTotalMovByImp() {
        return movimentacaoService.totalByCatImportacao();
    }

    private long getTotalMovByExp() {
        return movimentacaoService.totalByCatExportacao();
    }
}
