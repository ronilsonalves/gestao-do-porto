package com.ronilsonalves.gestaoporto.data.entity;

import com.ronilsonalves.gestaoporto.data.enums.Category;
import com.ronilsonalves.gestaoporto.data.enums.Status;
import com.ronilsonalves.gestaoporto.data.enums.ContainerType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "container")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Container extends GenericEntity<GenericEntity> {

    @ManyToOne(cascade = {
            CascadeType.MERGE,CascadeType.DETACH,CascadeType.REFRESH
    })
    @JoinColumn(name = "cliente_id")
    @NotNull(message = "Campo obrigatório, por favor, preencha-o!")
    private Client client;

    @NotBlank(message = "Campo obrigatório, por favor, preencha-o!")
    @Column(unique = true, name = "numero")
    private String number;

    @NotNull(message = "Campo obrigatório, por favor, preencha-o!")
    @Enumerated(EnumType.STRING)
    private ContainerType containerType;

    @NotNull(message = "Campo obrigatório, por favor, preencha-o!")
    @Enumerated(EnumType.STRING)
    private Status status;

    @NotNull(message = "Campo obrigatório, por favor, preencha-o!")
    @Enumerated(EnumType.STRING)
    private Category category;

}