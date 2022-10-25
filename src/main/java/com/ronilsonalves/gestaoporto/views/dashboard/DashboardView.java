package com.ronilsonalves.gestaoporto.views.dashboard;

import com.ronilsonalves.gestaoporto.data.IReportModel;
import com.ronilsonalves.gestaoporto.data.entity.Client;
import com.ronilsonalves.gestaoporto.data.entity.Container;
import com.ronilsonalves.gestaoporto.data.entity.Transaction;
import com.ronilsonalves.gestaoporto.data.enums.TransactionType;
import com.ronilsonalves.gestaoporto.data.repository.AddressRepository;
import com.ronilsonalves.gestaoporto.data.repository.ClientRepository;
import com.ronilsonalves.gestaoporto.data.repository.ContainerRepository;
import com.ronilsonalves.gestaoporto.data.repository.TransactionRepository;
import com.ronilsonalves.gestaoporto.data.service.impl.AddressServiceImpl;
import com.ronilsonalves.gestaoporto.data.service.impl.ClientServiceImpl;
import com.ronilsonalves.gestaoporto.data.service.impl.ContainerServiceImpl;
import com.ronilsonalves.gestaoporto.data.service.impl.TransactionServiceImpl;
import com.ronilsonalves.gestaoporto.views.MainLayout;
import com.ronilsonalves.gestaoporto.views.clientes.ClientsView;
import com.ronilsonalves.gestaoporto.views.containers.ContainersView;
import com.ronilsonalves.gestaoporto.views.transactions.TransactionsView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.button.Button;
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
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.BeanUtils;

