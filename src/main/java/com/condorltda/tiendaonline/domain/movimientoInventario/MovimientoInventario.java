package com.condorltda.tiendaonline.domain.movimientoInventario;



import com.condorltda.tiendaonline.domain.producto.Producto;
import com.condorltda.tiendaonline.domain.proveedor.Proveedor;
import com.condorltda.tiendaonline.domain.usuario.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate; // Usar LocalDate para tipo DATE de MySQL
import java.math.BigDecimal; // Para cantidad

@Table(name = "movimientos_inventario") // Nombre de la tabla en la BD
@Entity(name = "MovimientoInventario") // Nombre de la entidad
@Getter // Genera getters
@Setter // Genera setters si es necesario (menos común para movimientos históricos)
@NoArgsConstructor // Genera constructor sin argumentos
@AllArgsConstructor // Genera constructor con todos los argumentos
public class MovimientoInventario {

    @Id // PK
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Autoincremento en MySQL
    @Column(name = "id_movimientos") // Nombre de columna de tu DDL
    private Integer id; // Tipo de dato Java que mapea a INT AUTO_INCREMENT en MySQL (considerar BIGINT si esperas MUCHOS movimientos)

    @ManyToOne(fetch = FetchType.LAZY) // Relación Muchos a Uno: Muchos Movimientos son de un Producto
    @JoinColumn(name = "productos_id_producto", nullable = false) // Columna FK en esta tabla (nombre de tu DDL)
    private Producto producto; // Relación con la entidad Producto

    @Column(name = "tipo_movimiento", nullable = false) // VARCHAR(10) NOT NULL
    private String tipoMovimiento; // Podría mapear a un Enum (ej. TipoMovimiento: ENTRADA, SALIDA_VENTA, AJUSTE)

    @Column(name = "cantidad", nullable = false) // DECIMAL(10, 2) NOT NULL (Usando DECIMAL como sugerimos para cantidades)
    private BigDecimal cantidad; // Cantidad del movimiento (+/-). Usar BigDecimal.

    @Column(name = "cantidad_disponible", nullable = false) // MEDIUMINT NOT NULL (Según tu DDL original)
    private Integer cantidadDisponible; // Este campo existía en tu DDL, pero lo habíamos sugerido eliminar/derivar. Mantenido según tu script.

    @Column(name = "fecha_movimiento", nullable = false) // DATE NOT NULL (Según tu DDL original) - Si añadiste DEFAULT CURRENT_TIMESTAMP, la BD lo maneja
    private LocalDate fechaMovimiento; // Usar LocalDate para tipo DATE de MySQL

    @Column(name = "referencia", nullable = false) // INT NOT NULL (Según tu DDL original) - Lo habíamos sugerido como VARCHAR para referencias de texto/IDs
    private String referenciaFactura; // Mantenido como Integer según tu script. Nota: Esto limita a referenciar IDs numéricos.

    @ManyToOne(fetch = FetchType.LAZY) // Relación Muchos a Uno: Muchos Movimientos registrados por un Usuario
    @JoinColumn(name = "usuarios_id_usuario") // Columna FK, puede ser NULL en BD
    private Usuario usuario; // Usuario (personal o cliente) que realizó/causó el movimiento

    @ManyToOne(fetch = FetchType.LAZY) // Relación Muchos a Uno: Muchas Entradas de Movimiento de un Proveedor
    @JoinColumn(name = "proveedores_id_proveedor") // Columna FK, puede ser NULL en BD
    private Proveedor proveedor; // Proveedor asociado si es una entrada

    // Constructor para usar al crear un nuevo MovimientoInventario
    public MovimientoInventario(Producto producto, String tipoMovimiento, BigDecimal cantidad, Integer cantidadDisponible, LocalDate fechaMovimiento, String referenciaFactura, Usuario usuario, Proveedor proveedor) {
        this.producto = producto;
        this.tipoMovimiento = tipoMovimiento;
        this.cantidad = cantidad;
        this.cantidadDisponible = cantidadDisponible; // Atributo que estaba en tu DDL
        this.fechaMovimiento = fechaMovimiento;
        this.referenciaFactura = referenciaFactura; // Atributo que estaba en tu DDL
        this.usuario = usuario;
        this.proveedor = proveedor;
    }

    // Constructor alternativo para salida por venta (referencia=idFactura, usuario=cliente, proveedor=null)
    public MovimientoInventario(Producto producto, BigDecimal cantidad, Integer cantidadDisponible, LocalDate fechaMovimiento, String referenciaFactura, Usuario usuario) {
        this.producto = producto;
        this.tipoMovimiento = "salida_venta"; // Define el tipo directamente
        this.cantidad = cantidad;
        this.cantidadDisponible = cantidadDisponible;
        this.fechaMovimiento = fechaMovimiento;
        this.referenciaFactura = referenciaFactura;
        this.usuario = usuario;
        this.proveedor = null; // No hay proveedor en una venta
    }

    // Constructor alternativo para entrada por compra (referencia=idRecepcion/facturaProveedor, usuario=personal, proveedor=proveedor)
    public MovimientoInventario(Producto producto, BigDecimal cantidad, Integer cantidadDisponible, LocalDate fechaMovimiento, String referenciaFactura, Usuario usuario, Proveedor proveedor) {
        this.producto = producto;
        this.tipoMovimiento = "entrada"; // Define el tipo directamente
        this.cantidad = cantidad;
        this.cantidadDisponible = cantidadDisponible;
        this.fechaMovimiento = fechaMovimiento;
        this.referenciaFactura = referenciaFactura; // Aquí se usaría el ID de la factura de proveedor si es Integer
        this.usuario = usuario;
        this.proveedor = proveedor;
    }
}
