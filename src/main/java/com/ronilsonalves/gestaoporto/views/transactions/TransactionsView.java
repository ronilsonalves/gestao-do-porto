package com.ronilsonalves.gestaoporto.views.transactions;

import com.ronilsonalves.gestaoporto.data.entity.Container;
import com.ronilsonalves.gestaoporto.data.entity.Transaction;
import com.ronilsonalves.gestaoporto.data.enums.TransactionType;
import com.ronilsonalves.gestaoporto.data.repository.ContainerRepository;
import com.ronilsonalves.gestaoporto.data.repository.TransactionRepository;
import com.ronilsonalves.gestaoporto.data.service.impl.ContainerServiceImpl;
import com.ronilsonalves.gestaoporto.data.service.impl.GenericEntityServiceImpl;
import com.ronilsonalves.gestaoporto.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
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
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.annotation.security.RolesAllowed;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

@PageTitle("Gest??o de Movimenta????es - Gest??o do Porto")
@Route(value = "/transactions",layout = MainLayout.class)
@Uses(Icon.class)
@RolesAllowed("ADMIN")
public class TransactionsView extends Div {

    private GridPro<Transaction> grid = new GridPro<>(Transaction.class);
    private Grid.Column<Transaction> transactionTypeColumn;
    private Grid.Column<Transaction> startDateAndTimeColumn;
    private Grid.Column<Transaction> endDateAndTimeColumn;
    private Grid.Column<Transaction> containerNumberColumn;

    //Campos para o formul??rio de edi????o/cria????o
    private Select<TransactionType> transactionType;
    private DateTimePicker startDateAndTime;
    private DateTimePicker endDateAndTime;
    private Select<Container> containerNumber;

    private Button addTransaction;

    private BeanValidationBinder<Transaction> binder;
    private Transaction transaction;

    private final GenericEntityServiceImpl transactionService;
    private final ContainerServiceImpl containerService;


