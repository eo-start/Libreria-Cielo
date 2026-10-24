/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msbook.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer libroId;
    private String nombreProducto;
    private String tipo;
    private Integer cantidad;
    private String notas;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;

    private String usuario;
}
