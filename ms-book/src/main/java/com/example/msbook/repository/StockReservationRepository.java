/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msbook.repository;
import com.example.msbook.entity.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;
public interface StockReservationRepository extends JpaRepository<StockReservation,String>{}
