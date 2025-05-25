package com.condorltda.tiendaonline.controller;

import com.condorltda.tiendaonline.domain.factura.Factura;
import com.condorltda.tiendaonline.domain.factura.FacturaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/facturas") // Endpoint para listar facturas
public class FacturaController {
    @Autowired
    private FacturaRepository facturaRepository;

    @GetMapping
    public ResponseEntity<List<Factura>> listarTodasLasFacturas() {
        List<Factura> facturas = facturaRepository.findAll();
        return ResponseEntity.ok(facturas);
    }
}
