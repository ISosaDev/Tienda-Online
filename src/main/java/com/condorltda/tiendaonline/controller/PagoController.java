package com.condorltda.tiendaonline.controller;

import com.condorltda.tiendaonline.domain.factura.Factura;
import com.condorltda.tiendaonline.domain.service.PagoService;
import com.condorltda.tiendaonline.domain.pedido.DatosRespuestaPedido;
import com.condorltda.tiendaonline.domain.pedido.DatosRespuestaItemPedido;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;
import java.util.ArrayList;


@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private static final Logger logger = LoggerFactory.getLogger(PagoController.class);

    @Autowired
    private PagoService pagoService;

    @PostMapping("/factura/{facturaId}/pagar")
    public ResponseEntity<?> realizarPago(@PathVariable Integer facturaId) {
        logger.info("Recibida solicitud de pago para Factura ID: {}", facturaId);
        try {
            Factura facturaPagada = pagoService.procesarPagoExitoso(facturaId);

            // Construir DTO de respuesta similar a DatosRespuestaPedido
            // para mantener consistencia en el frontend si lo deseas.
            DatosRespuestaPedido respuestaPago = new DatosRespuestaPedido(
                    facturaPagada.getId(),
                    facturaPagada.getFecha().atStartOfDay(), // Ajusta si usas LocalDateTime
                    facturaPagada.getValorTotalFactura(),
                    facturaPagada.getMetodoPago(),
                    facturaPagada.getEstadoFactura().toString(), // Enum a String
                    facturaPagada.getItems().stream().map(item -> new DatosRespuestaItemPedido(
                            item.getProducto().getId(),
                            item.getProducto().getNombre(),
                            item.getCantidadVendida(),
                            item.getPrecioUnitario(),
                            item.calcularSubtotal()
                    )).collect(Collectors.toList())
            );
            logger.info("Pago procesado exitosamente para Factura ID: {}. Nuevo estado: {}", facturaId, facturaPagada.getEstadoFactura());
            return ResponseEntity.ok(respuestaPago);
        } catch (Exception e) {
            // Las excepciones como ValidacionNegocioException o EntityNotFoundException
            // serán manejadas por el TratadorDeErrores global.
            // Si necesitas un log específico aquí:
            logger.error("Error al procesar pago para Factura ID: {}. Mensaje: {}", facturaId, e.getMessage(), e);
            throw e; // Re-lanzar para que TratadorDeErrores la maneje.
        }
    }
}
