package com.condorltda.tiendaonline.domain.service;


import com.condorltda.tiendaonline.domain.StockInsuficienteException;
import com.condorltda.tiendaonline.domain.ValidacionNegocioException;
import com.condorltda.tiendaonline.domain.cliente.Cliente;
import com.condorltda.tiendaonline.domain.cliente.ClienteRepository;
import com.condorltda.tiendaonline.domain.detalleFactura.DetalleFactura;
import com.condorltda.tiendaonline.domain.detalleFactura.DetalleFacturaRepository;
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
import org.springframework.transaction.annotation.Transactional; // Importante para @Transactional

import java.math.BigDecimal;
import java.time.LocalDate; // Usar LocalDate si tu campo fecha es DATE
import java.util.ArrayList;
import java.util.List;
import java.util.Optional; // Para manejar resultados que pueden no existir

// Importa el logger de SLF4J (parte de Spring Boot por defecto)
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service // Indica a Spring que esto es un componente de servicio
public class PedidoService {

    // Obtiene un logger para esta clase (para simular el log de transacciones)
    private static final Logger logger = LoggerFactory.getLogger(PedidoService.class);

    // Inyección de dependencias de los Repositorios necesarios
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
    private UsuarioRepository usuarioRepository; // Necesario si cliente_id_usuario es la FK para Usuario

