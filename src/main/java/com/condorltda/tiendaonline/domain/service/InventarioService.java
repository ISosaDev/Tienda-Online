package com.condorltda.tiendaonline.domain.service;


import com.condorltda.tiendaonline.domain.ValidacionNegocioException;
import com.condorltda.tiendaonline.domain.movimientoInventario.DatosRegistroEntradaInventario;
import com.condorltda.tiendaonline.domain.movimientoInventario.MovimientoInventario;
import com.condorltda.tiendaonline.domain.movimientoInventario.MovimientoInventarioRepository;
import com.condorltda.tiendaonline.domain.producto.Producto;
import com.condorltda.tiendaonline.domain.producto.ProductoRepository;
import com.condorltda.tiendaonline.domain.proveedor.Proveedor;
import com.condorltda.tiendaonline.domain.proveedor.ProveedorRepository;
import com.condorltda.tiendaonline.domain.usuario.Usuario;
import com.condorltda.tiendaonline.domain.usuario.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importante para @Transactional

import java.time.LocalDate; // Usar LocalDate
import java.math.BigDecimal; // Para cantidad

// Importa el logger
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class InventarioService {

    // Obtiene un logger para esta clase (para simular el log de transacciones)
    private static final Logger logger = LoggerFactory.getLogger(InventarioService.class);

    // Inyección de dependencias de los Repositorios necesarios
    @Autowired
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @Autowired
    private ProductoRepository productoRepository; // Necesario para validar producto

    @Autowired
    private ProveedorRepository proveedorRepository; // Necesario para validar proveedor

    @Autowired
    private UsuarioRepository usuarioRepository; // Necesario para validar usuario que registra

    // ****************************************************************
    // Método para registrar una entrada de inventario - Transacción
    // ****************************************************************
    @Transactional // Esta anotación indica a Spring que gestione una transacción para este método.
    // Si una RuntimeException (como ValidacionNegocioException) ocurre, Spring hará ROLLBACK.
    // Si el método finaliza sin excepciones (o con excepciones verificadas por defecto), Spring hará COMMIT.
    public void registrarEntradaInventario(DatosRegistroEntradaInventario datosEntrada) {

        // --- Simulación de Log: Inicio de la Transacción ---
        logger.info("BEGIN Transaction - Registrando entrada inventario para producto ID: {}", datosEntrada.idProducto());


        // 1. Validar que el producto, proveedor y usuario existan
        Producto producto = productoRepository.findById(datosEntrada.idProducto())
                .orElseThrow(() -> new ValidacionNegocioException("Producto no encontrado con ID: " + datosEntrada.idProducto()));

        Proveedor proveedor = proveedorRepository.findById(datosEntrada.idProveedor())
                .orElseThrow(() -> new ValidacionNegocioException("Proveedor no encontrado con ID: " + datosEntrada.idProveedor()));

        Usuario usuarioRegistro = usuarioRepository.findById(datosEntrada.idUsuarioRegistro())
                .orElseThrow(() -> new ValidacionNegocioException("Usuario (Personal) no encontrado con ID: " + datosEntrada.idUsuarioRegistro()));


        // 2. Validar y obtener el valor numérico de la referencia si el campo en BD es INT
        Integer referenciaNumerica = null;
        // Aquí intentamos convertir la referencia del DTO (String) a Integer
        // Esto SOLO funciona si el campo referencia_factura en tu BD es INT y esperas un número ahí
        // Si referencia_factura debería almacenar texto (ej. "OC#123"), el tipo en BD y Entidad debería ser VARCHAR.
        try {
            if (datosEntrada.referencia() != null && !datosEntrada.referencia().trim().isEmpty()) {
                referenciaNumerica = Integer.parseInt(datosEntrada.referencia()); // Intenta convertir a Integer
            }
        } catch (NumberFormatException e) {
            // Manejar el error si la referencia NO es un número válido y el campo en BD es INT
            logger.error("ValidacionNegocioException - La referencia de entrada debe ser un número válido si el campo en BD es INT. Valor: {}", datosEntrada.referencia());
            throw new ValidacionNegocioException("La referencia de entrada debe ser un número válido.");
        }


        // ******************************************************************
        // Corregido: Crear el Movimiento de Inventario de Entrada
        // Usa el constructor sin el ID para crear un nuevo movimiento
        // Asegúrate del orden y los tipos de datos: Producto, String, BigDecimal, Integer, LocalDate, Integer, Usuario, Proveedor
        // ******************************************************************
        MovimientoInventario movimientoEntrada = new MovimientoInventario(
                producto,                               // producto (Producto)
                "entrada",                              // tipoMovimiento (String)
                datosEntrada.cantidad(),                // cantidad (BigDecimal)
                0,                                      // cantidadDisponible (Integer) - Placeholder.
                LocalDate.now(),                        // fechaMovimiento (LocalDate)
                datosEntrada.referencia(),              // *** Corregido: Usa directamente el String 'referencia' del DTO ***
                // El constructor de MovimientoInventario ahora espera String aquí.
                usuarioRegistro,                        // usuario (Usuario)
                proveedor                               // proveedor (Proveedor)
        );


        // 3. Guardar el Movimiento de Inventario
        logger.info("Guardando Movimiento de Inventario de Entrada...");
        movimientoInventarioRepository.save(movimientoEntrada);
        logger.info("Movimiento de inventario de entrada guardado (ID generado por BD si es autoincremental).");


        // 4. (Si usas caché) Actualizar cantidad_en_stock en la entidad Producto
        // Esto debe ocurrir DENTRO de la misma transacción.
        // logger.info("Actualizando stock en entidad Producto...");
        // Producto productoParaActualizar = productoRepository.getReferenceById(producto.getId()); // Obtener referencia
        // productoParaActualizar.setCantidadEnStock(productoParaActualizar.getCantidadEnStock().add(datosEntrada.cantidad())); // Suma la cantidad (si cantidad en Producto es BigDecimal)
        // // Si cantidadEnStock en Producto es Integer:
        // // productoParaActualizar.setCantidadEnStock(productoParaActualizar.getCantidadEnStock() + datosEntrada.cantidad().intValue()); // Suma la cantidad (convierte a Integer si cantidad en DTO es BigDecimal)
        // productoRepository.save(productoParaActualizar); // Guarda el producto actualizado
        // logger.info("Stock en entidad Producto actualizado.");

        // --- Simulación de Log: Fin de la Transacción ---
        logger.info("COMMIT Transaction - Entrada de inventario registrada exitosamente.");

        // No devuelve nada si la operación es simplemente registrar
    }

    // ****************************************************************
    // Método: Obtener el stock actual de un producto
    // Expone la funcionalidad del repositorio para calcular el stock.
    // ****************************************************************
    // @Transactional(readOnly = true) // Opcional: Marcar como solo lectura para consultas
    public BigDecimal calcularStockActualByProductoId(Integer productoId) {
        // Llama al método del repositorio para obtener la suma de cantidades
        // El método del repositorio ya maneja si no hay movimientos (COALESCE -> 0)
        BigDecimal stock = movimientoInventarioRepository.calcularStockActualByProductoId(productoId);

        return stock; // Devuelve el stock calculado
    }

    // Puedes añadir otros métodos de servicio para inventario aquí, como:
    // - verHistorialMovimientosProducto(Integer productoId)
    // - etc.
}
