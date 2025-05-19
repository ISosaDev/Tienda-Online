package com.condorltda.tiendaonline.domain.ciudad;


import com.condorltda.tiendaonline.domain.departamento.Departamento;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "ciudades") // Nombre de la tabla en la BD
@Entity(name = "Ciudad") // Nombre de la entidad
@Getter // Genera getters
@Setter // Genera setters si es necesario
@NoArgsConstructor // Genera constructor sin argumentos
@AllArgsConstructor // Genera constructor con todos los argumentos
public class Ciudad {

    @Id // Marca este atributo como la clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configura la estrategia de autoincremento para MySQL
    @Column(name = "id_ciudad") // Mapea al nombre exacto de la columna en la tabla
    private Integer id; // Tipo de dato Java que mapea a SMALLINT AUTO_INCREMENT en MySQL

    @Column(name = "nombre_ciudad", nullable = false) // Mapea al nombre de la columna, es NOT NULL en BD
    private String nombre; // VARCHAR(100) en MySQL (usamos 100 en el último DDL)

    @ManyToOne(fetch = FetchType.LAZY) // Relación Muchos a Uno: Muchas Ciudades pertenecen a un Departamento
    @JoinColumn(name = "id_departamento", nullable = false) // Columna FK en la tabla 'ciudades' (nombre de tu DDL)
    private Departamento departamento; // Relación con la entidad Departamento

    // Constructor para usar al crear una nueva Ciudad sin especificar ID
    public Ciudad(String nombre, Departamento departamento) {
        this.nombre = nombre;
        this.departamento = departamento;
    }
}
