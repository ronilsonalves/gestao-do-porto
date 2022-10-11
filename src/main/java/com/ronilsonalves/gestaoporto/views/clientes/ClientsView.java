package com.ronilsonalves.gestaoporto.views.clientes;

import com.ronilsonalves.gestaoporto.data.entity.Client;
import com.ronilsonalves.gestaoporto.data.entity.Address;
import com.ronilsonalves.gestaoporto.data.enums.State;
import com.ronilsonalves.gestaoporto.data.repository.AddressRepository;
import com.ronilsonalves.gestaoporto.data.repository.ClientRepository;
import com.ronilsonalves.gestaoporto.data.repository.ContainerRepository;
import com.ronilsonalves.gestaoporto.data.service.impl.AddressServiceImpl;
import com.ronilsonalves.gestaoporto.data.service.impl.ClientServiceImpl;
import com.ronilsonalves.gestaoporto.data.service.impl.ContainerServiceImpl;
import com.ronilsonalves.gestaoporto.data.service.impl.GenericEntityServiceImpl;
import com.ronilsonalves.gestaoporto.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.gridpro.GridPro;
import com.vaadin.flow.component.gridpro.GridProVariant;
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
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.regex.Pattern;


@PageTitle("Clients - Gestão do Porto")
@Route(value = "/clients", layout = MainLayout.class)
@Uses(Icon.class)
public class ClientsView extends Div {

    private GridPro<Client> grid = new GridPro<>(Client.class);

    private Grid.Column<Client> clientName;
    private Grid.Column<Client> clientSurname;
    private Grid.Column<Client> clientDocument;
    private Grid.Column<Client> clientBirthdate;
    private Grid.Column<Client> clientEmail;
    private Grid.Column<Client> clientPhone;
    private Grid.Column<Client> clientAddress;

    private TextField clientNameField;
    private TextField clientSurnameField;
    private TextField clientDocumentField;
    private DatePicker clientBirthdateField;
    private EmailField clientEmailField;
    private TextField clientPhoneField;
    private TextField cliLogAddress;
    private TextField cliNumAddress;
    private TextField cliZipAddress;
    private TextField cliNeighborhoodAddress;
    private TextField cliCityAddress;
    private Select<State> cliStateAddress;

    private Button addClientBtn;

    private BeanValidationBinder<Client> clientBinder;
    private BeanValidationBinder<Address> cliAddressBinder;

    private Client client;
    private Address address;

    private final ClientServiceImpl clientService;
    private final AddressServiceImpl addressService;
    private final GenericEntityServiceImpl containerService;

