package com.condorltda.tiendaonline.domain.proveedor;


import com.condorltda.tiendaonline.domain.ciudad.Ciudad;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "proveedores") // Nombre de la tabla en la BD
@Entity(name = "Proveedor") // Nombre de la entidad
@Getter // Genera getters
@Setter // Genera setters si es necesario
@NoArgsConstructor // Genera constructor sin argumentos
@AllArgsConstructor // Genera constructor con todos los argumentos
public class Proveedor {

    @Id // Marca este atributo como la clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configura la estrategia de autoincremento para MySQL
    @Column(name = "id_proveedor") // Mapea al nombre exacto de la columna en la tabla
    private Integer id; // Tipo de dato Java que mapea a INT AUTO_INCREMENT en MySQL

    @Column(name = "tipo_persona", nullable = false) // VARCHAR(10) NOT NULL
    private String tipoPersona; // Ej: 'Natural', 'Juridica'

    @Column(name = "tipo_identificacion", nullable = false) // VARCHAR(10) NOT NULL
    private String tipoIdentificacion; // Ej: 'CC', 'NIT'

    @Column(name = "numero_identificacion", nullable = false) // INT NOT NULL (Según tu DDL original, aunque discutimos VARCHAR)
    private String numeroIdentificacion; // Usar Integer si insistes en INT en BD

    @Column(name = "nombre_razonsocial", nullable = false) // VARCHAR(100) NOT NULL
    private String nombreRazonSocial; // Nombre del proveedor o razón social

    //@Column(name = "contacto") // VARCHAR(100) - Aunque no estaba en tu DDL original, es común tener un contacto
    //private String contacto; // Nombre de la persona de contacto

    @Column(name = "telefono", nullable = false) // INT NOT NULL (Según tu DDL original, aunque discutimos VARCHAR)
    private String telefono; // Usar Integer si insistes en INT en BD

    @Column(name = "email") // VARCHAR(100) - Puede ser nulo según tu DDL original
    private String email; // Email de contacto

    @Column(name = "direccion", nullable = false) // VARCHAR(100) NOT NULL (Según tu DDL original)
    private String direccion;

    @ManyToOne(fetch = FetchType.LAZY) // Relación Muchos a Uno: Muchos Proveedores están en una Ciudad
    @JoinColumn(name = "id_ciudad", nullable = false) // Columna FK en la tabla 'proveedores' (nombre de tu DDL)
    private Ciudad ciudad; // Relación con la entidad Ciudad

    // Nota: La restricción UNIQUE KEY (tipo_identificacion, numero_identificacion)
    // se define en la tabla en el DDL. JPA la entenderá a través de la configuración
    // en el modelador/DLL. No es necesario añadir @Column(unique=true) a ambos campos
    // individualmente aquí, ya que es una restricción compuesta.

    // Constructor para usar al crear un nuevo Proveedor sin especificar ID
    public Proveedor(String tipoPersona, String tipoIdentificacion, String numeroIdentificacion, String nombreRazonSocial, String contacto, String telefono, String email, String direccion, Ciudad ciudad) {
        this.tipoPersona = tipoPersona;
        this.tipoIdentificacion = tipoIdentificacion;
        this.numeroIdentificacion = numeroIdentificacion;
        this.nombreRazonSocial = nombreRazonSocial;
        //this.contacto = contacto;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
        this.ciudad = ciudad;
    }

}
