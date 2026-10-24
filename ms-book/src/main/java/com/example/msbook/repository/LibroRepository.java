/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msbook.repository;

import com.example.msbook.entity.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Integer> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select l from Libro l where l.id = :id")
    java.util.Optional<Libro> findForUpdate(@org.springframework.data.repository.query.Param("id") Integer id);


    List<Libro> findByCategoriaId(Integer categoriaId);

    List<Libro> findByAnio(LocalDate anio);

    List<Libro> findByAnioAfter(LocalDate anio);
}