    public ClientsView(ClientRepository repository, AddressRepository endRepository, ContainerRepository contRepository) {
        this.addressService = new AddressServiceImpl(endRepository);
        this.clientService = new ClientServiceImpl(repository,addressService);
        this.containerService = new ContainerServiceImpl(contRepository);

        addClassName("clients-view");
        setSizeFull();

        addClientBtn = new Button("Adicionar Cliente", new Icon(VaadinIcon.USER), (add) -> {
            Dialog dialog = new Dialog();
            dialog.getElement().setAttribute("aria-label","Adicionar novo cliente");

            VerticalLayout addLayout = createOrEdit(dialog,this.client);
            dialog.add(addLayout);
            dialog.setHeaderTitle("Adicioanr novo cliente");

            Button saveBtn = new Button("Salvar", (save) -> {
                try {
                    if(this.client == null) {
                        this.client = new Client();
                        this.address = new Address();
                    }
                    cliAddressBinder.writeBean(this.address);
                    clientBinder.writeBean(this.client);
                    this.client.setAddress(this.address);
                    System.out.println(formatAddress(this.client));

                    clientService.save(this.client);
                    refreshGrid();
                    Notification.show("Novo cliente salvo com sucesso!", 3000, Notification.Position.BOTTOM_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    dialog.close();
                    UI.getCurrent().navigate(ClientsView.class);
                } catch (ValidationException validationException) {
                    Notification.show("Certifique-se de preencher todos os campos corretamente",
                            4500, Notification.Position.BOTTOM_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            });
            saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SUCCESS);
            saveBtn.getStyle().set("margin-right","auto");
            dialog.getFooter().add(saveBtn);

            Button cancelBtn = new Button("Cancelar", (cancel) -> dialog.close());
            cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            dialog.getFooter().add(cancelBtn);
            dialog.open();
            add(dialog);
        });

        VerticalLayout newClient = new VerticalLayout(addClientBtn);
        newClient.setAlignItems(FlexComponent.Alignment.CENTER);
        createGrid();
        add(newClient,grid);
    }

    private void createGrid() {
        createGridComponent();
        addColumnsToGrid();
    }

    private void createGridComponent() {
        grid = new GridPro<>();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addThemeVariants(GridProVariant.LUMO_NO_BORDER,GridProVariant.LUMO_COLUMN_BORDERS);
        grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);

        grid.setItems(query -> clientService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query))
        ).stream().map(genericEntity -> {
            Client response = new Client();
            BeanUtils.copyProperties(genericEntity,response);
            return response;
        }));
        grid.setHeightFull();
    }

    private void addColumnsToGrid() {
        createActionColumn();
        createClientNameColumn();
        createClientSurnameColumn();
        createClientDocumentColumn();
        createClientBirthdateColumn();
        createClientEmailColumn();
        createClientPhoneColumn();
        createClientAddressColumn();
    }

    private void createActionColumn() {
        //Falta construir os métodos das opções deste actionMenu
        grid.addComponentColumn(client -> {
            MenuBar menuBar = new MenuBar();
            menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY);
            MenuItem menuItem = menuBar.addItem("•••");
            menuItem.getElement().setAttribute("aria-label","Opções");
            SubMenu subMenu = menuItem.getSubMenu();
            subMenu.addItem("Ver detalhes", menuItemClickEvent -> {
                Dialog dialog = new Dialog();
                dialog.getElement().setAttribute("aria-label","Detalhes do cliente");

                VerticalLayout detailsLayout = showDetailsOrDelete(dialog,client);
                dialog.add(detailsLayout);
                dialog.setHeaderTitle("Detalhes do cliente");

                Button closeBtn = new Button(new Icon("lumo", "cross"), (close) -> dialog.close());
                closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                dialog.getHeader().add(closeBtn);
                dialog.open();
                add(dialog);
            });
            subMenu.addItem("Editar cliente",menuItemClickEvent -> {
                Dialog dialog = new Dialog();
                dialog.getElement().setAttribute("aria-label","Editar cliente");

                VerticalLayout editLayout = createOrEdit(dialog,client);
                dialog.add(editLayout);
                dialog.setHeaderTitle("Editar cliente");
                Button saveButton = new Button("Salvar", (save) -> {
                    try {
                        if(this.client == null) {
                            this.client = new Client();
                            this.address = new Address();
                        }
                        clientBinder.writeBean(this.client);
                        cliAddressBinder.writeBean(this.address);
                        clientService.save(this.client);
                        refreshGrid();
                        Notification.show("Cliente atualizado com sucesso!",
                                        4500, Notification.Position.BOTTOM_CENTER)
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        UI.getCurrent().navigate(ClientsView.class);
                        dialog.close();
                    } catch (ValidationException validationException) {
                        Notification.show("Certifique-se de preencher todos os campos corretamente",
                                4500, Notification.Position.BOTTOM_CENTER)
                                .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                });
                saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SUCCESS);
                saveButton.getStyle().set("margin-right","auto");
                dialog.getFooter().add(saveButton);

                Button cancelButton = new Button("Cancelar edição", (cancel) -> {
                    dialog.close();
                    refreshGrid();
                });
                cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                dialog.getFooter().add(cancelButton);
                dialog.open();
                add(dialog);
            });
            subMenu.addItem("Excluir cliente",menuItemClickEvent -> {
                Dialog dialog = new Dialog();
                dialog.getElement().setAttribute("aria-label", "Excluir cliente");

                VerticalLayout deleteLayout = showDetailsOrDelete(dialog,client);
                dialog.add(deleteLayout);
                dialog.setHeaderTitle(String.format("Excluir cliente com documento: \"%s\" ?", client.getDocumento()));
                Button deleteButton = new Button("Excluir cliente", (delete) -> {
                    try {
                        this.clientService.delete(client.getId());
                        dialog.close();
                        refreshGrid();
                        Notification.show("Cliente excluído com sucesso!")
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        UI.getCurrent().navigate(ClientsView.class);
                    } catch (DataIntegrityViolationException e) {
                        Notification.show("Não é possível excluir um cliente que possui containers/movimentaçoes cadastradas.",
                                        6000,Notification.Position.BOTTOM_START)
                                .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                });
                deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_ERROR);
                deleteButton.getStyle().set("margin-right", "auto");
                dialog.getFooter().add(deleteButton);

                Button cancelButton = new Button("Cancelar exclusão", (cancel) -> dialog.close());
                cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                dialog.getFooter().add(cancelButton);
                dialog.open();
                add(dialog);
            });
            menuBar.setEnabled(true);
            return menuBar;
        }).setFlexGrow(0).setAutoWidth(true).setHeader("Opções");
    }

    private void createClientNameColumn() {
        clientName = grid.addColumn(new ComponentRenderer<>(client -> {
            HorizontalLayout horizontalLayout = new HorizontalLayout();
            horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            Span span = new Span();
            span.setClassName("clientName");
            span.setText(client.getNome());
            horizontalLayout.add(span);
            return horizontalLayout;
        })).setSortable(true).setComparator(Client::getNome).setHeader("Nome");
    }

    private void createClientSurnameColumn() {
        clientSurname = grid.addColumn(new ComponentRenderer<>(client -> {
            HorizontalLayout horizontalLayout = new HorizontalLayout();
            horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            Span span = new Span();
            span.setClassName("clientSurname");
            span.setText(client.getSobrenome());
            horizontalLayout.add(span);
            return horizontalLayout;
        })).setSortable(true).setComparator(Client::getSobrenome).setHeader("Sobrenome");
    }

    private void createClientDocumentColumn() {
        clientDocument = grid.addColumn(new ComponentRenderer<>(client -> {
            HorizontalLayout horizontalLayout = new HorizontalLayout();
            horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            Span span = new Span();
            span.setClassName("clientDocument");
            span.setText(client.getDocumento());
            horizontalLayout.add(span);
            return horizontalLayout;
        })).setSortable(true).setComparator(Client::getDocumento).setHeader("CNPJ/CPF");
    }

    private void createClientBirthdateColumn() {
        clientBirthdate = grid.addColumn(new LocalDateRenderer<>(Client::getDataDeNascimento,
                DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setSortable(true)
                .setComparator(Client::getDataDeNascimento).setHeader("Data de Nascimento");
    }

    private void createClientEmailColumn() {
        clientEmail = grid.addColumn(new ComponentRenderer<>(client -> {
            HorizontalLayout horizontalLayout = new HorizontalLayout();
            horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            Span span = new Span();
            span.setClassName("clientEmail");
            span.setText(client.getEmail());
            horizontalLayout.add(span);
            return horizontalLayout;
        })).setSortable(true).setComparator(Client::getEmail).setHeader("Email");
    }

    private void createClientPhoneColumn() {
        clientPhone = grid.addColumn(new ComponentRenderer<>(client -> {
            HorizontalLayout horizontalLayout = new HorizontalLayout();
            horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            Span span = new Span();
            span.setClassName("clientPhone");
            span.setText(client.getTelefone());
            horizontalLayout.add(span);
            return horizontalLayout;
        })).setSortable(true).setComparator(Client::getTelefone).setHeader("Telefone");
    }

    private void createClientAddressColumn() {
        clientAddress = grid.addColumn(new ComponentRenderer<>(client -> {
            HorizontalLayout horizontalLayout = new HorizontalLayout();
            Span span = new Span();
            span.setClassName("clientAddress");
            span.setText(formatAddress(client));
            horizontalLayout.add(span);
            return horizontalLayout;
        })).setSortable(true).setHeader("Endereço");
    }

    private String formatAddress(Client client) {
        Address address = client.getAddress();

        return String.format("%s, %s, %s, %s - %s, %s",address.getLogradouro(),
                address.getNumero(),address.getBairro(),address.getCidade(),address.getEstado(),
                address.getCEP());
    }

    private VerticalLayout showDetailsOrDelete(Dialog dialog, Client client) {
        TextField name = new TextField("Nome",client.getNome(),client.getNome());
        name.setReadOnly(true);

        TextField surname = new TextField("Sobrenome",client.getSobrenome(),client.getSobrenome());
        surname.setReadOnly(true);

        TextField document = new TextField("Documento",client.getDocumento(),client.getDocumento());
        document.setReadOnly(true);

        TextField birthdate = new TextField("Data de Nascimento",client.getDataDeNascimento().toString(),client.getDataDeNascimento().toString());
        birthdate.setReadOnly(true);

        TextField email = new TextField("Email",client.getEmail(),client.getEmail());
        email.setReadOnly(true);

        TextField phone = new TextField("Telefone",client.getTelefone(),client.getTelefone());
        phone.setReadOnly(true);

        TextField address = new TextField("Endereço",formatAddress(client),formatAddress(client));
        address.setReadOnly(true);

        VerticalLayout fieldLayout = new VerticalLayout(name,surname,document,birthdate,email,phone,address);
        fieldLayout.setSpacing(false);
        fieldLayout.setPadding(false);
        fieldLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        fieldLayout.getStyle().set("width","480px").set("max-width","100%");

        return fieldLayout;
    }

    private VerticalLayout createOrEdit(Dialog dialog, Client client) {
        Address clientAddress = new Address();
        clientBinder = new BeanValidationBinder<>(Client.class);
        cliAddressBinder = new BeanValidationBinder<>(Address.class);

        if (client != null) {
            this.client = client;
            this.address = client.getAddress();
            clientBinder.readBean(this.client);
            cliAddressBinder.readBean(this.client.getAddress());
            clientAddress = client.getAddress();
        }

        FormLayout formLayout = new FormLayout();
        clientNameField = new TextField("Nome");
        clientSurnameField = new TextField("Sobrenome");
        clientDocumentField = new TextField("Documento (CNPJ/CPF)");
        clientBirthdateField = new DatePicker("Data de Nascimento");
        clientBirthdateField.setPlaceholder("dd/mm/aaaa");
        clientBirthdateField.setMax(LocalDate.now().minus(18, ChronoUnit.YEARS));
        clientEmailField = new EmailField("Email");
        clientPhoneField = new TextField("Telefone");
        //clientPhoneField.setPattern("^[]");
        cliLogAddress = new TextField("Logradouro");
        cliNumAddress = new TextField("Número");
        cliNeighborhoodAddress = new TextField("Bairro");
        cliCityAddress = new TextField("Cidade");
        cliStateAddress = new Select<>();
        cliStateAddress.setLabel("Estado");
        cliStateAddress.setItems(State.values());
        cliStateAddress.setItemLabelGenerator(State::getValue);
        cliZipAddress = new TextField("CEP");
        cliZipAddress.setHelperText("Por favor, inserir somente números.");

        if(client != null) {
            clientNameField.setValue(client.getNome());
            clientSurnameField.setValue(client.getSobrenome());
            clientDocumentField.setValue(client.getDocumento());
            clientBirthdateField.setValue(client.getDataDeNascimento());
            clientEmailField.setValue(client.getEmail());
            clientPhoneField.setValue(client.getTelefone());
            cliLogAddress.setValue(clientAddress.getLogradouro());
            cliNumAddress.setValue(clientAddress.getNumero());
            cliNeighborhoodAddress.setValue(clientAddress.getBairro());
            cliCityAddress.setValue(clientAddress.getCidade());
            cliStateAddress.setValue(clientAddress.getEstado());
            cliZipAddress.setValue(clientAddress.getCEP());
        }

        Component[] fields = new Component[] {clientNameField,clientSurnameField,clientDocumentField,clientBirthdateField,clientEmailField,clientPhoneField};
        Component[] fieldsAddress = new Component[] {cliLogAddress,cliNumAddress,cliNeighborhoodAddress,cliCityAddress,cliStateAddress,cliZipAddress};

        //Now we can apply the validation rules to the fields
        clientBinder.forField(clientNameField).asRequired("Por favor, insira o nome do cliente.").bind(Client::getNome, Client::setNome);
        clientBinder.forField(clientSurnameField).asRequired("Por favor, insira o sobrenome do cliente.").bind(Client::getSobrenome, Client::setSobrenome);
        clientBinder.forField(clientDocumentField).asRequired("Por favor, insira o número do documento do cliente.")
                .withValidator(numero -> Pattern.matches("([0-9]{2}[\\.]?[0-9]{3}[\\.]?[0-9]{3}[\\/]?[0-9]{4}[-]?[0-9]{2})|([0-9]{3}[\\.]?[0-9]{3}[\\.]?[0-9]{3}[-]?[0-9]{2})",numero),
                        "Por favor, informe um número de documento válido.").bind(Client::getDocumento, Client::setDocumento);
        clientBinder.forField(clientBirthdateField).asRequired("Por favor, insira a data de nascimento do cliente.")
                .withValidator(birthDate ->!(birthDate.isAfter(LocalDate.now())),
                        "Por favor, insira a data de nascimento do cliente corretamente")
                .withValidator(birthDate -> (birthDate.isBefore(LocalDate.now().minus(18, ChronoUnit.YEARS))),
                        "O cliente deverá ser maior de idade").bind(Client::getDataDeNascimento, Client::setDataDeNascimento);
        clientBinder.forField(clientEmailField).asRequired("Por favor, insira o email do cliente.")
                .withValidator(new EmailValidator(
                        "O endereço de e-mail informado parece não ser válido, por favor, verifique o endereço informado.")).bind(Client::getEmail, Client::setEmail);
        clientBinder.forField(clientPhoneField).asRequired("Por favor, insira o telefone do cliente.")
                .withValidator(numero -> (Pattern.matches("^\\(?[1-9]{2}\\)?\\s?\\d{4,5}(\\-|\\s)?\\d{4}$",numero)),
                        "Por favor, insira um número de telefone/celular válido.").bind(Client::getTelefone, Client::setTelefone);
        clientBinder.bindInstanceFields(this);
        cliAddressBinder.forField(cliLogAddress).asRequired("Por favor, insira o logradouro do cliente.").bind(Address::getLogradouro, Address::setLogradouro);
        cliAddressBinder.forField(cliNumAddress).asRequired("Por favor, insira o número do endereço.")
                .withValidator(numero -> (Pattern.matches("s|S(n|S|N)|[0-9]{1,5}$",numero)),
                        "Por favor, informe um número válido. Se não possuir, digite SN").bind(Address::getNumero, Address::setNumero);
        cliAddressBinder.forField(cliNeighborhoodAddress).asRequired("Por favor, insira o bairro do cliente.").bind(Address::getBairro, Address::setBairro);
        cliAddressBinder.forField(cliCityAddress).asRequired("Por favor, insira a cidade do cliente.").bind(Address::getCidade, Address::setCidade);
        cliAddressBinder.forField(cliStateAddress).asRequired("Por favor, insira o estado do cliente.").bind(Address::getEstado, Address::setEstado);
        cliAddressBinder.forField(cliZipAddress).asRequired("Por favor, insira o CEP do cliente.").bind(Address::getCEP, Address::setCEP);

        formLayout.add(fields);
        formLayout.add(fieldsAddress);

        VerticalLayout fieldLayout = new VerticalLayout(formLayout);
        fieldLayout.setSpacing(false);
        fieldLayout.setPadding(false);
        fieldLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        fieldLayout.getStyle().set("width","480px").set("max-width","100%");

        return fieldLayout;
    }

    private void refreshGrid() {
        this.client = null;
        this.address = null;
        grid.getLazyDataView().refreshAll();
    }
}
