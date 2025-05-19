package com.condorltda.tiendaonline.domain.personal;


import com.condorltda.tiendaonline.domain.usuario.Usuario;
import com.condorltda.tiendaonline.domain.ciudad.Ciudad;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "personal_empresa") // Nombre de la tabla en la BD
@Entity(name = "PersonalEmpresa") // Nombre de la entidad
@Getter // Genera getters
@Setter // Genera setters si es necesario
@NoArgsConstructor // Genera constructor sin argumentos
@AllArgsConstructor // Genera constructor con todos los argumentos
public class PersonalEmpresa {


    @Id // Marca este atributo como la clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configura la estrategia de autoincremento para MySQL
    @Column(name = "id_empleado") // Mapea al nombre exacto de la columna en la tabla
    private Integer id; // Tipo de dato Java que mapea a MEDIUMINT AUTO_INCREMENT en MySQL

    @OneToOne // Relación Uno a Uno con Usuario
    @JoinColumn(name = "usuarios_id_usuario", nullable = false, unique = true) // FK en la tabla 'personal_empresa', NOT NULL y UNIQUE
    private Usuario usuario; // Relación con la entidad Usuario

    @Column(name = "tipo_identificacion", nullable = false) // VARCHAR(10) NOT NULL
    private String tipoIdentificacion; // Ej: 'CC', 'TI', etc.

    @Column(name = "numero_identificacion", nullable = false) // INT NOT NULL (Según tu DDL original, aunque discutimos VARCHAR)
    private Integer numeroIdentificacion; // Usar Integer si insistes en INT en BD

    @Column(name = "nombres_apellidos", nullable = false) // VARCHAR(50) NOT NULL
    private String nombresApellidos;

    @Column(name = "cargo", nullable = false) // VARCHAR(10) NOT NULL
    private String cargo; // Ej: 'Compra', 'Admin', etc.

    @Column(name = "tipo_contrato", nullable = false) // VARCHAR(20) NOT NULL
    private String tipoContrato;

    @Column(name = "email", nullable = false) // VARCHAR(100) NOT NULL (Email corporativo o de contacto del empleado)
    private String email; // Nota: El email principal de login está en la tabla Usuarios

    @Column(name = "telefono", nullable = false) // INT NOT NULL (Según tu DDL original, aunque discutimos VARCHAR)
    private Integer telefono; // Usar Integer si insistes en INT en BD (Teléfono de contacto del empleado)

    @Column(name = "direccion", nullable = false) // VARCHAR(50) NOT NULL (Dirección de residencia o contacto del empleado)
    private String direccion;

    @ManyToOne(fetch = FetchType.LAZY) // Relación Muchos a Uno: Muchos empleados viven en una Ciudad
    @JoinColumn(name = "ciudades_id_ciudad", nullable = false) // Columna FK en la tabla 'personal_empresa'
    private Ciudad ciudad; // Relación con la entidad Ciudad

    // Nota: La restricción UNIQUE KEY (tipo_identificacion, numero_identificacion)
    // se define en la tabla en el DDL. JPA la entenderá.
    // La restricción UNIQUE KEY (usuarios_id_usuario) se maneja con @JoinColumn(unique=true).

    // Constructor para usar al crear un nuevo registro de PersonalEmpresa
    public PersonalEmpresa(Usuario usuario, String tipoIdentificacion, Integer numeroIdentificacion, String nombresApellidos, String cargo, String tipoContrato, String email, Integer telefono, String direccion, Ciudad ciudad) {
        this.usuario = usuario;
        this.tipoIdentificacion = tipoIdentificacion;
        this.numeroIdentificacion = numeroIdentificacion;
        this.nombresApellidos = nombresApellidos;
        this.cargo = cargo;
        this.tipoContrato = tipoContrato;
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
        this.ciudad = ciudad;
    }

    // Puedes añadir métodos para actualizar datos del empleado si es necesario
}
