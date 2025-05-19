package com.condorltda.tiendaonline.domain.usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // Métodos CRUD básicos automáticos.

    // Ejemplo: buscar un usuario por su email (clave para login)
    // Optional<Usuario> findByEmail(String email); // Usar Optional
}
