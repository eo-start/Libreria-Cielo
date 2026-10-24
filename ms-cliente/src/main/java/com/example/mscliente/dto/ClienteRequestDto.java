/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.mscliente.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "DTO para registrar o actualizar clientes.")
public class ClienteRequestDto {

    @NotBlank(message = "El nombre del cliente es obligatorio.")
    @Size(max = 100, message = "El nombre no debe superar los 100 caracteres.")
    @Schema(description = "Nombre completo del cliente", example = "Juan Pérez García")
    private String nombreCompleto;

    @NotBlank(message = "El DNI del cliente es obligatorio.")
    @Size(min = 8, max = 8, message = "El DNI debe tener 8 dígitos.")
    @Schema(description = "DNI del cliente", example = "70894512")
    private String dni;

    @Size(max = 15, message = "El teléfono no debe superar los 15 caracteres.")
    @Schema(description = "Teléfono del cliente", example = "987654321")
    private String telefono;

    @Size(max = 150, message = "La dirección no debe superar los 150 caracteres.")
    @Schema(description = "Dirección del cliente", example = "Jr. Lima 123 - Juliaca")
    private String direccion;
}
