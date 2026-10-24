/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msbook.service;
import com.example.msbook.entity.Libro;
import com.example.msbook.entity.StockReservation;
import com.example.msbook.repository.LibroRepository;
import com.example.msbook.repository.StockReservationRepository;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
@Service
public class StockReservationService {
 public record Item(Integer libroId,Integer cantidad,BigDecimal precioEsperado){}
 public record Request(String operacionId,Integer userId,List<Item> items){}
 private final LibroRepository libros; private final StockReservationRepository reservas;
 public StockReservationService(LibroRepository libros,StockReservationRepository reservas){this.libros=libros;this.reservas=reservas;}
 @Transactional
 public void reserve(Request request,Integer callerId){
  try{UUID.fromString(request.operacionId());}catch(Exception e){throw invalid("Identificador de operación inválido");}
  if(!Objects.equals(callerId,request.userId())||request.items()==null||request.items().isEmpty()||request.items().size()>100)
   throw invalid("Reserva inválida");
  List<Item> items=new ArrayList<>(request.items());
  for(Item i:items)if(i.libroId()==null||i.cantidad()==null||i.cantidad()<=0||i.precioEsperado()==null||i.precioEsperado().signum()<0)throw invalid("Cantidad o precio inválido");
  items.sort(Comparator.comparing(Item::libroId));
  if(items.stream().map(Item::libroId).distinct().count()!=items.size())throw invalid("Productos duplicados en la reserva");
  String payload=items.stream().map(i->i.libroId()+":"+i.cantidad()+":"+i.precioEsperado().stripTrailingZeros().toPlainString()).reduce((a,b)->a+";"+b).orElseThrow();
  var previous=reservas.findById(request.operacionId());
  if(previous.isPresent()){
   if(!Objects.equals(previous.get().getUserId(),callerId)||!previous.get().getPayload().equals(payload))throw conflict("La operación ya existe con otros datos");
   return;
  }
  // The unique operation is persisted in the same transaction as every stock change.
  StockReservation reservation=new StockReservation();reservation.setId(request.operacionId());reservation.setUserId(callerId);reservation.setPayload(payload);
  reservas.saveAndFlush(reservation);
  for(Item item:items){
   Libro libro=libros.findForUpdate(item.libroId()).orElseThrow(()->conflict("El producto ya no existe"));
   if(!"ACTIVO".equals(libro.getEstado())||libro.getStock()==null||libro.getStock()<item.cantidad())throw conflict("Stock insuficiente para "+libro.getTitulo());
   if(libro.getPrecio()==null||BigDecimal.valueOf(libro.getPrecio()).compareTo(item.precioEsperado())!=0)throw conflict("El precio cambió; vuelve a revisar el carrito");
   libro.setStock(libro.getStock()-item.cantidad());libros.save(libro);
  }
 }
 private ResponseStatusException invalid(String m){return new ResponseStatusException(HttpStatus.BAD_REQUEST,m);}
 private ResponseStatusException conflict(String m){return new ResponseStatusException(HttpStatus.CONFLICT,m);}
}
