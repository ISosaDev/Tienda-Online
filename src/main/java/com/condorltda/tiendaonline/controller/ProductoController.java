package com.condorltda.tiendaonline.controller;



import com.condorltda.tiendaonline.domain.movimientoInventario.DatosListadoProducto;
import com.condorltda.tiendaonline.domain.producto.Producto;
import com.condorltda.tiendaonline.domain.producto.ProductoRepository;
import com.condorltda.tiendaonline.domain.service.InventarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity; // Para construir la respuesta
import org.springframework.web.bind.annotation.GetMapping; // Para manejar solicitudes GET
import org.springframework.web.bind.annotation.RequestMapping; // Para mapear la URL base
import org.springframework.web.bind.annotation.RestController; // Indica que es un controlador REST

import java.math.BigDecimal; // Para BigDecimal
import java.util.List; // Para listas
import java.util.stream.Collectors; // Para mapear

@RestController // Marca la clase como un controlador REST
@RequestMapping("/api/productos") // Mapea a /api/productos
public class ProductoController {

    // Inyecta el repositorio de productos para obtener los productos básicos
    @Autowired
    private ProductoRepository productoRepository;

    // Inyecta el servicio de inventario para obtener el stock (o podrías inyectar MovimientoInventarioRepository directamente si solo haces la suma)
    @Autowired
    private InventarioService inventarioService; // Asumimos que el servicio de inventario tiene un método para obtener stock

    // Endpoint para manejar solicitudes GET a /api/productos (listar todos)
    @GetMapping // Maneja solicitudes GET a /api/productos
    public ResponseEntity<List<DatosListadoProducto>> listarProductos() {
        // Obtiene todos los productos de la base de datos
        List<Producto> productos = productoRepository.findAll();

        // Para cada producto, obtener su stock y mapear a DTO
        List<DatosListadoProducto> datosListado = productos.stream()
                .map(producto -> {
                    // Obtiene el stock actual del producto usando el servicio de inventario
                    // Asumimos que InventarioService tiene un método para esto
                    BigDecimal stockDisponible = inventarioService.calcularStockActualByProductoId(producto.getId()); // Usamos el método que pusimos en MovimientoInventarioRepository y asumimos que el servicio lo llama o lo expone

                    // Crea el DTO de salida con los datos del producto y el stock
                    return new DatosListadoProducto(
                            producto.getId(),
                            producto.getNombre(),
                            producto.getMarca(),
                            producto.getPrecioActual(),
                            stockDisponible // Incluye el stock calculado
                    );
                })
                .collect(Collectors.toList()); // Recopila los DTOs en una lista

        // Devuelve una respuesta HTTP 200 OK con la lista de productos (con stock)
        return ResponseEntity.ok(datosListado);
    }

    // Podrías añadir otros endpoints aquí, como:
    // - @GetMapping("/{id}") para ver detalles de un producto específico
    // - @PostMapping para crear un nuevo producto (requiere un DTO de entrada y lógica de negocio)
    // - @PutMapping para actualizar un producto
    // - @DeleteMapping para eliminar un producto
}
