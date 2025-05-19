package com.condorltda.tiendaonline.domain.pedido;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive; // La cantidad debe ser positiva

// Record para los datos de cada producto en el pedido
public record DatosItemPedido(

        @NotNull // El ID del producto no puede ser nulo
        Integer idProducto, // El ID del producto que se está comprando

        @NotNull // La cantidad no puede ser nula
        @Positive // La cantidad debe ser mayor que cero
        Integer cantidad // La cantidad de este producto
) {
}
