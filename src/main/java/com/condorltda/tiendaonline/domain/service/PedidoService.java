package com.condorltda.tiendaonline.domain.service;


import com.condorltda.tiendaonline.domain.StockInsuficienteException;
import com.condorltda.tiendaonline.domain.ValidacionNegocioException;
import com.condorltda.tiendaonline.domain.cliente.Cliente;
import com.condorltda.tiendaonline.domain.cliente.ClienteRepository;
import com.condorltda.tiendaonline.domain.detalleFactura.DetalleFactura;
import com.condorltda.tiendaonline.domain.detalleFactura.DetalleFacturaRepository;
import com.condorltda.tiendaonline.domain.factura.EstadoFactura;
import com.condorltda.tiendaonline.domain.factura.Factura;
import com.condorltda.tiendaonline.domain.factura.FacturaRepository;
import com.condorltda.tiendaonline.domain.movimientoInventario.MovimientoInventario;
import com.condorltda.tiendaonline.domain.movimientoInventario.MovimientoInventarioRepository;
import com.condorltda.tiendaonline.domain.pedido.DatosItemPedido;
import com.condorltda.tiendaonline.domain.pedido.DatosRegistroPedido;
import com.condorltda.tiendaonline.domain.pedido.DatosRespuestaItemPedido;
import com.condorltda.tiendaonline.domain.pedido.DatosRespuestaPedido;
import com.condorltda.tiendaonline.domain.producto.Producto;
import com.condorltda.tiendaonline.domain.producto.ProductoRepository;
import com.condorltda.tiendaonline.domain.usuario.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PedidoService {

    private static final Logger logger = LoggerFactory.getLogger(PedidoService.class);

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private DetalleFacturaRepository detalleFacturaRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PagoService pagoService; // <--- ¡INYECCIÓN DE DEPENDENCIA!

    @Transactional
    public DatosRespuestaPedido procesarPedido(DatosRegistroPedido datosPedido) {

        logger.info("BEGIN Transaction - Procesando pedido para cliente ID: {}", datosPedido.idCliente());

        Optional<Cliente> clienteOptional = clienteRepository.findById(datosPedido.idCliente());
        if (!clienteOptional.isPresent()) {
            logger.error("ValidacionNegocioException - Cliente no encontrado con ID: {}", datosPedido.idCliente());
            throw new ValidacionNegocioException("Cliente no encontrado con ID: " + datosPedido.idCliente());
        }
        Cliente cliente = clienteOptional.get();

        BigDecimal valorTotalCalculado = BigDecimal.ZERO;
        List<DetalleFactura> itemsFactura = new ArrayList<>();
        List<MovimientoInventario> movimientosSalida = new ArrayList<>();

        logger.info("Validando stock e items del pedido...");
        for (DatosItemPedido itemPedido : datosPedido.items()) {
            Optional<Producto> productoOptional = productoRepository.findById(itemPedido.idProducto());
            if (!productoOptional.isPresent()) {
                logger.error("ValidacionNegocioException - Producto no encontrado con ID: {}", itemPedido.idProducto());
                throw new ValidacionNegocioException("Producto no encontrado con ID: " + itemPedido.idProducto());
            }
            Producto producto = productoOptional.get();

            BigDecimal stockActual = movimientoInventarioRepository.calcularStockActualByProductoId(producto.getId());

            logger.debug("Producto ID: {}, Stock Actual: {}, Cantidad Pedida: {}", producto.getId(), stockActual, itemPedido.cantidad());

            if (stockActual.compareTo(new BigDecimal(itemPedido.cantidad())) < 0) {
                logger.error("StockInsuficienteException - Stock insuficiente para producto ID: {}", producto.getId());
                throw new StockInsuficienteException("Stock insuficiente para producto ID: " + producto.getId(), producto.getId());
            }

            DetalleFactura detalle = new DetalleFactura(
                    null,
                    producto,
                    itemPedido.cantidad(),
                    producto.getPrecioActual()
            );
            itemsFactura.add(detalle);

            valorTotalCalculado = valorTotalCalculado.add(detalle.calcularSubtotal());

            MovimientoInventario movimientoSalida = new MovimientoInventario(
                    producto,
                    "salida_venta",
                    new BigDecimal(-itemPedido.cantidad()),
                    0,
                    LocalDate.now(),
                    null, // Referencia se asignará después de obtener ID de factura
                    cliente.getUsuario(),
                    null
            );
            movimientosSalida.add(movimientoSalida);
        }

        Factura factura = new Factura(
                LocalDate.now(),
                cliente,
                valorTotalCalculado,
                datosPedido.metodoPago(),
                EstadoFactura.PENDIENTE
        );

        for (DetalleFactura item : itemsFactura) {
            factura.agregarItem(item);
        }

        logger.info("Guardando Factura y Detalles...");
        Factura facturaGuardada = facturaRepository.save(factura);
        logger.info("Factura {} y sus detalles guardados.", facturaGuardada.getId());

        String idFacturaGenerado = String.valueOf(facturaGuardada.getId());

        logger.info("Guardando Movimientos de Inventario de Salida...");
        for (MovimientoInventario movimiento : movimientosSalida) {
            movimiento.setReferenciaFactura(idFacturaGenerado);
            movimientoInventarioRepository.save(movimiento);
        }
        logger.info("Movimientos de inventario de salida guardados.");

        logger.info("COMMIT Transaction - Pedido procesado exitosamente. Factura ID: {}", idFacturaGenerado);

        // --- ¡NUEVA LÓGICA CLAVE: INICIAR SIMULACIÓN DE PAGO DESPUÉS DEL COMMIT! ---
        // Registrar una acción para ser ejecutada justo después de que la transacción actual (procesarPedido)
        // haya completado exitosamente su COMMIT en la base de datos.
        Factura finalFacturaGuardada = facturaGuardada; // Variable efectivamente final para la lambda

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                // Este código se ejecuta solo si la transacción principal (procesarPedido) fue exitosa.
                try {
                    // Llamar al PagoService para iniciar el temporizador de pago.
                    // Esto es asíncrono y en un hilo diferente, no bloquea el hilo del pedido.
                    pagoService.iniciarSimulacionPago(finalFacturaGuardada.getId());
                    logger.info("Simulación de pago iniciada para factura ID: {} después del commit del pedido.", finalFacturaGuardada.getId());
                } catch (Exception e) {
                    // Es importante manejar excepciones aquí porque afterCommit se ejecuta fuera de la tx original.
                    // Si esto falla, el pedido está hecho pero el timer de pago no se inició.
                    // En un entorno de producción, considerar mecanismos de alerta o reintento.
                    logger.error("CRÍTICO: Error al iniciar la simulación de pago para factura ID: {} después del commit. El pedido se creó pero el pago no se podrá simular automáticamente.",
                            finalFacturaGuardada.getId(), e);
                }
            }

            @Override
            public void afterCompletion(int status) {
                // Este método se llama después de afterCommit o afterRollback.
                // Es útil para logging o limpieza final.
                if (status == STATUS_ROLLED_BACK) {
                    logger.info("Transacción de pedido para cliente ID: {} fue revertida (rolled back). No se iniciará simulación de pago.", datosPedido.idCliente());
                } else if (status == STATUS_COMMITTED) {
                    logger.info("Transacción de pedido para factura ID: {} completada y comprometida.", finalFacturaGuardada.getId());
                }
            }
        });
        // --- FIN DE LA NUEVA LÓGICA ---

        List<DatosRespuestaItemPedido> respuestaItems = new ArrayList<>();
        for (DetalleFactura item : facturaGuardada.getItems()) {
            respuestaItems.add(new DatosRespuestaItemPedido(
                    item.getProducto().getId(),
                    item.getProducto().getNombre(),
                    item.getCantidadVendida(),
                    item.getPrecioUnitario(),
                    item.calcularSubtotal()
            ));
        }

        return new DatosRespuestaPedido(
                facturaGuardada.getId(),
                facturaGuardada.getFecha().atStartOfDay(),
                facturaGuardada.getValorTotalFactura(),
                facturaGuardada.getMetodoPago(),
                facturaGuardada.getEstadoFactura().toString(),
                respuestaItems
        );
    }
}