package com.ronilsonalves.gestaoporto.views.containers;

import com.ronilsonalves.gestaoporto.data.entity.Client;
import com.ronilsonalves.gestaoporto.data.entity.Container;
import com.ronilsonalves.gestaoporto.data.entity.GenericEntity;
import com.ronilsonalves.gestaoporto.data.enums.Category;
import com.ronilsonalves.gestaoporto.data.enums.Status;
import com.ronilsonalves.gestaoporto.data.enums.ContainerType;
import com.ronilsonalves.gestaoporto.data.repository.ClientRepository;
import com.ronilsonalves.gestaoporto.data.repository.ContainerRepository;
import com.ronilsonalves.gestaoporto.data.service.impl.GenericEntityServiceImpl;
import com.ronilsonalves.gestaoporto.views.MainLayout;
import com.ronilsonalves.gestaoporto.views.transactions.TransactionsView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.annotation.security.RolesAllowed;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@PageTitle("Gestão de Containers - Gestão do Porto")
@Route(value = "/containers/:containerID?/:action?(edit)", layout = MainLayout.class)
@Uses(Icon.class)
@RolesAllowed("ADMIN")
public class ContainersView extends Div implements BeforeEnterObserver {

    private final String CONTAINER_EDIT_ROUTE_TEMPLATE = "/containers/%s/edit";

    private Grid<Container> grid = new Grid<>(Container.class, false);
    private GridContextMenu<Container> menu = grid.addContextMenu();

    private ComboBox<Client> client;
    private TextField containerNumber;
    private ComboBox<ContainerType> containerType;
    private ComboBox<Status> status;
    private ComboBox<Category> category;

    private Button cancelBtn = new Button("Cancelar");
    private Button saveBtn = new Button("Salvar container");
    private Button deleteBtn = new Button("Excluir");

    private BeanValidationBinder<Container> binder;

    private Container container;

    private final GenericEntityServiceImpl containerService;
    private final GenericEntityServiceImpl clienteService;

    @Autowired
    public ContainersView(ContainerRepository repository, ClientRepository clientRepository) {
        this.containerService = new GenericEntityServiceImpl(repository) {};
        this.clienteService = new GenericEntityServiceImpl(clientRepository) {};

        addClassNames("containers-view");

        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        grid.addColumn("client").setAutoWidth(true);
        grid.addColumn("number").setAutoWidth(true);
        grid.addColumn("containerType").setAutoWidth(true);
        grid.addColumn("status").setAutoWidth(true);
        grid.addColumn("category").setAutoWidth(true);
        menu.addItem("Ver movimentações", event ->UI.getCurrent().navigate(TransactionsView.class));

        grid.setItems(query -> containerService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream().map(abstractEntity -> {
                    Container response = new Container();
                    BeanUtils.copyProperties(abstractEntity,response);
                    return response;
                }));
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(CONTAINER_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(ContainersView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(Container.class);

        // Validando os valores dos imputs
        binder.forField(containerNumber).withValidator(numeroContainer -> Pattern.matches("^[a-zA-Z]{4}[0-9]{7}$",numeroContainer),
                "Por favor, use uma sequência de 4 letras maiúsculas e 7 números. Ex.: CONT1234567")
                .bind(Container::getNumber,Container::setNumber);

        binder.bindInstanceFields(this);

        cancelBtn.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        deleteBtn.addClickListener(buttonClickEvent -> {
            try {
                binder.writeBean(this.container);
                containerService.delete(this.container.getId());
                clearForm();
                refreshGrid();
                Notification.show("Container excluído com sucesso!")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                UI.getCurrent().navigate(ContainersView.class);
            } catch (ValidationException ValidationException) {
                Notification.show("Ocorreu um erro ao tentar excluir o container.")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (DataIntegrityViolationException exception) {
                Notification.show("Não é possível excluir um container que possui movimentaçoes cadastradas.",
                                6000,Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        saveBtn.addClickListener(e  -> {
            try {
                if (this.container == null) {
                    this.container = new Container();
                }
                binder.writeBean(this.container);

                containerService.update(this.container);
                clearForm();
                refreshGrid();
                Notification.show("Os detalhes do Container foram salvos.")
                        .addThemeVariants(NotificationVariant.LUMO_PRIMARY);
                UI.getCurrent().navigate(ContainersView.class);
            } catch (ValidationException ValidationException) {
                Notification.show("Certifique-se de preencher todos os campos corretamente",6000, Notification.Position.BOTTOM_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String CONTAINER_ID = "containerID";
        Optional<UUID> containerId = event.getRouteParameters().get(CONTAINER_ID).map(UUID::fromString);
        if (containerId.isPresent()) {
            Optional<GenericEntity> containerFromBackend = containerService.get(containerId.get());
            if (containerFromBackend.isPresent()) {
                populateForm((Container) containerFromBackend.get());
            } else {
                Notification.show(
                        String.format("O container requisitado não foi encontrado, ID = %s", containerId.get()), 6000,
                        Notification.Position.BOTTOM_START).addThemeVariants(NotificationVariant.LUMO_ERROR);
                refreshGrid();
                event.forwardTo(ContainersView.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        client = new ComboBox<>("Doc. do Cliente");
        client.setItems(getClientList());
        client.setItemLabelGenerator(Client::getDocument);
        containerNumber = new TextField("Núm. do Container");
        containerType = new ComboBox<>("Container Type");
        containerType.setItems(ContainerType.values());
        containerType.setItemLabelGenerator(contType -> String.valueOf(contType.getValor()));
        status = new ComboBox<>("Status");
        status.setItems(Status.values());
        category = new ComboBox<>("Categoria");
        category.setItems(Category.values());
        Component[] fields = new Component[]{client,containerNumber,containerType,status,category};

        formLayout.add(fields);
        formLayout.setResponsiveSteps(
                // Use one column by default
                new FormLayout.ResponsiveStep("0", 1)
        );
        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        if(this.container == null) {
            deleteBtn.setEnabled(false);
        }
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(saveBtn, cancelBtn, deleteBtn);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        grid.appendFooterRow();
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getLazyDataView().refreshAll();
        clearForm();
    }

    private void clearForm() {
        populateForm(null);
        deleteBtn.setEnabled(false);
    }

    private void populateForm(Container value) {
        this.container = value;
        deleteBtn.setEnabled(true);
        binder.readBean(this.container);
    }

    private List<Client> getClientList() {
        return clienteService.list(Pageable.unpaged()).getContent().stream().map(genericEntity -> (Client) genericEntity).collect(Collectors.toList());
    }
}
