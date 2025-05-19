package com.condorltda.tiendaonline.controller;



import com.condorltda.tiendaonline.domain.pedido.DatosRegistroPedido;
import com.condorltda.tiendaonline.domain.pedido.DatosRespuestaPedido;
import com.condorltda.tiendaonline.service.PedidoService;
import jakarta.validation.Valid; // Para activar la validación de los DTOs de entrada
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity; // Para construir la respuesta HTTP
import org.springframework.web.bind.annotation.PostMapping; // Para manejar solicitudes POST
import org.springframework.web.bind.annotation.RequestBody; // Para obtener datos del cuerpo de la solicitud
import org.springframework.web.bind.annotation.RequestMapping; // Para mapear la URL base del controlador
import org.springframework.web.bind.annotation.RestController; // Indica que es un controlador REST
import org.springframework.web.util.UriComponentsBuilder; // Para construir la URL de la respuesta 201 Created

import java.net.URI; // Para el tipo de dato URI


@RestController // Marca la clase como un controlador REST
@RequestMapping("/api/pedidos") // Mapea todas las solicitudes a /api/pedidos a este controlador
public class PedidoController {

    // Inyecta el servicio de pedidos donde está la lógica transaccional
    @Autowired
    private PedidoService pedidoService;

    // Endpoint para manejar solicitudes POST a /api/pedidos
    @PostMapping // Maneja solicitudes POST
    public ResponseEntity<DatosRespuestaPedido> registrarPedido(
            @RequestBody @Valid DatosRegistroPedido datosPedido, // Obtiene el cuerpo de la solicitud y lo valida
            UriComponentsBuilder uriComponentsBuilder // Utilidad para construir URLs
    ) {
        // Llama al método transaccional en el servicio para procesar el pedido
        // El servicio lanzará excepciones (ej. StockInsuficienteException) que serán capturadas por el TratadorDeErrores
        DatosRespuestaPedido respuesta = pedidoService.procesarPedido(datosPedido);

        // Si el servicio no lanzó una excepción, la transacción fue exitosa (COMMIT)

        // Construye la URL para el recurso creado (la nueva factura)
        // Opcional, pero es buena práctica devolver 201 Created con la ubicación del nuevo recurso
        URI url = uriComponentsBuilder.path("/api/pedidos/{id}").buildAndExpand(respuesta.idFactura()).toUri();

        // Devuelve una respuesta HTTP 201 Created con el cuerpo de la respuesta (los detalles del pedido/factura)
        return ResponseEntity.created(url).body(respuesta);
    }

    // Podrías añadir otros endpoints aquí, como:
    // - @GetMapping("/{id}") para ver detalles de un pedido/factura
    // - @GetMapping para listar pedidos de un cliente
    // - etc.
}
