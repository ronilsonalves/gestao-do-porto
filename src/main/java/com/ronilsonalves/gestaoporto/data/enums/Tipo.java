package com.ronilsonalves.gestaoporto.data.enums;

import com.vaadin.exampledata.DataType;

public enum Tipo {
    VINTE(20),QUARENTA(40);
    private final int valor;
    Tipo(int valor) {
        this.valor = valor;
    }

    public int getValor() {
        return valor;
    }
}
