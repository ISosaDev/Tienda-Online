package com.condorltda.tiendaonline.domain.movimientoInventario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Integer> {

    // Métodos CRUD básicos automáticos.

    // **************** Métodos Clave para la Lógica de Stock ****************

    // Método para calcular el stock actual de un producto sumando todos sus movimientos
    // Esta es la forma 'verdadera' de obtener el stock si no usas campo cache
    // Usar una consulta @Query para esto
    // NOTA: Sumar cantidades DECIMAL en Java requiere cuidado (usar BigDecimal.add())
    // Es mejor hacer la suma directamente en SQL para precisión y rendimiento.
    // Ejemplo con @Query:
    /*
    @Query("SELECT SUM(m.cantidad) FROM MovimientoInventario m WHERE m.producto.id = :productoId")
    BigDecimal sumarCantidadesByProductoId(@Param("productoId") Integer productoId);
    */
    // O si el campo 'cantidad' es Integer/Integer:
    // @Query("SELECT SUM(m.cantidad) FROM MovimientoInventario m WHERE m.producto.id = :productoId")
    // Integer sumarCantidadesByProductoId(@Param("productoId") Integer productoId);

    // Método para calcular el stock actual de un producto sumando todos sus movimientos
    // Suma las cantidades de movimientos para un producto específico.
    // Usar @Query para hacer la suma directamente en la base de datos.
    // Retorna BigDecimal porque la cantidad es DECIMAL en la entidad.
    // El COALESCE(SUM(m.cantidad), 0) asegura que si no hay movimientos, retorne 0 en lugar de NULL.
    @Query("SELECT COALESCE(SUM(m.cantidad), 0) FROM MovimientoInventario m WHERE m.producto.id = :productoId")
    BigDecimal calcularStockActualByProductoId(@Param("productoId") Integer productoId);

    // Métodos para encontrar movimientos por tipo o referencia
    // List<MovimientoInventario> findByTipoMovimiento(String tipoMovimiento);
    // List<MovimientoInventario> findByReferenciaFactura(Integer referenciaFactura); // Si referencia_factura es INT en BD
}