    // ****************************************************************
    // Método principal para procesar un pedido - ¡Esta es la Transacción!
    // ****************************************************************
    @Transactional // Esta anotación indica a Spring que gestione una transacción para este método.
    // Si una RuntimeException ocurre, Spring hará ROLLBACK.
    // Si el método finaliza sin excepciones (o con excepciones verificadas por defecto), Spring hará COMMIT.
    public DatosRespuestaPedido procesarPedido(DatosRegistroPedido datosPedido) {

        // --- Simulación de Log: Inicio de la Transacción ---
        logger.info("BEGIN Transaction - Procesando pedido para cliente ID: {}", datosPedido.idCliente());

        // 1. Validar que el cliente exista
        Optional<Cliente> clienteOptional = clienteRepository.findById(datosPedido.idCliente());
        if (!clienteOptional.isPresent()) {
            logger.error("ValidacionNegocioException - Cliente no encontrado con ID: {}", datosPedido.idCliente());
            throw new ValidacionNegocioException("Cliente no encontrado con ID: " + datosPedido.idCliente());
        }
        Cliente cliente = clienteOptional.get();
        // O si necesitas el Usuario asociado al cliente:
        // Usuario usuarioCliente = cliente.getUsuario();


        // 2. Validar y obtener los productos y verificar stock
        BigDecimal valorTotalCalculado = BigDecimal.ZERO;
        List<DetalleFactura> itemsFactura = new ArrayList<>();
        List<MovimientoInventario> movimientosSalida = new ArrayList<>(); // Para movimientos de salida

        logger.info("Validando stock e items del pedido...");
        for (DatosItemPedido itemPedido : datosPedido.items()) {
            Optional<Producto> productoOptional = productoRepository.findById(itemPedido.idProducto());
            if (!productoOptional.isPresent()) {
                logger.error("ValidacionNegocioException - Producto no encontrado con ID: {}", itemPedido.idProducto());
                throw new ValidacionNegocioException("Producto no encontrado con ID: " + itemPedido.idProducto());
            }
            Producto producto = productoOptional.get();

            // *** Lógica de Validación de Stock basada en sumar movimientos ***
            // Consulta el stock actual sumando movimientos
            BigDecimal stockActual = movimientoInventarioRepository.calcularStockActualByProductoId(producto.getId());

            logger.debug("Producto ID: {}, Stock Actual: {}, Cantidad Pedida: {}", producto.getId(), stockActual, itemPedido.cantidad());

            if (stockActual.compareTo(new BigDecimal(itemPedido.cantidad())) < 0) {
                // Si el stock es menor que la cantidad pedida
                logger.error("StockInsuficienteException - Stock insuficiente para producto ID: {}", producto.getId());
                throw new StockInsuficienteException("Stock insuficiente para producto ID: " + producto.getId(), producto.getId());
            }

            // Crear el Detalle de Factura (aún no guardado)
            // Usar el precio actual del producto al momento de la venta
            DetalleFactura detalle = new DetalleFactura(
                    null, // La Factura se asignará después
                    producto,
                    itemPedido.cantidad(),
                    producto.getPrecioActual() // Captura el precio al momento de la venta
            );
            itemsFactura.add(detalle);

            // Calcular subtotal para sumar al total de la factura
            valorTotalCalculado = valorTotalCalculado.add(detalle.calcularSubtotal());

            // Crear el Movimiento de Inventario de Salida (aún no guardado)
            // La referencia_factura y la factura padre se asignarán después.
            // Usamos cantidad negativa para salida.
            // Nota: Si en BD referencia_factura es INT, solo puedes guardar IDs numéricos.
            // Usamos LocalDate.now() para la fecha si no configuras auto-asignación en BD.
            MovimientoInventario movimientoSalida = new MovimientoInventario(
                    producto,                                     // producto (Producto)
                    "salida_venta",                               // tipoMovimiento (String)
                    new BigDecimal(-itemPedido.cantidad()),       // cantidad (BigDecimal, negativa para salida)
                    0,                                            // cantidadDisponible (Integer) - *** Placeholder ***.
                    // Su significado real aquí es confuso si el stock se deriva.
                    // Pasa 0 o un valor que tenga sentido según tu regla para este campo.
                    LocalDate.now(),                              // fechaMovimiento (LocalDate) - Si no se auto-asigna en BD.
                    // Usa LocalDateTime.now() si el campo en BD es DATETIME.
                    null,                                         // referenciaFactura (Integer) - Se asignará DESPUÉS de guardar la Factura para tener el ID.
                    cliente.getUsuario(),                         // usuario (Usuario) - El usuario que realizó la compra/movimiento (el cliente).
                    null                                          // proveedor (Proveedor) - NULL para movimientos de venta.
            );

            movimientosSalida.add(movimientoSalida); // Añade el movimiento a la lista de movimientos a guardar
        }


        // 3. Crear la Factura principal
        // Usar LocalDate.now() si el campo fecha en BD es DATE y no se auto-asigna
        Factura factura = new Factura(
                LocalDate.now(),
                cliente,
                valorTotalCalculado, // Usamos el valor total calculado de los items
                datosPedido.metodoPago(),
                "PENDIENTE" // Estado inicial de la factura
        );

        // Asigna los items de detalle a la factura y viceversa (importante para JPA y Cascade)
        for (DetalleFactura item : itemsFactura) {
            factura.agregarItem(item); // El método agregarItem en Factura ya setea la referencia Factura en el Detalle
        }


        // 4. Guardar la Factura y sus Detalles (gracias a Cascade en Factura)
        // Al guardar la Factura, JPA también guardará los DetalleFactura asociados si la relación tiene Cascade.ALL
        logger.info("Guardando Factura y Detalles...");
        Factura facturaGuardada = facturaRepository.save(factura);
        logger.info("Factura {} y sus detalles guardados.", facturaGuardada.getId());

        // Después de guardar la factura, ya tenemos su ID generado
        String idFacturaGenerado = String.valueOf(facturaGuardada.getId());

        // 5. Guardar los Movimientos de Inventario de Salida
        logger.info("Guardando Movimientos de Inventario de Salida...");
        for (MovimientoInventario movimiento : movimientosSalida) {
            // Asigna la referencia a la factura recién creada
            movimiento.setReferenciaFactura(idFacturaGenerado); // Si referencia_factura es INT
            // Si referencia es VARCHAR y quieres guardar "Factura #ID":
            // movimiento.setReferencia("Factura #" + idFacturaGenerado);

            // Si tu entidad MovimientoInventario tiene una relación ManyToOne con Factura, asigna la entidad:
            // movimiento.setFactura(facturaGuardada); // Esto requiere añadir una FK a Factura en MovimientosInventario si no la tienes

            movimientoInventarioRepository.save(movimiento);
        }
        logger.info("Movimientos de inventario de salida guardados.");


        // 6. (Si usas caché) Actualizar cantidad_en_stock en la entidad Producto
        // Esto debe ocurrir DENTRO de la misma transacción.
        // logger.info("Actualizando stock en entidades Producto...");
        // for (DatosItemPedido itemPedido : datosPedido.items()) {
        //     Producto producto = productoRepository.getReferenceById(itemPedido.idProducto()); // O buscarlo si no lo tienes ya
        //     producto.setCantidadEnStock(producto.getCantidadEnStock() - itemPedido.cantidad()); // Resta la cantidad
        //     productoRepository.save(producto); // Guarda el producto actualizado (dentro de la misma transacción)
        // }
        // logger.info("Stock en entidades Producto actualizado.");


        // --- Simulación de Log: Fin de la Transacción ---
        logger.info("COMMIT Transaction - Pedido procesado exitosamente. Factura ID: {}", idFacturaGenerado);


        // 7. Construir y devolver el DTO de respuesta
        // Podrías obtener los detalles guardados de la factura si es necesario, o usar los objetos que ya tienes
        List<DatosRespuestaItemPedido> respuestaItems = new ArrayList<>();
        for (DetalleFactura item : itemsFactura) {
            respuestaItems.add(new DatosRespuestaItemPedido(
                    item.getProducto().getId(),
                    item.getProducto().getNombre(), // Obtener nombre del producto relacionado
                    item.getCantidadVendida(),
                    item.getPrecioUnitario(),
                    item.calcularSubtotal()
            ));
        }


        return new DatosRespuestaPedido(
                facturaGuardada.getId(),
                // Usar LocalDateTime.now() si tu DTO usa LocalDateTime
                // Si la base de datos auto-asigna la fecha, podrías querer recargar la factura para obtenerla exacta,
                // o usar LocalDate.now() si la precisión no es crítica en el DTO de respuesta.
                // Si tu campo fecha en BD es DATE, usa LocalDate.now() o la fecha de la entidad Factura.
                LocalDate.now().atStartOfDay(), // Ejemplo: convertir LocalDate a LocalDateTime si el DTO lo requiere
                facturaGuardada.getValorTotalFactura(),
                facturaGuardada.getMetodoPago(),
                facturaGuardada.getEstadoFactura(),
                respuestaItems
        );
    }

    // Puedes añadir otros métodos de servicio aquí, como:
    // - verDetallesPedido(Integer idFactura)
    // - cancelarPedido(Integer idFactura) - Esto también sería una transacción que implica movimientos de inventario de entrada (ajuste)
    // - etc.
}
