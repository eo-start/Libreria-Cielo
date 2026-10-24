/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msbook.service;
import com.example.msbook.entity.*;
import com.example.msbook.repository.*;
import java.util.*;
import java.math.BigDecimal;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
@DataJpaTest @Import(StockReservationService.class)
@Transactional(propagation=Propagation.NOT_SUPPORTED)
class StockReservationServiceTest {
 @Autowired StockReservationService service;@Autowired LibroRepository libros;@Autowired CategoriaRepository categorias;@Autowired ProvedoresRepository proveedores;@Autowired StockReservationRepository reservas;
 Integer id,other;
 @BeforeEach void init(){reservas.deleteAll();libros.deleteAll();categorias.deleteAll();proveedores.deleteAll();var c=new Categoria();c.setNombre("Test");c=categorias.save(c);var p=new Provedores();p.setNombre("Test");p=proveedores.save(p);
  var a=new Libro();a.setTitulo("A");a.setStock(1);a.setPrecio(10.0);a.setCategoria(c);a.setProvedores(p);id=libros.save(a).getId();
  var b=new Libro();b.setTitulo("B");b.setStock(0);b.setPrecio(10.0);b.setCategoria(c);b.setProvedores(p);other=libros.save(b).getId();
 }
 StockReservationService.Item item(Integer i){return new StockReservationService.Item(i,1,new BigDecimal("10.00"));}
 StockReservationService.Request request(String key){return new StockReservationService.Request(key,7,List.of(item(id)));}
 @Test void duplicateRequestDoesNotDecrementTwice(){var r=request(UUID.randomUUID().toString());service.reserve(r,7);service.reserve(r,7);assertEquals(0,libros.findById(id).orElseThrow().getStock());assertEquals(1,reservas.count());}
 @Test void batchRollsBackEveryProductWhenOneHasNoStock(){var r=new StockReservationService.Request(UUID.randomUUID().toString(),7,List.of(item(id),item(other)));assertThrows(ResponseStatusException.class,()->service.reserve(r,7));assertEquals(1,libros.findById(id).orElseThrow().getStock());assertEquals(0,reservas.count());}
 @Test void twoBuyersCannotConsumeTheSameLastUnit() throws Exception {
  var start=new CountDownLatch(1);var pool=Executors.newFixedThreadPool(2);
  try{
   Callable<Boolean> attempt=()->{start.await();try{service.reserve(request(UUID.randomUUID().toString()),7);return true;}catch(ResponseStatusException e){return false;}};
   var a=pool.submit(attempt);var b=pool.submit(attempt);start.countDown();assertNotEquals(a.get(15,TimeUnit.SECONDS),b.get(15,TimeUnit.SECONDS));assertEquals(0,libros.findById(id).orElseThrow().getStock());assertEquals(1,reservas.count());
  }finally{pool.shutdownNow();}
 }
 @Test void rejectedPriceDoesNotConsumeStock(){var r=new StockReservationService.Request(UUID.randomUUID().toString(),7,List.of(new StockReservationService.Item(id,1,BigDecimal.ONE)));assertThrows(ResponseStatusException.class,()->service.reserve(r,7));assertEquals(1,libros.findById(id).orElseThrow().getStock());}
}
