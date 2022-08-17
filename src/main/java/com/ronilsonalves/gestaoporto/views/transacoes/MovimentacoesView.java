package com.ronilsonalves.gestaoporto.views.transacoes;

import com.ronilsonalves.gestaoporto.data.entity.Container;
import com.ronilsonalves.gestaoporto.data.entity.Movimentacao;
import com.ronilsonalves.gestaoporto.data.enums.TipoMovimentacao;
import com.ronilsonalves.gestaoporto.data.repository.ContainerRepository;
import com.ronilsonalves.gestaoporto.data.repository.MovimentacaoRepository;
import com.ronilsonalves.gestaoporto.data.service.impl.ContainerServiceImpl;
import com.ronilsonalves.gestaoporto.data.service.impl.GenericEntityServiceImpl;
import com.ronilsonalves.gestaoporto.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.gridpro.GridPro;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.LazyDataView;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@PageTitle("Gestão de Movimentações - Gestão do Porto")
@Route(value = "/movimentacoes",layout = MainLayout.class)
@Uses(Icon.class)
public class MovimentacoesView extends Div {

    private GridPro<Movimentacao> grid = new GridPro<>(Movimentacao.class);
    private GridListDataView<Movimentacao> movimentacaoDataView;
    private Grid.Column<Movimentacao> movimentationType;
    private Grid.Column<Movimentacao> startDateAndTime;
    private Grid.Column<Movimentacao> endDateAndTime;
    private Grid.Column<Movimentacao> containerNumber;

    //Campos para o formulário de edição/criação
    private Select<TipoMovimentacao> tipoDeMovimentacao;
    private DateTimePicker dataHoraDeInicio;
    private DateTimePicker dataHoraDeFim;
    private Select<Container> numeroDoContainer;

    private Button addMovimentacao;

    private BeanValidationBinder<Movimentacao> binder;
    private Movimentacao movimentacao;

    private final GenericEntityServiceImpl movimentacaoService;
    private final ContainerServiceImpl containerService;


