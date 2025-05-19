package com.condorltda.tiendaonline.domain.movimientoInventario;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal; // Para cantidad

// Record para los datos de registro de una entrada de inventario
public record DatosRegistroEntradaInventario(

        @NotNull // El ID del producto no puede ser nulo
        Integer idProducto,

        @NotNull // La cantidad no puede ser nula
        @Positive // La cantidad debe ser mayor que cero
        BigDecimal cantidad, // Usar BigDecimal para la cantidad, como en la entidad

        @NotNull // El ID del proveedor no puede ser nulo
        Integer idProveedor,

        @NotBlank // La referencia (ej. número de factura proveedor) no puede estar vacía
        String referencia, // Usar String para la referencia, aunque en BD la tengas como INT

        @NotNull // El ID del usuario (personal) que registra no puede ser nulo
        Integer idUsuarioRegistro // El ID del empleado que realiza la operación
) {
}
