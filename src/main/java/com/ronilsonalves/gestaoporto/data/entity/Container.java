package com.ronilsonalves.gestaoporto.data.entity;

import com.ronilsonalves.gestaoporto.data.enums.Categoria;
import com.ronilsonalves.gestaoporto.data.enums.Status;
import com.ronilsonalves.gestaoporto.data.enums.Tipo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.validator.constraints.UniqueElements;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
@Table(name = "container")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Container extends GenericEntity<GenericEntity> {

    @NotBlank(message = "Campo obrigatório, por favor, preencha-o!")
    private String cliente;

    @NotBlank
    @Column(unique = true, name = "numero")
    private String numero;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Tipo tipo;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Status status;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Categoria categoria;

//    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
//    private List<Movimentacao> movimentacoes;

}