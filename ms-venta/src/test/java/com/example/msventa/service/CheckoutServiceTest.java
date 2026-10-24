/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msventa.service;
import com.example.msventa.dto.LibroDto;
import com.example.msventa.entity.*;
import com.example.msventa.feign.LibroFeign;
import com.example.msventa.repository.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@DataJpaTest @Import(CheckoutService.class)
@Transactional(propagation=Propagation.NOT_SUPPORTED)
class CheckoutServiceTest {
 @Autowired CheckoutService checkout;@Autowired VentaRepository ventas;@Autowired CarritoItemRepository carrito;
 @MockBean LibroFeign book;
 @BeforeEach void init(){ventas.deleteAll();carrito.deleteAll();reset(book);
  var item=new CarritoItem();item.setUserId(7);item.setLibroId(1);item.setCantidad(2);carrito.save(item);
  var libro=new LibroDto();libro.setId(1);libro.setTitulo("Producto");libro.setPrecio(10.0);libro.setStock(10);when(book.listarLibro(1)).thenReturn(ResponseEntity.ok(libro));
 }
 Venta buy(String key){return checkout.checkout(7,"cajero",key,"Cliente","EFECTIVO",null,null);}
 @Test void sameKeyCreatesOnlyOneSaleAndKeepsPayment(){
  String key=UUID.randomUUID().toString();Venta a=buy(key),b=buy(key);
  assertEquals(a.getId(),b.getId());assertEquals(1,ventas.count());assertEquals("COMPLETADA",b.getEstado());assertEquals(23.6,b.getTotalConIgv(),0.001);assertEquals(0,carrito.count());verify(book,times(1)).reservar(any());
 }
 @Test void ambiguousNetworkFailureKeepsDurablePendingSaleAndRetryDoesNotCreateAnother(){
  doThrow(feign.FeignException.errorStatus("reserve",feign.Response.builder().status(503).reason("unavailable").request(feign.Request.create(feign.Request.HttpMethod.POST,"http://book",Map.of(),null,java.nio.charset.StandardCharsets.UTF_8,null)).build())).doNothing().when(book).reservar(any());
  String key=UUID.randomUUID().toString();assertThrows(ResponseStatusException.class,()->buy(key));
  Venta pending=checkout.pending(7);assertNotNull(pending);assertEquals(1,carrito.count());
  Venta completed=checkout.retry(pending.getId(),7);assertEquals("COMPLETADA",completed.getEstado());assertEquals(1,ventas.count());assertEquals(0,carrito.count());
 }
 @Test void stockConflictCancelsSaleWithoutLosingCart(){
  doThrow(feign.FeignException.errorStatus("reserve",feign.Response.builder().status(409).reason("stock").request(feign.Request.create(feign.Request.HttpMethod.POST,"http://book",Map.of(),null,java.nio.charset.StandardCharsets.UTF_8,null)).build())).when(book).reservar(any());
  assertThrows(ResponseStatusException.class,()->buy(UUID.randomUUID().toString()));assertEquals("CANCELADA",ventas.findAll().get(0).getEstado());assertEquals(1,carrito.count());
 }
 @Test void anotherUserCannotRetryOrReuseSale(){String key=UUID.randomUUID().toString();Venta sale=buy(key);assertThrows(ResponseStatusException.class,()->checkout.retry(sale.getId(),8));assertThrows(ResponseStatusException.class,()->checkout.checkout(8,"other",key,"Cliente","EFECTIVO",null,null));}
 @Test void retryCannotChangePayment(){String key=UUID.randomUUID().toString();buy(key);assertThrows(ResponseStatusException.class,()->checkout.checkout(7,"cajero",key,"Cliente","YAPE_PLIN",null,null));}
 @Test void rejectsInvalidOperationWithoutWriting(){assertThrows(ResponseStatusException.class,()->buy("invalid"));assertEquals(0,ventas.count());verify(book,never()).reservar(any());}
}
