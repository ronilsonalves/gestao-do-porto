package com.ronilsonalves.gestaoporto.data.enums;

public enum ContainerType {
    VINTE(20),QUARENTA(40);
    private final int valor;
    ContainerType(int valor) {
        this.valor = valor;
    }

    public int getValor() {
        return valor;
    }
}
