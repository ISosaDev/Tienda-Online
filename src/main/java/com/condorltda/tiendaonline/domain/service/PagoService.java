package com.condorltda.tiendaonline.domain.service;

import com.condorltda.tiendaonline.domain.factura.Factura;
import com.condorltda.tiendaonline.domain.factura.FacturaRepository;
import com.condorltda.tiendaonline.domain.factura.EstadoFactura;
import com.condorltda.tiendaonline.domain.detalleFactura.DetalleFactura;
import com.condorltda.tiendaonline.domain.movimientoInventario.MovimientoInventario;
import com.condorltda.tiendaonline.domain.movimientoInventario.MovimientoInventarioRepository;
import com.condorltda.tiendaonline.domain.ValidacionNegocioException;
import com.condorltda.tiendaonline.domain.usuario.Usuario; // Asegúrate de tener la entidad Usuario
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
    private final static long TIEMPO_ESPERA_PAGO_SEGUNDOS = 30; // Tiempo para el demo

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoInventarioRepository;

    // Necesario si Cliente tiene referencia a Usuario y quieres usarla.
    // Si no, puedes obtener el cliente directamente de la factura.
    // @Autowired
    // private ClienteRepository clienteRepository;

    @Autowired
    private TaskScheduler taskScheduler;

    // Mapa para rastrear las tareas de cancelación pendientes
    private final Map<Integer, ScheduledFuture<?>> tareasPendientesCancelacion = new ConcurrentHashMap<>();

    /**
     * Inicia la simulación de pago para una factura.
     * Programa una tarea para cancelar la factura si no se paga a tiempo.
     * Este método se llama DESPUÉS de que la transacción de creación de pedido haya hecho COMMIT.
     * @param facturaId El ID de la factura.
     */
    public void iniciarSimulacionPago(Integer facturaId) {

        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> {
                    logger.error("Intento de iniciar simulación de pago para factura no existente ID: {}", facturaId);
                    return new EntityNotFoundException("Factura no encontrada para iniciar simulación de pago: " + facturaId);
                });

        if (factura.getEstadoFactura() != EstadoFactura.PENDIENTE) {
            logger.warn("Intento de iniciar simulación de pago para factura {} que no está PENDIENTE. Estado actual: {}", facturaId, factura.getEstadoFactura());
            return;
        }

        logger.info("Iniciando simulación de pago para Factura ID: {}. Tiempo de espera: {} segundos.", facturaId, TIEMPO_ESPERA_PAGO_SEGUNDOS);

        ScheduledFuture<?> tareaProgramada = taskScheduler.schedule(
                () -> cancelarFacturaPorTimeout(facturaId), // La acción a ejecutar
                Instant.now().plusSeconds(TIEMPO_ESPERA_PAGO_SEGUNDOS) // Cuándo ejecutarla
        );

        logger.info("✅ Tarea programada para cancelar factura ID: {} en {} segundos", facturaId, TIEMPO_ESPERA_PAGO_SEGUNDOS);

        // Almacenar la tarea para poder cancelarla si el pago se realiza a tiempo
        tareasPendientesCancelacion.put(facturaId, tareaProgramada);
    }

    /**
     * Procesa un intento de pago como exitoso.
     * Cambia el estado de la factura a PAGADA y cancela la tarea de timeout.
     * @param facturaId El ID de la factura a pagar.
     * @return La factura actualizada.
     * @throws ValidacionNegocioException si la factura no está en estado PENDIENTE o ya fue pagada/cancelada.
     */
    @Transactional
    public Factura procesarPagoExitoso(Integer facturaId) {
        logger.info("Procesando pago para Factura ID: {}", facturaId);
        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new EntityNotFoundException("Factura no encontrada: " + facturaId));

        if (factura.getEstadoFactura() == EstadoFactura.PAGADA) {
            logger.warn("Factura ID: {} ya se encuentra PAGADA.", facturaId);
            throw new ValidacionNegocioException("La factura ya ha sido pagada.");
        }
        if (factura.getEstadoFactura() == EstadoFactura.CANCELADA) {
            logger.warn("Factura ID: {} se encuentra CANCELADA. No se puede pagar.", facturaId);
            throw new ValidacionNegocioException("La factura está cancelada y no se puede pagar.");
        }
        if (factura.getEstadoFactura() != EstadoFactura.PENDIENTE) {
            logger.error("Factura ID: {} no está PENDIENTE. Estado actual: {}", facturaId, factura.getEstadoFactura());
            throw new ValidacionNegocioException("La factura no está en estado PENDIENTE para ser pagada. Estado actual: " + factura.getEstadoFactura());
        }

        ScheduledFuture<?> tareaPendiente = tareasPendientesCancelacion.get(facturaId);
        if (tareaPendiente != null) {
            boolean canceladaExitosamente = tareaPendiente.cancel(false); // false: no interrumpir si ya está corriendo
            if (canceladaExitosamente) {
                logger.info("Tarea de cancelación por timeout para Factura ID: {} fue cancelada exitosamente debido al pago.", facturaId);
            } else {
                // Si no se pudo cancelar, podría ser porque ya se ejecutó o está a punto.
                // Es crucial re-verificar el estado de la factura para evitar condiciones de carrera.
                logger.warn("No se pudo cancelar la tarea de timeout para Factura ID: {}. Verificando estado actual de la factura.", facturaId);
                Factura facturaActualizada = facturaRepository.findById(facturaId).orElseThrow(); // Re-obtener
                if (facturaActualizada.getEstadoFactura() == EstadoFactura.CANCELADA) {
                    logger.warn("Factura ID: {} fue CANCELADA por timeout justo antes/durante el intento de pago.", facturaId);
                    throw new ValidacionNegocioException("El tiempo para pagar la factura expiró y fue cancelada.");
                }
            }
            tareasPendientesCancelacion.remove(facturaId); // Remover del mapa
        } else {
            // Si no hay tarea pendiente, podría ser que el timeout ya ocurrió y procesó.
            logger.warn("No se encontró tarea de cancelación pendiente para Factura ID: {}. El timeout podría haber ocurrido.", facturaId);
            // Verificar el estado actual de la factura es una buena práctica aquí también.
            if (factura.getEstadoFactura() != EstadoFactura.PENDIENTE) {
                throw new ValidacionNegocioException("El estado de la factura ya no es PENDIENTE. Estado actual: " + factura.getEstadoFactura());
            }
        }

        factura.setEstadoFactura(EstadoFactura.PAGADA);
        Factura facturaPagada = facturaRepository.save(factura);
        logger.info("Factura ID: {} marcada como PAGADA.", facturaId);
        // El inventario ya fue descontado al crear el pedido, no se toca aquí.
        return facturaPagada;
    }

    /**
     * Cancela una factura debido a la expiración del tiempo de pago.
     * Cambia el estado a CANCELADA y realiza una transacción compensatoria para revertir el stock.
     * Este método es llamado por el TaskScheduler.
     * @param facturaId El ID de la factura a cancelar.
     */
    @Transactional
    public void cancelarFacturaPorTimeout(Integer facturaId) {
        logger.info("⏰ Ejecutando cancelación automática por timeout para factura ID: {}", facturaId);

        // Buscar la factura
        Factura factura = facturaRepository.findById(facturaId).orElse(null);
        if (factura == null) {
            logger.warn("Factura ID: {} no encontrada. No se puede cancelar.", facturaId);
            return;
        }

        // Verificar estado actual
        if (factura.getEstadoFactura() != EstadoFactura.PENDIENTE) {
            logger.info("Factura ID: {} ya no está en estado PENDIENTE (actual: {}). No se cancela.", facturaId, factura.getEstadoFactura());
            return;
        }

        // Forzar carga de ítems si la relación es LAZY
        if (factura.getItems() == null || factura.getItems().isEmpty()) {
            logger.warn("Factura ID: {} no tiene ítems cargados. Verifica configuración de FetchType.", facturaId);
            return;
        }

        // Cambiar estado a CANCELADA
        factura.setEstadoFactura(EstadoFactura.CANCELADA);
        facturaRepository.save(factura);
        logger.info("Factura ID: {} marcada como CANCELADA.", facturaId);

        // Revertir inventario
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
            logger.info("Stock revertido para Producto ID: {} (Cantidad: {}).", item.getProducto().getId(), cantidadARestaurar);
        }

        logger.info("✅ Transacción compensatoria completada para Factura ID: {}", facturaId);
    }

}
