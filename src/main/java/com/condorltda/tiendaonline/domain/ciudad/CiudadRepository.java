package com.condorltda.tiendaonline.domain.ciudad;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CiudadRepository extends JpaRepository<Ciudad, Integer> {

    // Métodos CRUD básicos automáticos.

    // Ejemplo de método personalizado si necesitas buscar ciudades por departamento:
    // List<Ciudad> findByDepartamento(Departamento departamento);
    // List<Ciudad> findByDepartamentoId(Integer departamentoId);
}
