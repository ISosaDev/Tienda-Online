package com.condorltda.tiendaonline.domain.detalleFactura;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetalleFacturaRepository extends JpaRepository<DetalleFactura, Integer> {

    // Métodos CRUD básicos automáticos.

    // Ejemplo: buscar detalles por factura
    // List<DetalleFactura> findByFactura(Factura factura);
    // List<DetalleFactura> findByFacturaId(Integer facturaId);
}
