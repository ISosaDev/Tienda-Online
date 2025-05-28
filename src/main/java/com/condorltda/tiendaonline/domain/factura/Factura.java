package com.condorltda.tiendaonline.domain.factura;


import com.condorltda.tiendaonline.domain.cliente.Cliente;
import com.condorltda.tiendaonline.domain.detalleFactura.DetalleFactura;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate; // Usar LocalDate para tipo DATE de MySQL
import java.math.BigDecimal; // Para valor_total_factura
import java.time.LocalDateTime;
import java.util.List; // Para relación con DetalleFactura

@Table(name = "facturas") // Nombre de la tabla en la BD
@Entity(name = "Factura") // Nombre de la entidad
@Getter // Genera getters
@Setter // Genera setters si es necesario (ej. para actualizar estado)
@NoArgsConstructor // Genera constructor sin argumentos
@AllArgsConstructor // Genera constructor con todos los argumentos

public class Factura {

    @Id // PK
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Autoincremento en MySQL
    @Column(name = "id_factura") // Nota: Tu DDL usaba id_facturas, nuestro modelo sugería id_factura. Mantengo tu DDL.
    private Integer id; // Tipo de dato Java que mapea a INT AUTO_INCREMENT en MySQL

    @Column(name = "fecha", nullable = false) // DATE NOT NULL en tu DDL. Si necesitas hora, usa DATETIME y LocalDateTime en Java
    private LocalDateTime fecha; // Usar LocalDate para tipo DATE de MySQL

    @ManyToOne(fetch = FetchType.LAZY) // Relación Muchos a Uno: Muchas Facturas pertenecen a un Cliente
    @JoinColumn(name = "id_cliente", nullable = false) // Columna FK en la tabla 'facturas' (nombre de tu DDL)
    private Cliente cliente; // Relación con la entidad Cliente

    @Column(name = "valor_total_factura", nullable = false) // DECIMAL(10, 2) NOT NULL
    private BigDecimal valorTotalFactura; // Usar BigDecimal

    @Column(name = "metodo_pago", nullable = false) // VARCHAR(10) NOT NULL
    private String metodoPago; // Podría mapear a un Enum si defines uno

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_factura", nullable = false) // VARCHAR(10) NOT NULL
    private EstadoFactura estadoFactura; // Podría mapear a un Enum si defines uno

    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true) // Relación Uno a Muchos: Una Factura tiene muchos DetalleFactura
    // cascade = CascadeType.ALL: Si guardas/borras la Factura, JPA también guarda/borra sus detalles asociados
    // orphanRemoval = true: Si quitas un DetalleFactura de la lista 'items', JPA lo borra de la BD
    private List<DetalleFactura> items; // Lista de los detalles de la factura

    // Constructor para usar al crear una nueva Factura (los items se añaden por separado)
    public Factura(LocalDateTime fecha, Cliente cliente, BigDecimal valorTotalFactura, String metodoPago, EstadoFactura estadoFactura) {
        this.fecha = LocalDateTime.now();
        this.cliente = cliente;
        this.valorTotalFactura = valorTotalFactura; // Este valor debería calcularse o validarse
        this.metodoPago = metodoPago;
        this.estadoFactura = estadoFactura;
    }

    // Método para añadir un detalle a la factura (importante para la relación OneToMany)
    public void agregarItem(DetalleFactura item) {
        if (this.items == null) {
            this.items = new java.util.ArrayList<>();
        }
        this.items.add(item);
        item.setFactura(this); // Asegura que el lado Many (DetalleFactura) también tenga la referencia al padre (Factura)
    }

    // Métodos para actualizar estado o valor si es necesario
    public void actualizarEstado(EstadoFactura nuevoEstado) {
        this.estadoFactura = nuevoEstado;
    }

    // Podrías tener un método para (re)calcular el valor total basado en los items
    public void calcularValorTotal() {
        if (this.items != null) {
            this.valorTotalFactura = this.items.stream()
                    .map(item -> item.getPrecioUnitario().multiply(new BigDecimal(item.getCantidadVendida())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            this.valorTotalFactura = BigDecimal.ZERO;
        }
    }
}
