package com.condorltda.tiendaonline.domain.pedido;


import java.math.BigDecimal;
import java.time.LocalDateTime; // O LocalDate si usas solo fecha
import java.util.List; // Para la lista de items respondidos

// Record para la respuesta después de registrar un pedido
public record DatosRespuestaPedido(

        Integer idFactura, // El ID de la factura generada
        LocalDateTime fecha, // La fecha y hora de la factura (o LocalDate)
        BigDecimal valorTotal, // El valor total de la factura
        String metodoPago, // Método de pago
        String estadoFactura, // Estado de la factura

        // Puedes incluir detalles del cliente si es necesario
        // DatosClienteRespuesta datosCliente,

        List<DatosRespuestaItemPedido> items // Lista de los detalles de los items del pedido
) {
    // Puedes añadir un constructor para mapear desde la entidad Factura si es conveniente
    // public DatosRespuestaPedido(Factura factura) { ... }
}
