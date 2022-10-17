package com.ronilsonalves.gestaoporto.data.entity;

import com.ronilsonalves.gestaoporto.data.enums.State;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Address extends GenericEntity<GenericEntity> {

    @NotBlank
    @Size(min = 4)
    private String streetAddress;

    @NotBlank(message = "Por favor, insira o número do endereço")
    private String number;

    @NotBlank
    @Size(min = 8,max = 8, message = "CEP deve conter 8 caracteres")
    private String ZIPCode;

    @NotBlank
    @Size(min = 5)
    private String neighborhood;

    @NotBlank
    @Size(min = 3)
    private String city;

    @NotNull
    @Enumerated(EnumType.STRING)
    private State state;
}
