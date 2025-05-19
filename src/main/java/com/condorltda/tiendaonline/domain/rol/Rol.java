package com.condorltda.tiendaonline.domain.rol;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "rol") // Nombre de la tabla en la BD
@Entity(name = "Rol") // Nombre de la entidad
@Getter // Genera getters
@Setter // Genera setters si es necesario
@NoArgsConstructor // Genera constructor sin argumentos
@AllArgsConstructor // Genera constructor con todos los argumentos
public class Rol {

    @Id // Marca este atributo como la clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configura la estrategia de autoincremento para MySQL
    @Column(name = "id_rol") // Mapea al nombre exacto de la columna en la tabla
    private Integer id; // Tipo de dato Java que mapea a TINYINT AUTO_INCREMENT en MySQL

    @Column(name = "nombre_rol", nullable = false) // Mapea al nombre de la columna, es NOT NULL en BD
    private String nombreRol; // VARCHAR(20) en MySQL

    // Constructor para usar al crear un nuevo Rol sin especificar ID (BD lo genera)
    public Rol(String nombreRol) {
        this.nombreRol = nombreRol;
    }

    // Si necesitas un constructor para cargar roles existentes (ej. para asociar a usuarios)
    // public Rol(Integer id, String nombreRol) {
    //     this.id = id;
    //     this.nombreRol = nombreRol;
    // }
}
