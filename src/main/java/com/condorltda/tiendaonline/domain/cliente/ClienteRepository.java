package com.condorltda.tiendaonline.domain.cliente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    // Métodos CRUD básicos automáticos.

    // Ejemplo: buscar un cliente por su relación con el usuario (relación 1:1)
    // Optional<Cliente> findByUsuarioId(Integer usuarioId); // Usar Optional
    // Ejemplo: buscar un cliente por su tipo y número de identificación (UNIQUE KEY)
    // Optional<Cliente> findByTipoIdentificacionAndNumeroIdentificacion(String tipo, Integer numero); // Usar Optional
}
