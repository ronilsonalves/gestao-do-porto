package com.ronilsonalves.gestaoporto.data.enums;

public enum TransactionType {
    EMBARQUE("Embarque"),DESCARGA("Descarga"),GATE_IN("Gate In"),GATE_OUT("Gate Out"),REPOSICIONAMENTO("Reposicionamento"),
    PESAGEM("Pesagem"),SCANNER("Scanner");

    private String value;

    TransactionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return super.toString();
    }

}
