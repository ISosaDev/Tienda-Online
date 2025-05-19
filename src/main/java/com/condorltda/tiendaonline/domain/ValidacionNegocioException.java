package com.condorltda.tiendaonline.domain;


// Excepción para representar errores de validación de negocio
// Extiende RuntimeException para que @Transactional la maneje automáticamente con ROLLBACK
public class ValidacionNegocioException extends RuntimeException{
    public ValidacionNegocioException(String message) {
        super(message);
    }
}
