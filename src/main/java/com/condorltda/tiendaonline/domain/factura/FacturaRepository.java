package com.condorltda.tiendaonline.domain.factura;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface FacturaRepository extends JpaRepository<Factura, Integer> {

    // Métodos CRUD básicos automáticos.

    // Ejemplo: buscar facturas por cliente
    // List<Factura> findByCliente(Cliente cliente);
    // List<Factura> findByClienteId(Integer clienteId);
    // Ejemplo: buscar facturas por rango de fecha
    // List<Factura> findByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin);
}