    public MovimentacoesView(MovimentacaoRepository repository, ContainerRepository CRepository) {
        this.movimentacaoService = new GenericEntityServiceImpl(repository) {
            @Override
            public int count() {
                return 0;
            }
        };
        this.containerService = new ContainerServiceImpl(CRepository) {};

        addClassName("transacoes-view");
        setSizeFull();
        setHeightFull();
        addMovimentacao = new Button("Adicionar Movimentação", new Icon(VaadinIcon.FILE_ADD), (add) -> {
            Dialog dialog = new Dialog();
            dialog.getElement().setAttribute("aria-label","Adicionar nova movimentação");

            VerticalLayout addLayout = createOrEdit(dialog,this.movimentacao);
            dialog.add(addLayout);
            dialog.setHeaderTitle("Adicionar nova movimentação");

            Button saveButton = new Button("Salvar",(salvar) -> {
                try {
                    if(this.movimentacao == null) {
                        this.movimentacao = new Movimentacao();
                    }
                    binder.writeBean(this.movimentacao);

                    movimentacaoService.save(this.movimentacao);
                    refreshGrid();
                    Notification.show("Os detalhes da movimentação foran salvos com sucesso.");
                    dialog.close();
                    UI.getCurrent().navigate(MovimentacoesView.class);
                } catch (ValidationException validationException) {
                    Notification.show("Certifique-se de preencher todos os campos corretamente",
                                    4500, Notification.Position.BOTTOM_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            });
            saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SUCCESS);
            saveButton.getStyle().set("margin-right","auto");
            dialog.getFooter().add(saveButton);

            Button cancelButton = new Button("Cancelar", (cancel) -> dialog.close());
            cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            dialog.getFooter().add(cancelButton);
            dialog.open();
            add(dialog);
        });
        VerticalLayout novaMovimentacao = new VerticalLayout(addMovimentacao);
        novaMovimentacao.setAlignItems(FlexComponent.Alignment.CENTER);
        createGrid();
        add(novaMovimentacao,grid);
    }

    private void createGrid() {
        createGridComponent();
        addColumnsToGrid();
    }

    private void createGridComponent() {
        grid = new GridPro<>();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_COLUMN_BORDERS);

        //List<Movimentacao> movimentacoes = getTransactions();
        grid.setItems(query -> movimentacaoService.list(
                        PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream().map(genericEntity -> {
                    Movimentacao response = new Movimentacao();
                    BeanUtils.copyProperties(genericEntity,response);
                    return response;
                }));
        grid.setHeightFull();
    }

    private void addColumnsToGrid() {
        createActionColumn();
        createMovimentationTypeColumn();
        createStartDateAndTimeColumn();
        createEndDateAndTimeColumn();
        createContainerNumberColumn();
    }

    private void createActionColumn() {
        grid.addComponentColumn(movimentacao -> {
            MenuBar menuBar = new MenuBar();
            menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY);
            MenuItem menuItem = menuBar.addItem("•••");
            menuItem.getElement().setAttribute("aria-label","Opções");
            SubMenu subMenu = menuItem.getSubMenu();
            subMenu.addItem("Ver detalhes", menuItemClickEvent -> {
                Dialog dialog = new Dialog();
                dialog.getElement().setAttribute("aria-label","Visualizar detalhes");

                VerticalLayout dialogLayout = showDetaisOrDelete(dialog,movimentacao);
                dialog.add(dialogLayout);
                dialog.setHeaderTitle("Detalhes da movimentação");

                Button closeButton = new Button(new Icon("lumo","cross"), (e) -> dialog.close());
                closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                dialog.getHeader().add(closeButton);
                dialog.open();
                add(dialog);
            });
            subMenu.addItem("Editar movimentação", menuItemClickEvent -> {
                Dialog dialog = new Dialog();
                dialog.getElement().setAttribute("aria-label","Editar movimentação");

                VerticalLayout editLayout = createOrEdit(dialog,movimentacao);
                dialog.add(editLayout);
                dialog.setHeaderTitle("Editar movimentação");
                Button saveButton = new Button("Salvar",(salvar) -> {
                    try {
                        if(this.movimentacao == null) {
                            this.movimentacao = new Movimentacao();
                        }
                        binder.writeBean(this.movimentacao);

                        movimentacaoService.update(this.movimentacao);
                        refreshGrid();
                        Notification.show("A movimentação foi atualizada.");
                        dialog.close();
                        UI.getCurrent().navigate(MovimentacoesView.class);
                    } catch (ValidationException validationException) {
                        Notification.show("Certifique-se de preencher todos os campos corretamente",
                                        4500, Notification.Position.BOTTOM_CENTER)
                                .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                });
                saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SUCCESS);
                saveButton.getStyle().set("margin-right","auto");
                dialog.getFooter().add(saveButton);

                Button cancelButton = new Button("Cancelar", (cancel) -> dialog.close());
                cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                dialog.getFooter().add(cancelButton);
                dialog.open();
                add(dialog);
            });
            subMenu.addItem("Apagar", menuItemClickEvent -> {
                Dialog dialog = new Dialog();
                dialog.getElement().setAttribute("arial-label","Excluir movimentacao");

                VerticalLayout dialogLayout = showDetaisOrDelete(dialog,movimentacao);
                dialog.add(dialogLayout);
                dialog.setHeaderTitle(String.format("Excluir movimentação do Container \"%s\" ?",movimentacao.getContainer().getNumero()));
                Button deleteButton = new Button("Excluir", (delete) -> {
                    try {
                        this.movimentacaoService.delete(movimentacao.getId());
                    } catch (DataIntegrityViolationException e) {
                        throw new RuntimeException(e);
                    }
                    dialog.close();
                    refreshGrid();
                    UI.getCurrent().navigate(MovimentacoesView.class);
                });
                deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_ERROR);
                deleteButton.getStyle().set("margin-right", "auto");
                dialog.getFooter().add(deleteButton);

                Button cancelButton = new Button("Cancelar", (cancel) -> dialog.close());
                cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                dialog.getFooter().add(cancelButton);
                dialog.open();
                add(dialog);
            });
            menuBar.setEnabled(true);
            return menuBar;
        }).setFlexGrow(0).setAutoWidth(true).setHeader("Opções");
    }

    private void createMovimentationTypeColumn() {
        movimentationType = grid.addColumn(new ComponentRenderer<>(movimentacao -> {
            HorizontalLayout horizontalLayout = new HorizontalLayout();
            horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            Span span = new Span();
            span.setClassName("movimentationType");
            span.setText(movimentacao.getTipoDeMovimentacao().getValue());
            horizontalLayout.add(span);
            return horizontalLayout;
        })).setSortable(false).setComparator(Movimentacao::getTipoDeMovimentacao).setHeader("Tipo de Movimentação");
    }

    private void createStartDateAndTimeColumn() {
        startDateAndTime = grid.addColumn(new LocalDateTimeRenderer<>(Movimentacao::getDataHoraDeInicio,
                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                .setSortable(false)
                .setComparator(Movimentacao::getDataHoraDeInicio).setHeader("Data&Hora de Início");
    }

    private void createEndDateAndTimeColumn() {
        endDateAndTime = grid.addColumn(new LocalDateTimeRenderer<>(Movimentacao::getDataHoraDeFim,
                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                .setSortable(false)
                .setComparator(Movimentacao::getDataHoraDeFim).setHeader("Data&Hora de Término");
    }

    private void createContainerNumberColumn() {
        containerNumber = grid.addColumn(new ComponentRenderer<>(movimentacao -> {
            HorizontalLayout horizontalLayout = new HorizontalLayout();
            horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            Span span = new Span();
            span.setClassName("containerNumber");
            span.setText(movimentacao.getContainer().getNumero());
            horizontalLayout.add(span);
            return horizontalLayout;
        })).setSortable(false).setComparator(movimentacao -> movimentacao.getContainer().getNumero()).setHeader("Núm. do Container");
    }

    private VerticalLayout showDetaisOrDelete(Dialog dialog,Movimentacao movimentacao) {
        TextField movType = new TextField("Tipo de Movimentação",movimentacao.getTipoDeMovimentacao().getValue(),movimentacao.getTipoDeMovimentacao().getValue());
        movType.setReadOnly(true);

        TextField movDateStart = new TextField("Data&Hora de Início",movimentacao.getDataHoraDeInicio().toString(),movimentacao.getDataHoraDeInicio().toString());
        movDateStart.setVisible(true);
        movDateStart.setReadOnly(true);

        TextField movDateEnd = new TextField("Data&Hora de Término",movimentacao.getDataHoraDeFim().toString(),movimentacao.getDataHoraDeFim().toString());
        movDateEnd.setReadOnly(true);

        TextField movContainer = new TextField("Núm do Container",movimentacao.getContainer().getNumero(),movimentacao.getContainer().getNumero());
        movContainer.setReadOnly(true);

        TextField movClient = new TextField("Cliente",movimentacao.getContainer().getCliente(),movimentacao.getContainer().getCliente());
        movClient.setReadOnly(true);

        TextField movCategoria = new TextField("Categoria",movimentacao.getContainer().getCategoria().toString(),movimentacao.getContainer().getCategoria().toString());
        movCategoria.setReadOnly(true);

        VerticalLayout fieldLayout = new VerticalLayout(movType,movDateStart,movDateEnd,movContainer,movClient,movCategoria);
        fieldLayout.setSpacing(false);
        fieldLayout.setPadding(false);
        fieldLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        fieldLayout.getStyle().set("width","480px").set("max-width","100%");

        return fieldLayout;
    }

    private VerticalLayout createOrEdit(Dialog dialog, Movimentacao movimentacao) {

        binder = new BeanValidationBinder<>(Movimentacao.class);

        if(movimentacao != null) {
            this.movimentacao = movimentacao;
            binder.readBean(this.movimentacao);
        }

        FormLayout formLayout = new FormLayout();
        tipoDeMovimentacao = new Select<>();
        tipoDeMovimentacao.setLabel("Tipo de Movimentação");
        tipoDeMovimentacao.setItems(TipoMovimentacao.values());
        tipoDeMovimentacao.setItemLabelGenerator(TipoMovimentacao::getValue);
        dataHoraDeInicio = new DateTimePicker("Data&Hora de Início");
        dataHoraDeInicio.setMax(LocalDateTime.now());
        dataHoraDeFim = new DateTimePicker("Data&Hora do Fim");
        dataHoraDeFim.setMax(LocalDateTime.now());
        numeroDoContainer = new Select<>();
        numeroDoContainer.setLabel("Núm. do Container");
        numeroDoContainer.setItems(containerService.list(Pageable.unpaged()).map(genericEntity -> {
            Container container = new Container();
            BeanUtils.copyProperties(genericEntity,container);
            return container;
        }).toList());
        numeroDoContainer.setItemLabelGenerator(Container::getNumero);
        //numeroDoContainer.setPattern("^[a-zA-Z]{4}[0-9]{7}$");
        numeroDoContainer.setHelperText("Use uma sequência de 4 letras e 7 números. Ex.: CONT1234567");
        //Condicional para o form de cadastro.
        if(movimentacao != null) {
            tipoDeMovimentacao.setValue(movimentacao.getTipoDeMovimentacao());
            dataHoraDeInicio.setValue(movimentacao.getDataHoraDeInicio());
            dataHoraDeFim.setValue(movimentacao.getDataHoraDeFim());
            numeroDoContainer.setValue(movimentacao.getContainer());
        }

        Component[] fields = new Component[]{tipoDeMovimentacao,dataHoraDeInicio,dataHoraDeFim, numeroDoContainer};

        // Bind fields. This is where you'd define e.g. validation rules
        binder.forField(dataHoraDeInicio).withValidator(startDateTime -> !(startDateTime.isAfter(LocalDateTime.now())),
                "A data e horário de início não podem ser posteriores a hoje").bind(Movimentacao::getDataHoraDeInicio, Movimentacao::setDataHoraDeInicio);
        binder.forField(dataHoraDeFim).withValidator(endDateTime ->!(LocalDateTime.now().isBefore(endDateTime)),
                "A data e horário do fim não podem ser posteriores a hoje").withValidator(endDateTime -> !(endDateTime.isBefore(dataHoraDeInicio.getValue())),
                "O final da operacão não pode ser anterior ao início da operação").bind(Movimentacao::getDataHoraDeFim, Movimentacao::setDataHoraDeFim);

        binder.forField(numeroDoContainer).withValidator(numero -> (Pattern.matches("^[a-zA-Z]{4}[0-9]{7}$",numero.getNumero())),
                        "Por favor, use uma sequência de 4 letras e 7 números. Ex.: CONT1234567")
                .bind(Movimentacao::getContainer,Movimentacao::setContainer);
        binder.bindInstanceFields(this);

        formLayout.add(fields);

        VerticalLayout fieldLayout = new VerticalLayout(formLayout);
        fieldLayout.setSpacing(false);
        fieldLayout.setPadding(false);
        fieldLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        fieldLayout.getStyle().set("width","480px").set("max-width","100%");

        return fieldLayout;
    }

    private void refreshGrid() {
        this.movimentacao = null;
        grid.getLazyDataView().refreshAll();
    }
}
