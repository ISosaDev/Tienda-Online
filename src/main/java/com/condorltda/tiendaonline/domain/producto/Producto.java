package com.condorltda.tiendaonline.domain.producto;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter; // Añadido Setter para permitir actualizar precio si es necesario
import java.math.BigDecimal; // Para precio_actual

@Table(name = "productos") // Nombre de la tabla en la BD
@Entity(name = "Producto") // Nombre de la entidad
@Getter // Genera getters para todos los atributos
@Setter // Genera setters para permitir actualizar atributos si es necesario (ej. precio)
@NoArgsConstructor // Genera constructor sin argumentos (necesario para JPA)
@AllArgsConstructor // Genera constructor con todos los argumentos
public class Producto {

    @Id // Marca este atributo como la clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configura la estrategia de autoincremento para MySQL
    @Column(name = "id_producto") // Mapea al nombre exacto de la columna en la tabla (si difiere del nombre del atributo Java)
    private Integer id; // Tipo de dato Java que mapea a INT AUTO_INCREMENT en MySQL

    @Column(name = "nombre", nullable = false) // Mapea a la columna nombre, es NOT NULL en BD
    private String nombre; // VARCHAR(50) en MySQL

    @Column(name = "marca") // VARCHAR(20) en MySQL
    private String marca;

    @Column(name = "descripcion") // VARCHAR(150) en MySQL, aunque TEXT sería mejor para descripciones largas
    private String descripcion;

    @Column(name = "precio_actual", nullable = false) // DECIMAL(20, 2) en MySQL, es NOT NULL
    private BigDecimal precioActual; // Usar BigDecimal para valores monetarios

    // Nota: El atributo cantidad_disponible NO está en esta entidad, ya que se deriva de movimientos_inventario.
    // La lógica para obtener el stock se hará en el Service o Repository con una consulta.

    // Constructor para usar al crear un nuevo Producto sin especificar ID (BD lo genera)
    public Producto(String nombre, String marca, String descripcion, BigDecimal precioActual) {
        this.nombre = nombre;
        this.marca = marca;
        this.descripcion = descripcion;
        this.precioActual = precioActual;
    }
}
