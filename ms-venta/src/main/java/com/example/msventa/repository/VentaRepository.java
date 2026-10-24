/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msventa.repository;

import com.example.msventa.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VentaRepository extends JpaRepository<Venta, Integer> {
    java.util.Optional<Venta> findByOperacionId(String operacionId);
    java.util.Optional<Venta> findFirstByUserIdAndEstado(Integer userId,String estado);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select v from Venta v where v.operacionId = :operation")
    java.util.Optional<Venta> findOperationForUpdate(@org.springframework.data.repository.query.Param("operation") String operation);

}
