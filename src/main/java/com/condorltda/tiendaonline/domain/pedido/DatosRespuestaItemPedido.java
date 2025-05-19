package com.condorltda.tiendaonline.domain.pedido;


import java.math.BigDecimal;

// Record para los detalles de cada producto en la respuesta del pedido
public record DatosRespuestaItemPedido(

        Integer idProducto, // El ID del producto
        String nombreProducto, // El nombre del producto (para mostrar en la respuesta)
        Integer cantidadVendida, // Cantidad vendida
        BigDecimal precioUnitario, // Precio unitario al momento de la venta
        BigDecimal subtotalLinea // El subtotal de esta línea (cantidad * precio) - campo calculado para el DTO
) {
    // Puedes añadir un constructor para mapear desde la entidad DetalleFactura
    // public DatosRespuestaItemPedido(DetalleFactura detalleFactura) { ... }
}
