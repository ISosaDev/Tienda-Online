package com.condorltda.tiendaonline.domain.producto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    // Métodos CRUD básicos automáticos.

    // Puedes añadir métodos de consulta para buscar productos (ej. por nombre o marca)
    // List<Producto> findByNombreContaining(String nombre);
    // List<Producto> findByMarca(String marca);
}
