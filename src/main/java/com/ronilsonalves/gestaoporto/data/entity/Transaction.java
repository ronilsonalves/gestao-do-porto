package com.ronilsonalves.gestaoporto.data.entity;

import com.ronilsonalves.gestaoporto.data.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimentacao")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Transaction extends GenericEntity<GenericEntity> {

    @NotNull
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @NotNull
    private LocalDateTime startDateTime;

    @NotNull
    private LocalDateTime endDateTime;

    @NotNull
    @ManyToOne(cascade = {
            CascadeType.MERGE,CascadeType.DETACH,CascadeType.REFRESH
    })
    @JoinColumn(name = "container_numero")
    private Container container;
}