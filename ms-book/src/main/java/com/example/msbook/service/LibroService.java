/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msbook.service;

import com.example.msbook.entity.Libro;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LibroService {
    List<Libro> listar();
    Libro guardar(String titulo, String autor, Integer stock, Integer stockMinimo, Double precio,
                  Integer categoriaId, Integer provedoresId, MultipartFile file, LocalDate anio);
    Libro editar(Integer id, String titulo, String autor, Integer stock, Integer stockMinimo, Double precio,
                 Integer categoriaId, Integer provedoresId, MultipartFile file, LocalDate anio, String estado);
    void eliminar(Integer id);
    Optional<Libro> listarPorId(Integer id);
    Libro actualizarStock(Integer id, Integer cantidad);
    Libro actualizarStockMovimiento(Integer id, Integer cantidad, String tipo);
    List<Libro> listarPorCategoria(Integer categoriaId);
    List<Libro> listarPorAnio(LocalDate anio);
    List<Libro> listarDesdeAnio(LocalDate anio);
}
