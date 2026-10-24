/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msbook.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String codigo;
    private String titulo;
    private String autor;
    private String descripcion;
    private Integer stock;
    private Integer stockMinimo = 10;
    private Double precio;
    private LocalDate anio;
    private String imagenUrl;
    private String estado = "ACTIVO";

    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "provedores_id", nullable = false)
    private Provedores provedores;
}