package com.condorltda.tiendaonline.domain.pedido;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record DatosRegistroPedido(
        @NotNull // El ID del cliente/usuario que realiza el pedido no puede ser nulo
        Integer idCliente, // Asumimos que la API recibe el ID del cliente asociado

        @NotBlank // El método de pago no puede estar vacío
        String metodoPago, // Ej. "EFECTIVO", "TARJETA"

        @NotNull // La lista de items del pedido no puede ser nula
        List<@Valid DatosItemPedido> items // La lista de items debe existir y cada item debe ser válido
) {
}
