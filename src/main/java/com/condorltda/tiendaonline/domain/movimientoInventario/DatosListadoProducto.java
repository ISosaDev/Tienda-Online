package com.condorltda.tiendaonline.domain.movimientoInventario;


import java.math.BigDecimal; // Para precio y stock

// Record para mostrar información básica de un producto en listados
public record DatosListadoProducto(


        Integer id,
        String nombre,
        String marca,
        BigDecimal precioActual,

        // Este campo se añadirá DESPUÉS de calcular el stock en el Service/Repository
        BigDecimal cantidadDisponible // Stock actual derivado de movimientos_inventario
) {

    // Constructor para mapear desde la entidad Producto y añadir el stock
    public DatosListadoProducto(Integer id, String nombre, String marca, BigDecimal precioActual, BigDecimal cantidadDisponible) {
        this.id = id;
        this.nombre = nombre;
        this.marca = marca;
        this.precioActual = precioActual;
        this.cantidadDisponible = cantidadDisponible;
    }
    // Podrías tener un constructor que reciba la Entidad Producto
    // public DatosListadoProducto(Producto producto, BigDecimal cantidadDisponible) {
    //     this(producto.getId(), producto.getNombre(), producto.getMarca(), producto.getPrecioActual(), cantidadDisponible);
    // }
}
