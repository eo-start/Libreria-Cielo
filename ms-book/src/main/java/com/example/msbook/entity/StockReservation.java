/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msbook.entity;
import jakarta.persistence.*;
import lombok.Data;
@Entity @Data
public class StockReservation {
 @Id @Column(length=36) private String id;
 private Integer userId;
 @Column(length=4000,nullable=false) private String payload;
}
