/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msventa.feign;
import com.example.msventa.dto.LibroDto;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@FeignClient(name="ms-book-service")
public interface LibroFeign {
 @GetMapping("/libros/{id}") ResponseEntity<LibroDto> listarLibro(@PathVariable Integer id);
 @PutMapping("/libros/{id}/stock") ResponseEntity<Void> actualizarStock(@PathVariable Integer id,@RequestParam Integer cantidad);
 record Item(Integer libroId,Integer cantidad,BigDecimal precioEsperado){}
 record Reserva(String operacionId,Integer userId,List<Item> items){}
 @PostMapping("/internal/stock/reservas") void reservar(@RequestBody Reserva reserva);
}
