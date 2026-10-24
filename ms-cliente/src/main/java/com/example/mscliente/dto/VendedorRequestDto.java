/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.mscliente.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "DTO utilizado para registrar o actualizar vendedores.")
public class VendedorRequestDto {

    @NotBlank(message = "El nombre del vendedor es obligatorio.")
    @Size(max = 100, message = "El nombre no debe superar los 100 caracteres.")
    @Schema(description = "Nombre completo del vendedor", example = "María Quispe")
    private String nombre;

    @NotBlank(message = "El DNI del vendedor es obligatorio.")
    @Size(min = 8, max = 8, message = "El DNI debe tener 8 dígitos.")
    @Schema(description = "Documento Nacional de Identidad del vendedor", example = "71234567")
    private String dni;

    @Size(max = 15, message = "El teléfono no debe superar los 15 caracteres.")
    @Schema(description = "Número telefónico del vendedor", example = "987123456")
    private String telefono;

    @Email(message = "El correo debe tener un formato válido.")
    @Schema(description = "Correo electrónico del vendedor", example = "vendedor@libreria.com")
    private String correo;

    @Size(max = 150, message = "La dirección no debe superar los 150 caracteres.")
    @Schema(description = "Dirección del vendedor", example = "Av. Circunvalación 456 - Juliaca")
    private String direccion;
}