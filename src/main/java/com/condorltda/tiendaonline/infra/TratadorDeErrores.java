package com.condorltda.tiendaonline.infra;



import com.condorltda.tiendaonline.domain.StockInsuficienteException;
import com.condorltda.tiendaonline.domain.ValidacionNegocioException;
import jakarta.persistence.EntityNotFoundException; // Excepción común de JPA cuando no se encuentra una entidad

import org.springframework.http.HttpStatus; // Para códigos de estado HTTP
import org.springframework.http.ResponseEntity; // Para construir respuestas HTTP
import org.springframework.validation.FieldError; // Para manejar errores de validación de campos
import org.springframework.web.bind.MethodArgumentNotValidException; // Excepción de validación de @Valid
import org.springframework.web.bind.annotation.ExceptionHandler; // Anotación para manejar excepciones específicas
import org.springframework.web.bind.annotation.RestControllerAdvice; // Anotación para manejo global de excepciones

import java.util.List;

// Indica a Spring que esta clase es un manejador global de excepciones para controladores REST
@RestControllerAdvice
public class TratadorDeErrores {


    // Maneja la excepción cuando una entidad referenciada no se encuentra (ej. al buscar por ID inexistente)
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Void> tratarError404() {
        // Devuelve una respuesta HTTP 404 Not Found sin cuerpo
        return ResponseEntity.notFound().build();
    }

    // Maneja la excepción cuando la validación de los argumentos de un método del controlador falla (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<DatosErrorValidacion>> tratarError400(MethodArgumentNotValidException e) {
        // Obtiene la lista de errores de validación de campo
        var errores = e.getFieldErrors().stream()
                .map(DatosErrorValidacion::new) // Mapea cada FieldError a nuestro DTO de error
                .toList(); // Recopila en una lista

        // Devuelve una respuesta HTTP 400 Bad Request con una lista de los errores de validación
        return ResponseEntity.badRequest().body(errores);
    }

    // Maneja tu excepción general de validación de negocio
    @ExceptionHandler(ValidacionNegocioException.class)
    public ResponseEntity<String> tratarErrorDeValidacionNegocio(ValidacionNegocioException e) {
        // Devuelve una respuesta HTTP 400 Bad Request con el mensaje de la excepción como cuerpo
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    // Maneja tu excepción específica de stock insuficiente
    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<String> tratarErrorStockInsuficiente(StockInsuficienteException e) {
        // Podrías devolver un status code específico como 409 Conflict si es apropiado,
        // o seguir usando 400 Bad Request. Aquí usamos 400 por simplicidad.
        return ResponseEntity.badRequest().body(e.getMessage());
        // Si necesitas devolver más detalles (como el ID del producto sin stock),
        // podrías crear un DTO de respuesta específico para este error.
    }


    // Opcional: Manejar otras excepciones genéricas si es necesario
    /*
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> tratarErrorGenerico(Exception e) {
        // Para cualquier otra excepción no manejada específicamente
        // Devuelve un 500 Internal Server Error
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor: " + e.getMessage());
    }
    */


    // Record interno para formatear los errores de validación de campo
    // Mapea de FieldError a una estructura simple (campo, error)
    private record DatosErrorValidacion(String campo, String error) {
        public DatosErrorValidacion(FieldError error) {
            this(error.getField(), error.getDefaultMessage()); // Obtiene el nombre del campo y el mensaje de error por defecto
        }
    }
}
