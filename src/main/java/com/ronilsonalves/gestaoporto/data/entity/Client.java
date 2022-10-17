package com.ronilsonalves.gestaoporto.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.UniqueElements;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "clients")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Client extends GenericEntity<GenericEntity> {

    @NotEmpty(message = "Por favor, insira o nome do cliente")
    @Size(min = 2)
    private String name;

    @NotEmpty(message = "Insira o sobrenome do cliente")
    @Size(min = 3)
    private String surname;

    @NotEmpty(message = "Por favor, insira um documento válido.")
    @Size(min = 11,max = 14)
    @Column(unique = true)
    private String document;

    @NotNull(message = "Por favor, insira uma data de nascimento válida.")
    private LocalDate birthDate;

    @NotEmpty(message = "Por favor, insira o endereço de e-mail.")
    @Email(message = "Por favor, insira um email válido.")
    private String email;

    @NotEmpty(message = "Por favor, insira um telefone válido.")
    private String phone;

    @OneToOne
    @JoinColumn(name = "address_id")
    @NotNull(message = "Por favor, insira um endereço válido.")
    private Address address;

    @OneToMany(cascade = CascadeType.MERGE,mappedBy = "client",orphanRemoval = true)
    @JsonIgnore
    private List<Container> containers;

    @Override
    public String toString() {
        return getName() + " " + getSurname();
    }
}
