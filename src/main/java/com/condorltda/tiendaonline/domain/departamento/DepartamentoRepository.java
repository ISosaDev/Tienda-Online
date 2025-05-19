package com.condorltda.tiendaonline.domain.departamento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartamentoRepository extends JpaRepository<Departamento, Integer> {

    // Spring Data JPA proveerá automáticamente métodos CRUD como:
    // save(Departamento departamento)
    // findById(Integer id)
    // findAll()
    // deleteById(Integer id)
    // ... y muchos más.

    // Puedes añadir métodos de consulta personalizados aquí si los necesitas,
    // siguiendo las convenciones de nombres de Spring Data JPA (ej. findByNombre(String nombre))
    // o usando la anotación @Query para JPQL o SQL nativo.
}
