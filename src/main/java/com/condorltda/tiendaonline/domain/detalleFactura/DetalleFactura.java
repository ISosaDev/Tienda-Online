package com.condorltda.tiendaonline.domain.detalleFactura;


import com.condorltda.tiendaonline.domain.factura.Factura;
import com.condorltda.tiendaonline.domain.producto.Producto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal; // Para precio_unitario

@Table(name = "detalle_facturas") // Nombre de la tabla en la BD
@Entity(name = "DetalleFactura") // Nombre de la entidad
@Getter // Genera getters
@Setter // Genera setters (necesario para la relación bidireccional con Factura)
@NoArgsConstructor // Genera constructor sin argumentos
@AllArgsConstructor // Genera constructor con todos los argumentos
public class DetalleFactura {

    @Id // PK
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Autoincremento en MySQL
    @Column(name = "id_detalle_factura") // Nombre de columna de tu DDL
    private Integer id; // Tipo de dato Java que mapea a INT AUTO_INCREMENT en MySQL

    @ManyToOne(fetch = FetchType.LAZY) // Relación Muchos a Uno: Muchos Detalles pertenecen a una Factura
    @JoinColumn(name = "id_factura", nullable = false) // Columna FK en esta tabla (nombre de tu DDL)
    private Factura factura; // Relación con la entidad Factura

    @ManyToOne(fetch = FetchType.LAZY) // Relación Muchos a Uno: Muchos Detalles son de un Producto
    @JoinColumn(name = "id_producto", nullable = false) // Columna FK en esta tabla (nombre de tu DDL)
    private Producto producto; // Relación con la entidad Producto

    @Column(name = "cantidad_vendida", nullable = false) // INT NOT NULL
    private Integer cantidadVendida; // Usar Integer

    @Column(name = "precio_unitario", nullable = false) // DECIMAL(10, 2) NOT NULL
    private BigDecimal precioUnitario; // Usar BigDecimal (Precio al momento de la venta)

    // Nota: El subtotal de la línea NO se almacena, se calcula: cantidadVendida * precioUnitario

    // Constructor para usar al crear un nuevo DetalleFactura
    public DetalleFactura(Factura factura, Producto producto, Integer cantidadVendida, BigDecimal precioUnitario) {
        this.factura = factura; // Asegura que el lado Many tenga la referencia al padre Factura
        this.producto = producto;
        this.cantidadVendida = cantidadVendida;
        this.precioUnitario = precioUnitario;
    }

    // Constructor alternativo si creas el detalle ANTES de tener la Factura completa (ej. al validar items del pedido)
    public DetalleFactura(Producto producto, Integer cantidadVendida, BigDecimal precioUnitario) {
        this.producto = producto;
        this.cantidadVendida = cantidadVendida;
        this.precioUnitario = precioUnitario;
        // La 'factura' se asignará cuando se añada a la lista de items de la Factura padre (ver Factura.agregarItem)
    }

    // Método para calcular el subtotal de esta línea
    public BigDecimal calcularSubtotal() {
        if (this.cantidadVendida != null && this.precioUnitario != null) {
            return this.precioUnitario.multiply(new BigDecimal(this.cantidadVendida));
        }
        return BigDecimal.ZERO; // Devuelve 0 si faltan datos para calcular
    }


}
