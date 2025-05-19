package com.condorltda.tiendaonline.domain;

public class StockInsuficienteException extends RuntimeException {

    private Integer productoId;

    public StockInsuficienteException(String message) {
        super(message);
    }

    public StockInsuficienteException(String message, Integer productoId) {
        super(message);
        this.productoId = productoId;
    }

    public Integer getProductoId() {
        return productoId;
    }
}