    public TransactionsView(TransactionRepository repository, ContainerRepository CRepository) {
        this.transactionService = new GenericEntityServiceImpl(repository) {
            @Override
            public int count() {
                return 0;
            }
        };
        this.containerService = new ContainerServiceImpl(CRepository) {};

        addClassName("transacoes-view");
        setSizeFull();
        setHeightFull();
        addTransaction = new Button("Adicionar Movimenta????o", new Icon(VaadinIcon.FILE_ADD), (add) -> {
            Dialog dialog = new Dialog();
            dialog.getElement().setAttribute("aria-label","Adicionar nova movimenta????o");

            VerticalLayout addLayout = createOrEdit(dialog,this.transaction);
            dialog.add(addLayout);
            dialog.setHeaderTitle("Adicionar nova movimenta????o");

            Button saveButton = new Button("Salvar",(salvar) -> {
                try {
                    if(this.transaction == null) {
                        this.transaction = new Transaction();
                    }
                    binder.writeBean(this.transaction);

                    transactionService.save(this.transaction);
                    refreshGrid();
                    Notification.show("Os detalhes da movimenta????o foran salvos com sucesso.");
                    dialog.close();
                    UI.getCurrent().navigate(TransactionsView.class);
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
        VerticalLayout novaMovimentacao = new VerticalLayout(addTransaction);
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
        grid.setItems(query -> transactionService.list(
                        PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream().map(genericEntity -> {
                    Transaction response = new Transaction();
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
            MenuItem menuItem = menuBar.addItem("?????????");
            menuItem.getElement().setAttribute("aria-label","Op????es");
            SubMenu subMenu = menuItem.getSubMenu();
            subMenu.addItem("Ver detalhes", menuItemClickEvent -> {
                Dialog dialog = new Dialog();
                dialog.getElement().setAttribute("aria-label","Visualizar detalhes");

                VerticalLayout dialogLayout = showDetaisOrDelete(dialog,movimentacao);
                dialog.add(dialogLayout);
                dialog.setHeaderTitle("Detalhes da movimenta????o");

                Button closeButton = new Button(new Icon("lumo","cross"), (e) -> dialog.close());
                closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                dialog.getHeader().add(closeButton);
                dialog.open();
                add(dialog);
            });
            subMenu.addItem("Editar movimenta????o", menuItemClickEvent -> {
                Dialog dialog = new Dialog();
                dialog.getElement().setAttribute("aria-label","Editar movimenta????o");

                VerticalLayout editLayout = createOrEdit(dialog,movimentacao);
                dialog.add(editLayout);
                dialog.setHeaderTitle("Editar movimenta????o");
                Button saveButton = new Button("Salvar",(salvar) -> {
                    try {
                        if(this.transaction == null) {
                            this.transaction = new Transaction();
                        }
                        binder.writeBean(this.transaction);

                        transactionService.update(this.transaction);
                        refreshGrid();
                        Notification.show("A movimenta????o foi atualizada.");
                        dialog.close();
                        UI.getCurrent().navigate(TransactionsView.class);
                    } catch (ValidationException validationException) {
                        Notification.show("Certifique-se de preencher todos os campos corretamente",
                                        4500, Notification.Position.BOTTOM_CENTER)
                                .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                });
                saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SUCCESS);
                saveButton.getStyle().set("margin-right","auto");
                dialog.getFooter().add(saveButton);

                Button cancelButton = new Button("Cancelar", (cancel) -> {
                    dialog.close();
                    refreshGrid();
                });
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
                dialog.setHeaderTitle(String.format("Excluir movimenta????o do Container \"%s\" ?",movimentacao.getContainer().getNumber()));
                Button deleteButton = new Button("Excluir", (delete) -> {
                    try {
                        this.transactionService.delete(movimentacao.getId());
                    } catch (DataIntegrityViolationException e) {
                        throw new RuntimeException(e);
                    }
                    dialog.close();
                    refreshGrid();
                    UI.getCurrent().navigate(TransactionsView.class);
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
        }).setFlexGrow(0).setAutoWidth(true).setHeader("Op????es");
    }

    private void createMovimentationTypeColumn() {
        transactionTypeColumn = grid.addColumn(new ComponentRenderer<>(movimentacao -> {
            HorizontalLayout horizontalLayout = new HorizontalLayout();
            horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            Span span = new Span();
            span.setClassName("transactionTypeColumn");
            span.setText(movimentacao.getTransactionType().getValue());
            horizontalLayout.add(span);
            return horizontalLayout;
        })).setSortable(false).setComparator(Transaction::getTransactionType).setHeader("Tipo de Movimenta????o");
    }

    private void createStartDateAndTimeColumn() {
        startDateAndTimeColumn = grid.addColumn(new LocalDateTimeRenderer<>(Transaction::getStartDateTime,
                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                .setSortable(false)
                .setComparator(Transaction::getStartDateTime).setHeader("Data&Hora de In??cio");
    }

    private void createEndDateAndTimeColumn() {
        endDateAndTimeColumn = grid.addColumn(new LocalDateTimeRenderer<>(Transaction::getEndDateTime,
                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                .setSortable(false)
                .setComparator(Transaction::getEndDateTime).setHeader("Data&Hora de T??rmino");
    }

    private void createContainerNumberColumn() {
        containerNumberColumn = grid.addColumn(new ComponentRenderer<>(movimentacao -> {
            HorizontalLayout horizontalLayout = new HorizontalLayout();
            horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            Span span = new Span();
            span.setClassName("containerNumberColumn");
            span.setText(movimentacao.getContainer().getNumber());
            horizontalLayout.add(span);
            return horizontalLayout;
        })).setSortable(false).setComparator(movimentacao -> movimentacao.getContainer().getNumber()).setHeader("N??m. do Container");
    }

    private VerticalLayout showDetaisOrDelete(Dialog dialog, Transaction transaction) {
        TextField movType = new TextField("Tipo de Movimenta????o", transaction.getTransactionType().getValue(), transaction.getTransactionType().getValue());
        movType.setReadOnly(true);

        TextField movDateStart = new TextField("Data&Hora de In??cio", transaction.getStartDateTime().toString(), transaction.getStartDateTime().toString());
        movDateStart.setVisible(true);
        movDateStart.setReadOnly(true);

        TextField movDateEnd = new TextField("Data&Hora de T??rmino", transaction.getEndDateTime().toString(), transaction.getEndDateTime().toString());
        movDateEnd.setReadOnly(true);

        TextField movContainer = new TextField("N??m do Container", transaction.getContainer().getNumber(), transaction.getContainer().getNumber());
        movContainer.setReadOnly(true);

        TextField movClient = new TextField("Cliente/Doc", transaction.getContainer().getClient().getName()+"/"+transaction.getContainer().getClient().getDocument(),
                transaction.getContainer().getClient().getName()+"/"+transaction.getContainer().getClient().getDocument());
        movClient.setReadOnly(true);

        TextField movCategoria = new TextField("Categoria", transaction.getContainer().getCategory().toString(), transaction.getContainer().getCategory().toString());
        movCategoria.setReadOnly(true);

        VerticalLayout fieldLayout = new VerticalLayout(movType,movDateStart,movDateEnd,movContainer,movClient,movCategoria);
        fieldLayout.setSpacing(false);
        fieldLayout.setPadding(false);
        fieldLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        fieldLayout.getStyle().set("width","480px").set("max-width","100%");

        return fieldLayout;
    }

    private VerticalLayout createOrEdit(Dialog dialog, Transaction transaction) {

        binder = new BeanValidationBinder<>(Transaction.class);

        if(transaction != null) {
            this.transaction = transaction;
            binder.readBean(this.transaction);
        }

        FormLayout formLayout = new FormLayout();
        transactionType = new Select<>();
        transactionType.setLabel("Tipo de Movimenta????o");
        transactionType.setItems(TransactionType.values());
        transactionType.setItemLabelGenerator(TransactionType::getValue);
        startDateAndTime = new DateTimePicker("Data&Hora de In??cio");
        startDateAndTime.setMax(LocalDateTime.now());
        endDateAndTime = new DateTimePicker("Data&Hora do Fim");
        endDateAndTime.setMax(LocalDateTime.now());
        containerNumber = new Select<>();
        containerNumber.setLabel("N??m. do Container");
        containerNumber.setItems(containerService.list(Pageable.unpaged()).map(genericEntity -> {
            Container container = new Container();
            BeanUtils.copyProperties(genericEntity,container);
            return container;
        }).toList());
        containerNumber.setItemLabelGenerator(Container::getNumber);
        //containerNumber.setPattern("^[a-zA-Z]{4}[0-9]{7}$");
        containerNumber.setHelperText("Use uma sequ??ncia de 4 letras e 7 n??meros. Ex.: CONT1234567");
        //Condicional para o form de cadastro.
        if(transaction != null) {
            transactionType.setValue(transaction.getTransactionType());
            startDateAndTime.setValue(transaction.getStartDateTime());
            endDateAndTime.setValue(transaction.getEndDateTime());
            containerNumber.setValue(transaction.getContainer());
        }

        Component[] fields = new Component[]{transactionType,startDateAndTime,endDateAndTime, containerNumber};

        // Bind fields. This is where you'd define e.g. validation rules
        binder.forField(startDateAndTime).withValidator(startDateTime -> !(startDateTime.isAfter(LocalDateTime.now())),
                "A data e hor??rio de in??cio n??o podem ser posteriores a hoje").bind(Transaction::getStartDateTime, Transaction::setStartDateTime);
        binder.forField(endDateAndTime).withValidator(endDateTime ->!(LocalDateTime.now().isBefore(endDateTime)),
                "A data e hor??rio do fim n??o podem ser posteriores a hoje").withValidator(endDateTime -> !(endDateTime.isBefore(startDateAndTime.getValue())),
                "O final da operac??o n??o pode ser anterior ao in??cio da opera????o").bind(Transaction::getEndDateTime, Transaction::setEndDateTime);

        binder.forField(containerNumber).withValidator(numero -> (Pattern.matches("^[a-zA-Z]{4}[0-9]{7}$",numero.getNumber())),
                        "Por favor, use uma sequ??ncia de 4 letras e 7 n??meros. Ex.: CONT1234567")
                .bind(Transaction::getContainer, Transaction::setContainer);
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
        this.transaction = null;
        grid.getLazyDataView().refreshAll();
    }
}
