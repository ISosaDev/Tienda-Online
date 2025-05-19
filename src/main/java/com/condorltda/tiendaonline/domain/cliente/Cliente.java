package com.condorltda.tiendaonline.domain.cliente;


import com.condorltda.tiendaonline.domain.factura.Factura;
import com.condorltda.tiendaonline.domain.usuario.Usuario;
import com.condorltda.tiendaonline.domain.ciudad.Ciudad;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List; // Para relación con Facturas

@Table(name = "clientes") // Nombre de la tabla en la BD
@Entity(name = "Cliente") // Nombre de la entidad
@Getter // Genera getters
@Setter // Genera setters
@NoArgsConstructor // Genera constructor sin argumentos
@AllArgsConstructor // Genera constructor con todos los argumentos
public class Cliente {

    @Id // PK
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Autoincremento en MySQL
    @Column(name = "id_cliente")
    private Integer id; // Tipo de dato Java que mapea a MEDIUMINT AUTO_INCREMENT en MySQL

    @OneToOne // Relación Uno a Uno con Usuario
    @JoinColumn(name = "id_usuario", nullable = false, unique = true) // FK en la tabla 'clientes' referenciando a 'usuarios', es NOT NULL y UNIQUE
    private Usuario usuario; // Relación con la entidad Usuario

    @Column(name = "tipo_persona", nullable = false) // VARCHAR(10) NOT NULL
    private String tipoPersona;

    @Column(name = "tipo_identificacion", nullable = false) // VARCHAR(10) NOT NULL
    private String tipoIdentificacion; // Usar String, no int, como discutimos para VARCHAR

    @Column(name = "numero_identificacion", nullable = false) // INT NOT NULL (Según tu DDL original, aunque discutimos VARCHAR)
    private Integer numeroIdentificacion; // Usar Integer si insistes en INT en BD

    @Column(name = "nombres_o_razonsocial", nullable = false) // VARCHAR(100) NOT NULL
    private String nombresORazonSocial;


    @Column(name = "telefono", nullable = false) // INT NOT NULL (Según tu DDL original, aunque discutimos VARCHAR)
    private String telefono; // Usar Integer si insistes en INT en BD

    @Column(name = "direccion", nullable = false) // VARCHAR(150) NOT NULL (Usando el tamaño ampliado que discutimos)
    private String direccion;

    @ManyToOne(fetch = FetchType.LAZY) // Relación Muchos a Uno: Muchos Clientes viven en una Ciudad
    @JoinColumn(name = "id_ciudad", nullable = false) // FK en la tabla 'clientes'
    private Ciudad ciudad; // Relación con la entidad Ciudad

    @OneToMany(mappedBy = "cliente") // Relación Uno a Muchos: Un Cliente puede tener muchas Facturas
    private List<Factura> facturas; // Lista de facturas asociadas a este cliente

    // Nota: Las restricciones UNIQUE KEY (tipo_identificacion, numero_identificacion) y (id_usuario)
    // se definen en la tabla en el DDL, pero JPA las entiende por @Column(unique=true) o @JoinColumn(unique=true)
    // y/o la configuración en el modelador/DLL.

    // Constructor para usar al crear un nuevo Cliente
    public Cliente(Usuario usuario, String tipoPersona, String tipoIdentificacion, Integer numeroIdentificacion, String nombresORazonSocial, String emailContacto, String telefono, String direccion, Ciudad ciudad) {
        this.usuario = usuario;
        this.tipoPersona = tipoPersona;
        this.tipoIdentificacion = tipoIdentificacion;
        this.numeroIdentificacion = numeroIdentificacion;
        this.nombresORazonSocial = nombresORazonSocial;
        this.telefono = telefono;
        this.direccion = direccion;
        this.ciudad = ciudad;
    }

    // Métodos para actualizar datos del cliente si es necesario, similar a Medico.actualizarDatos
    public void actualizarDatos(String nuevaDireccion, Ciudad nuevaCiudad, String nuevoTelefono, String nuevoEmailContacto) {
        if (nuevaDireccion != null && !nuevaDireccion.trim().isEmpty()) {
            this.direccion = nuevaDireccion;
        }
        if (nuevaCiudad != null) {
            this.ciudad = nuevaCiudad;
        }
        if (nuevoTelefono != null) { // Considerar validación de formato/unicidad si es necesario
            this.telefono = nuevoTelefono;
        }

        // Nota: No se deberían cambiar Tipo/Numero Identificacion ni Usuario asociado con un simple actualizar
    }
}
