package com.condorltda.tiendaonline.domain.proveedor;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProveedorRepository extends JpaRepository<Proveedor, Integer> {

    // Métodos CRUD básicos automáticos.

    // Ejemplo: buscar un proveedor por su tipo y número de identificación (UNIQUE KEY)
    // Optional<Proveedor> findByTipoIdentificacionAndNumeroIdentificacion(String tipo, Integer numero); // Usar Optional
}
