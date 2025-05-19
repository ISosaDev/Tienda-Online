package com.condorltda.tiendaonline.domain.rol;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {

    // Métodos CRUD básicos automáticos.

    // Ejemplo: buscar un rol por su nombre (útil para login/registro)
    // Optional<Rol> findByNombreRol(String nombreRol); // Usar Optional para manejar si no se encuentra
}
