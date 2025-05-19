package com.condorltda.tiendaonline.domain.usuario;


import com.condorltda.tiendaonline.domain.rol.Rol;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter; // Añadido Setter si los datos del usuario pueden actualizarse

import java.time.LocalDateTime; // Para fecha_registro


@Table(name = "usuarios") // Nombre de la tabla en la BD
@Entity(name = "Usuario") // Nombre de la entidad
@Getter // Genera getters
@Setter // Genera setters si es necesario
@NoArgsConstructor // Genera constructor sin argumentos
@AllArgsConstructor // Genera constructor con todos los argumentos
public class Usuario {

    @Id // PK
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Autoincremento en MySQL
    @Column(name = "id_usuario")
    private Integer id; // Tipo de dato Java que mapea a SMALLINT AUTO_INCREMENT en MySQL

    @Column(name = "email", nullable = false, unique = true) // VARCHAR(100) NOT NULL UNIQUE
    private String email;

    @Column(name = "contraseña", nullable = false) // VARCHAR(30) NOT NULL - Nota: Debería ser VARCHAR(aprox 60-255) para contraseñas hasheadas
    private String contrasena; // Atributo en Java (contraseña en BD)

    @Column(name = "fecha_registro") // DATETIME en MySQL (Si añadiste DEFAULT CURRENT_TIMESTAMP, la BD lo maneja)
    private LocalDateTime fechaRegistro; // Usar LocalDateTime para DATETIME

    @Column(name = "activo", nullable = false) // INT NOT NULL (o BOOLEAN) en MySQL
    private Boolean activo; // Usar Boolean para mapear a INT 0/1 o BOOLEAN

    @ManyToOne(fetch = FetchType.LAZY) // Relación Muchos a Uno: Muchos Usuarios pertenecen a un Rol
    @JoinColumn(name = "id_rol", nullable = false) // Columna FK en la tabla 'usuarios'
    private Rol rol; // Relación con la entidad Rol

    // Relación Uno a Uno con Cliente (Opcional, depende de cómo la modeles en Entity)
    // Si usas la Opción 2 con FK en Clientes, la relación principal es desde Cliente a Usuario.
    // Si modelaste 1:1 con FK única en Clientes, podrías tener:
    // @OneToOne(mappedBy = "usuario") // 'usuario' es el nombre del atributo en la entidad Cliente
    // private Cliente cliente;

    // Relación Uno a Muchos con Facturas (Un usuario/cliente puede tener muchas facturas)
    // Nota: Esta relación es conceptualmente de Usuario a Factura si decides asociar Facturas a Usuarios directamente.
    // Si Facturas solo tiene FK a Clientes, la relación va de Cliente a Factura.
    // Basado en tu último DDL que relaciona Facturas a Clientes (clientes_id_cliente), la relación @OneToMany iría en la entidad Cliente.
    // Si prefieres relacionar Facturas directamente con Usuario:
    // @OneToMany(mappedBy = "usuario") // 'usuario' es el atributo FK en la entidad Factura si la modificas
    // private List<Factura> facturas;

    // Constructor para usar al crear un nuevo Usuario
    public Usuario(String email, String contrasena, Boolean activo, Rol rol) {
        this.email = email;
        this.contrasena = contrasena;
        this.activo = activo;
        this.rol = rol;
        // fechaRegistro se auto-asigna en BD si configuraste DEFAULT CURRENT_TIMESTAMP
    }
}

