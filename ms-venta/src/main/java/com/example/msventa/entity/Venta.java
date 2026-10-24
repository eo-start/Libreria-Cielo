/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msventa.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Venta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;

    private Integer userId;
    private double total;
    private double igv;
    private double totalConIgv;
    private boolean esMayorista;
    private double descuentoTotal;
    private String userName;
    @Column(unique=true,length=36)
    private String operacionId;
    private String estado = "COMPLETADA";
    private String metodoPago = "EFECTIVO";
    private String nroOperacion;
    private Integer clienteId;
    @ElementCollection(fetch=FetchType.EAGER)
    private java.util.Set<Integer> carritoIds = new java.util.LinkedHashSet<>();


    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<VentaDetalle> detalles = new ArrayList<>();
}