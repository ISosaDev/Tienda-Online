package com.condorltda.tiendaonline.controller;



import com.condorltda.tiendaonline.domain.movimientoInventario.DatosRegistroEntradaInventario;
import com.condorltda.tiendaonline.service.InventarioService;
import jakarta.validation.Valid; // Para activar la validación
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity; // Para construir la respuesta
import org.springframework.web.bind.annotation.PostMapping; // Para manejar solicitudes POST
import org.springframework.web.bind.annotation.RequestBody; // Para obtener datos del cuerpo
import org.springframework.web.bind.annotation.RequestMapping; // Para mapear la URL base
import org.springframework.web.bind.annotation.RestController; // Indica que es un controlador REST


@RestController // Marca la clase como un controlador REST
@RequestMapping("/api/inventario") // Mapea a /api/inventario
public class InventarioController {

    // Inyecta el servicio de inventario donde está la lógica transaccional de entrada
    @Autowired
    private InventarioService inventarioService;

    // Endpoint para manejar solicitudes POST a /api/inventario/entrada
    @PostMapping("/entrada") // Mapea a /api/inventario/entrada
    public ResponseEntity<Void> registrarEntradaInventario(
            @RequestBody @Valid DatosRegistroEntradaInventario datosEntrada // Obtiene el cuerpo y lo valida
    ) {
        // Llama al método transaccional en el servicio para registrar la entrada
        // Las excepciones serán capturadas por el TratadorDeErrores
        inventarioService.registrarEntradaInventario(datosEntrada);

        // Si el servicio no lanzó una excepción, la transacción fue exitosa (COMMIT)

        // Devuelve una respuesta HTTP 204 No Content para indicar que la operación fue exitosa pero no hay cuerpo de respuesta
        return ResponseEntity.noContent().build();
        // Alternativamente, puedes devolver ResponseEntity.ok().build() para un 200 OK
    }

    // Podrías añadir otros endpoints aquí, como:
    // - @GetMapping("/stock/{productoId}") para ver el stock de un producto (necesitaría un método en el servicio/repository)
    // - @GetMapping("/movimientos") para listar movimientos
    // - etc.
}
