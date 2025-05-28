package com.condorltda.tiendaonline.controller;

import com.condorltda.tiendaonline.domain.factura.Factura;
import com.condorltda.tiendaonline.domain.factura.FacturaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/facturas") // Endpoint para listar facturas
public class FacturaController {
    @Autowired
    private FacturaRepository facturaRepository;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listarFacturasSimplificadas() {
        List<Factura> facturas = facturaRepository.findAll();
        List<Map<String, Object>> resultado = facturas.stream().map(f -> {
            Map<String, Object> dto = new HashMap<>();
            dto.put("id", f.getId());
            dto.put("fecha", f.getFecha());
            dto.put("valorTotalFactura", f.getValorTotalFactura());
            dto.put("metodoPago", f.getMetodoPago());
            dto.put("estadoFactura", f.getEstadoFactura().toString());
            return dto;
        }).toList();
        return ResponseEntity.ok(resultado);
    }

}
