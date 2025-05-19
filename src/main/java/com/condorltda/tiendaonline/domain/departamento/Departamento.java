package com.condorltda.tiendaonline.domain.departamento;


import com.condorltda.tiendaonline.domain.ciudad.Ciudad;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List; // Para relación con Ciudades

@Table(name = "departamentos") // Nombre de la tabla en la BD
@Entity(name = "Departamento") // Nombre de la entidad
@Getter // Genera getters
@Setter // Genera setters si es necesario
@NoArgsConstructor // Genera constructor sin argumentos
@AllArgsConstructor // Genera constructor con todos los argumentos
public class Departamento {

    @Id // Marca este atributo como la clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configura la estrategia de autoincremento para MySQL
    @Column(name = "id_departamento") // Mapea al nombre exacto de la columna en la tabla
    private Integer id; // Tipo de dato Java que mapea a SMALLINT AUTO_INCREMENT en MySQL

    @Column(name = "nombre", nullable = false) // Mapea al nombre de la columna, es NOT NULL en BD
    private String nombre; // VARCHAR(100) en MySQL (usamos 100 en el último DDL)

    // Relación Uno a Muchos: Un Departamento tiene muchas Ciudades
    @OneToMany(mappedBy = "departamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ciudad> ciudades; // Lista de ciudades en este departamento

    // Constructor para usar al crear un nuevo Departamento sin especificar ID
    public Departamento(String nombre) {
        this.nombre = nombre;
    }

    // Método para añadir una ciudad (opcional pero útil para mantener consistencia en ambos lados de la relación)
    public void agregarCiudad(Ciudad ciudad) {
        if (this.ciudades == null) {
            this.ciudades = new java.util.ArrayList<>();
        }
        this.ciudades.add(ciudad);
        ciudad.setDepartamento(this); // Asegura que el lado Many (Ciudad) también tenga la referencia al padre (Departamento)
    }
}