import javax.annotation.security.RolesAllowed;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Dashboard - Gestão do Porto")
@Route(value = "/", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class DashboardView extends Main {

    private final Board report = new Board();
    private GridPro<IReportModel> grid;
    private GridPro<Client> gridClients;
    private GridPro<Container> gridContainers;
    private GridPro<Transaction> gridTransactions;

    private final AddressServiceImpl addressService;
    private ClientServiceImpl clientService;
    private ContainerServiceImpl containerService;
    private TransactionServiceImpl transactionService;

    public DashboardView(AddressRepository addressRepository, ClientRepository clientRepository, ContainerRepository containerRepository, TransactionRepository transactionRepository) {
        this.addressService = new AddressServiceImpl(addressRepository);
        this.clientService = new ClientServiceImpl(clientRepository, addressService);
        this.containerService = new ContainerServiceImpl(containerRepository);
        this.transactionService = new TransactionServiceImpl(transactionRepository);

        addClassName("report-view");
        setupReport();

        add(report);

    }
    private void setupReport() {
        //Adiciona o sumário das movimentaçoes
        report.addRow(createSumary("Total de movimentações",String.valueOf(transactionService.count())),
                createSumary("Importação",String.valueOf(getTotalMovByImp())),
                createSumary("Exportação",String.valueOf(getTotalMovByExp())));
        report.addRow(setupRecentClientsGrid(), setupRecentContainersGrid(), setupRecentsTransactionsGrid());
        report.addRow(setupGrid(),setupGraph()).setHeightFull();
        add(report);
    }

    private Component setupRecentClientsGrid() {
        HorizontalLayout header = createHeader("Novos Clientes", "Clientes cadastrados recentemente");
        gridClients = new GridPro<>();
        gridClients.setSelectionMode(Grid.SelectionMode.NONE);
        gridClients.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS);
        gridClients.setItems(getRecentClients());
        addClientNameColumn();
        addClientDocumentColumn();
        gridClients.setMaxHeight("240px");

        Button button = new Button("Gerenciar clientes", buttonClickEvent -> {
            UI.getCurrent().navigate(ClientsView.class);
        });

        button.setWidthFull();
        button.setAutofocus(true);

        return new VerticalLayout(header, gridClients,button);
    }

    private Component setupRecentContainersGrid() {
        HorizontalLayout header = createHeader("Novos Containers", "Containers cadastrados recentemente");

        gridContainers = new GridPro<>();
        gridContainers.setSelectionMode(Grid.SelectionMode.NONE);
        gridContainers.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS);
        gridContainers.setItems(getRecentContainers());
        addContainerNumberColumn();
        addContainerTypeColumn();
        addContainerStatusColumn();
        addContainerCategoryColumn();
        gridContainers.setMaxHeight("240px");

        Button button = new Button("Gerenciar containers", buttonClickEvent -> {
            UI.getCurrent().navigate(ContainersView.class);
        });

        button.setWidthFull();
        button.setAutofocus(true);

        return new VerticalLayout(header, gridContainers,button);
    }

    private Component setupRecentsTransactionsGrid() {
        HorizontalLayout header = createHeader("Movimentações recentes","Movimentações registradas recentemente");

        gridTransactions = new GridPro<>();
        gridTransactions.setSelectionMode(Grid.SelectionMode.NONE);
        gridTransactions.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS);
        gridTransactions.setItems(getRecentMovimentacoes());
        addMovTypeColumn();
        addMovContainerNumColumn();
        addMovStartDateColumn();
        gridTransactions.setMaxHeight("240px");

        Button button = new Button("Gerenciar movimentações", buttonClickEvent -> {
            UI.getCurrent().navigate(TransactionsView.class);
        });
        button.setWidthFull();
        button.setAutofocus(true);

        return new VerticalLayout(header, gridTransactions, button);
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

    private void addClientNameColumn() {
        Grid.Column<Client> clientNameColumn = gridClients.addColumn(new ComponentRenderer<>(cliente -> {
            HorizontalLayout horizontalLayout = new HorizontalLayout();
            Span span = new Span(cliente.getName());
            span.setClassName("clientName");
            span.setText(cliente.getName());
            horizontalLayout.add(span);
            return horizontalLayout;
        })).setHeader("Nome").setSortable(true).setAutoWidth(true);
    }

    private void addClientDocumentColumn() {
        Grid.Column<Client> clientDocumentColumn = gridClients.addColumn(new ComponentRenderer<>(cliente -> {
            HorizontalLayout horizontalLayout = new HorizontalLayout();
            Span span = new Span(cliente.getDocument());
            span.setClassName("clientDocument");
            span.setText(cliente.getDocument());
            horizontalLayout.add(span);
            return horizontalLayout;
        })).setHeader("Documento").setSortable(true).setAutoWidth(true);
    }

    private void addContainerNumberColumn() {
        Grid.Column<Container> containerNumberColumn = gridContainers.addColumn(new ComponentRenderer<>(container -> {
            HorizontalLayout horizontalLayout = new HorizontalLayout();
            Span span = new Span(container.getNumber());
            span.setClassName("containerNumber");
            span.setText(container.getNumber());
            horizontalLayout.add(span);
            return horizontalLayout;
        })).setHeader("Número").setSortable(true).setAutoWidth(true);
    }

    private void addContainerTypeColumn() {
        Grid.Column<Container> containerTypeColumn = gridContainers.addColumn(new ComponentRenderer<>(container -> {
            HorizontalLayout horizontalLayout = new HorizontalLayout();
            Span span = new Span(String.valueOf(container.getContainerType()));
            span.setClassName("containerType");
            span.setText(String.valueOf(container.getContainerType()));
            horizontalLayout.add(span);
            return horizontalLayout;
        })).setHeader("Tipo").setSortable(true).setAutoWidth(true);
    }

    private void addContainerStatusColumn() {
        Grid.Column<Container> containerStatusColumn = gridContainers.addColumn(new ComponentRenderer<>(container -> {
            HorizontalLayout horizontalLayout = new HorizontalLayout();
            Span span = new Span(String.valueOf(container.getStatus()));
            span.setClassName("containerStatus");
            span.setText(String.valueOf(container.getStatus()));
            horizontalLayout.add(span);
            return horizontalLayout;
        })).setHeader("Status").setSortable(true).setAutoWidth(true);
    }

    private void addContainerCategoryColumn() {
        Grid.Column<Container> containerCategoryColumn = gridContainers.addColumn(new ComponentRenderer<>(container -> {
            HorizontalLayout horizontalLayout = new HorizontalLayout();
            Span span = new Span(String.valueOf(container.getCategory()));
            span.setClassName("containerCategory");
            span.setText(String.valueOf(container.getCategory()));
            horizontalLayout.add(span);
            return horizontalLayout;
        })).setHeader("Categoria").setSortable(true).setAutoWidth(true);
    }

    private void addMovTypeColumn() {
        Grid.Column<Transaction> transactionTypeColumn = gridTransactions.addColumn(new ComponentRenderer<>(transaction -> {
            HorizontalLayout horizontalLayout = new HorizontalLayout();
            Span span = new Span(transaction.getTransactionType().getValue());
            span.setClassName("transactionType");
            span.setText(transaction.getTransactionType().getValue());
            horizontalLayout.add(span);
            return horizontalLayout;
        })).setHeader("Tipo de Mov.").setSortable(true).setAutoWidth(true);
    }

    private void addMovStartDateColumn() {
        Grid.Column<Transaction> transactionStartDateColumn = gridTransactions.addColumn(new LocalDateTimeRenderer<>(Transaction::getStartDateTime,
                "dd/MM/yyyy HH:mm"))
                .setHeader("Data/Hora de Início").setSortable(true).setAutoWidth(true);
    }

    private void addMovContainerNumColumn() {
        Grid.Column<Transaction> transactionContainerNumColumn = gridTransactions.addColumn(new ComponentRenderer<>(transaction -> {
            HorizontalLayout horizontalLayout = new HorizontalLayout();
            Span span = new Span(transaction.getContainer().getNumber());
            span.setClassName("transactionContainerNum");
            span.setText(transaction.getContainer().getNumber());
            horizontalLayout.add(span);
            return horizontalLayout;
        })).setHeader("Num. do Container").setSortable(true).setAutoWidth(true);
    }

    private void addMovimentacaoTypeColumn() {
        Grid.Column<IReportModel> transactionType = grid.addColumn(new ComponentRenderer<>(transaction -> {
            HorizontalLayout horizontalLayout = new HorizontalLayout();
            horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            Span span = new Span();
            span.setClassName("transactionType");
            span.setText(transaction.getTransactionType().getValue());
            horizontalLayout.add(span);
            return horizontalLayout;
        })).setSortable(true).setComparator(IReportModel::getTransactionType).setHeader("Tipo de Movimentação").setTextAlign(ColumnTextAlign.CENTER);
    }

    private void addContainerClienteColumn() {
        Grid.Column<IReportModel> containerCliente = grid.addColumn(new ComponentRenderer<>(transaction -> {
            HorizontalLayout horizontalLayout = new HorizontalLayout();
            horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            Span span = new Span();
            span.setClassName("containerClient");
            span.setText(transaction.getContainerClient().getName());
            horizontalLayout.add(span);
            return horizontalLayout;
        })).setHeader("Cliente").setTextAlign(ColumnTextAlign.CENTER);
    }

    private void addTotalMovColumn() {
        Grid.Column<IReportModel> movTotal = grid.addColumn(new ComponentRenderer<>(transaction -> {
            HorizontalLayout horizontalLayout = new HorizontalLayout();
            horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            Span span = new Span();
            span.setClassName("movTotal");
            span.setText(String.valueOf(transaction.getTotal()));
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

        Arrays.stream(TransactionType.values()).forEach(tipo -> {
            dataSeries.add(new DataSeriesItem(tipo.getValue(),transactionService.getMovimentacoesByTipo(tipo).size()));
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
        return transactionService.aggruped();
    }

    private long getTotalMovByImp() {
        return transactionService.totalByCatImportacao();
    }

    private long getTotalMovByExp() {
        return transactionService.totalByCatExportacao();
    }

    //  List of recent clients, containers and movements
    private List<Client> getRecentClients() {
        return clientService.findTop6ByOrderByCreatedAt().stream().map(genericEntity -> {
            Client response = new Client();
            BeanUtils.copyProperties(genericEntity,response);
            return response;
        }).collect(Collectors.toList());
    }

    private List<Container> getRecentContainers() {
        return containerService.findTop6ByOrderByCreatedAt().stream().map(genericEntity -> {
            Container response = new Container();
            BeanUtils.copyProperties(genericEntity,response);
            return response;
        }).collect(Collectors.toList());
    }

    private List<Transaction> getRecentMovimentacoes() {
        return transactionService.findTop6ByOrderByCreatedAt().stream().map(genericEntity -> {
            Transaction response = new Transaction();
            BeanUtils.copyProperties(genericEntity,response);
            return response;
        }).collect(Collectors.toList());
    }
}
