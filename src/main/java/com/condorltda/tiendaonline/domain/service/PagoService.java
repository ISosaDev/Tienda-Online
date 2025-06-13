package com.condorltda.tiendaonline.domain.service;

import com.condorltda.tiendaonline.domain.factura.Factura;
import com.condorltda.tiendaonline.domain.factura.FacturaRepository;
import com.condorltda.tiendaonline.domain.factura.EstadoFactura;
import com.condorltda.tiendaonline.domain.detalleFactura.DetalleFactura;
import com.condorltda.tiendaonline.domain.movimientoInventario.MovimientoInventario;
import com.condorltda.tiendaonline.domain.movimientoInventario.MovimientoInventarioRepository;
import com.condorltda.tiendaonline.domain.ValidacionNegocioException;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
public class PagoService {

    private static final Logger logger = LoggerFactory.getLogger(PagoService.class);

    // Tiempo límite para pagar en segundos
    private static final long TIEMPO_ESPERA_PAGO_SEGUNDOS = 30;

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @Autowired
    private TaskScheduler taskScheduler;

    // Mapa para llevar el seguimiento de tareas de cancelación pendientes
    private final Map<Integer, ScheduledFuture<?>> tareasPendientesCancelacion = new ConcurrentHashMap<>();

    /**
     * Inicia la simulación de pago. Programa una tarea para cancelar automáticamente
     * si no se paga en el tiempo definido.
     */
    public void iniciarSimulacionPago(Integer facturaId) {
        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> {
                    logger.error("Factura no encontrada para iniciar simulación de pago: {}", facturaId);
                    return new EntityNotFoundException("Factura no encontrada: " + facturaId);
                });

        if (factura.getEstadoFactura() != EstadoFactura.PENDIENTE) {
            logger.warn("Factura ID: {} no está en estado PENDIENTE. Estado actual: {}", facturaId, factura.getEstadoFactura());
            return;
        }

        logger.info("⏳ Iniciando simulación de pago para Factura ID: {}. Tiempo de espera: {} segundos.", facturaId, TIEMPO_ESPERA_PAGO_SEGUNDOS);

        // ✅ Programar tarea de cancelación futura
        ScheduledFuture<?> tareaProgramada = taskScheduler.schedule(() -> {
            try {
                cancelarFacturaPorTimeout(facturaId);
            } catch (Exception e) {
                logger.error("❌ Error durante cancelación por timeout para factura {}", facturaId, e);
            }
        }, Instant.now().plusSeconds(TIEMPO_ESPERA_PAGO_SEGUNDOS));

        tareasPendientesCancelacion.put(facturaId, tareaProgramada);
        logger.info("✅ Tarea de cancelación programada para factura ID: {}", facturaId);
    }

    /**
     * Procesa el pago exitoso de una factura y cancela la tarea de timeout.
     */
    @Transactional
    public Factura procesarPagoExitoso(Integer facturaId) {
        logger.info("💳 Procesando pago para Factura ID: {}", facturaId);

        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada: " + facturaId));

        if (factura.getEstadoFactura() != EstadoFactura.PENDIENTE) {
            logger.warn("❌ Factura ID: {} no está en estado PENDIENTE. Estado actual: {}", facturaId, factura.getEstadoFactura());
            throw new ValidacionNegocioException("La factura no está en estado PENDIENTE para ser pagada.");
        }

        // Cancelar la tarea programada de cancelación por timeout
        ScheduledFuture<?> tareaPendiente = tareasPendientesCancelacion.get(facturaId);
        if (tareaPendiente != null && tareaPendiente.cancel(false)) {
            logger.info("🛑 Tarea de cancelación por timeout cancelada para Factura ID: {}", facturaId);
            tareasPendientesCancelacion.remove(facturaId);
        }

        // Marcar factura como pagada
        factura.setEstadoFactura(EstadoFactura.PAGADA);
        Factura facturaPagada = facturaRepository.save(factura);

        logger.info("✅ Factura ID: {} marcada como PAGADA.", facturaId);
        return facturaPagada;
    }

    /**
     * Método que ejecuta el rollback simulado: cambia la factura a CANCELADA
     * y revierte los movimientos de inventario.
     * Este método se ejecuta desde una tarea programada y necesita una transacción.
     */
    @Transactional
    public void cancelarFacturaPorTimeout(Integer facturaId) {
        logger.info("⏰ Ejecutando cancelación automática por timeout para factura ID: {}", facturaId);

        // Se usa el método que carga explícitamente los ítems
        Factura factura = facturaRepository.findWithItemsAndClienteById(facturaId)
                .orElse(null);
        if (factura == null) {
            logger.warn("Factura ID: {} no encontrada. No se puede cancelar.", facturaId);
            return;
        }

        if (factura.getEstadoFactura() != EstadoFactura.PENDIENTE) {
            logger.info("Factura ID: {} ya no está en estado PENDIENTE (actual: {}). No se cancela.", facturaId, factura.getEstadoFactura());
            return;
        }

        // Ya no es necesario validar si getItems() está inicializado, se carga con @EntityGraph
        if (factura.getItems() == null || factura.getItems().isEmpty()) {
            logger.warn("Factura ID: {} no tiene ítems para revertir. No se ejecuta compensación.", facturaId);
            return;
        }

        factura.setEstadoFactura(EstadoFactura.CANCELADA);
        facturaRepository.save(factura);
        logger.info("⚠️ Factura ID: {} marcada como CANCELADA.", facturaId);

        for (DetalleFactura item : factura.getItems()) {
            BigDecimal cantidadARestaurar = new BigDecimal(item.getCantidadVendida());

            MovimientoInventario reversion = new MovimientoInventario(
                    item.getProducto(),
                    "REVERSION_PAGO_NO_REALIZADO",
                    cantidadARestaurar,
                    null,
                    LocalDate.now(),
                    "CANCELADA - Factura ID: " + factura.getId(),
                    factura.getCliente() != null ? factura.getCliente().getUsuario() : null,
                    null
            );

            movimientoInventarioRepository.save(reversion);
            logger.info("🔄 Stock revertido para Producto ID: {} (Cantidad: {}).", item.getProducto().getId(), cantidadARestaurar);
        }

        logger.info("✅ Transacción compensatoria completada para Factura ID: {}", facturaId);
    }

}

