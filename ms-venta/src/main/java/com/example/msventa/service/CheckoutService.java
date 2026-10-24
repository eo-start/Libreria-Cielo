/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msventa.service;
import com.example.msventa.dto.LibroDto;
import com.example.msventa.entity.*;
import com.example.msventa.feign.LibroFeign;
import com.example.msventa.repository.*;
import java.math.*;
import java.util.*;
import feign.FeignException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
@Service
public class CheckoutService {
 private final VentaRepository ventas;private final CarritoItemRepository carrito;private final LibroFeign book;private final TransactionTemplate tx;
 public CheckoutService(VentaRepository ventas,CarritoItemRepository carrito,LibroFeign book,PlatformTransactionManager manager){this.ventas=ventas;this.carrito=carrito;this.book=book;this.tx=new TransactionTemplate(manager);}
 public Venta checkout(Integer userId,String userName,String operation,String customer,String method,String reference,Integer customerId){
  try{UUID.fromString(operation);}catch(Exception e){throw bad("Idempotency-Key debe ser un UUID");}
  if(!Set.of("EFECTIVO","YAPE_PLIN","TRANSFERENCIA").contains(method))throw bad("Método de pago inválido");
  if(customer==null||customer.isBlank()||customer.length()>150)throw bad("Indica el nombre del cliente (máximo 150 caracteres)");
  if(reference!=null&&reference.length()>100)throw bad("Referencia de pago demasiado larga");
  if("TRANSFERENCIA".equals(method)&&(reference==null||reference.trim().length()<4))throw bad("Indica el número de operación de la transferencia");
  Venta sale=tx.execute(status->{
   var existing=ventas.findByOperacionId(operation);
   if(existing.isPresent()){
    Venta previous=existing.get();checkOwner(previous,userId);
    if(!Objects.equals(previous.getUserName(),customer)||!Objects.equals(previous.getMetodoPago(),method)||!Objects.equals(previous.getNroOperacion(),reference)||!Objects.equals(previous.getClienteId(),customerId))throw conflict("La misma operación no puede cambiar sus datos");
    return previous;
   }
   List<CarritoItem> items=carrito.findForCheckout(userId);
   if(ventas.findFirstByUserIdAndEstado(userId,"PENDIENTE").isPresent())throw conflict("Hay una venta pendiente. Reinténtala antes de crear otra");
   if(items.isEmpty())throw bad("El carrito está vacío");
   Venta v=new Venta();v.setOperacionId(operation);v.setEstado("PENDIENTE");v.setFecha(new Date());v.setUserId(userId);v.setUserName(customer);v.setMetodoPago(method);v.setNroOperacion(reference);v.setClienteId(customerId);
   Map<Integer,Integer> quantities=new TreeMap<>();
   for(CarritoItem i:items){if(i.getCantidad()==null||i.getCantidad()<=0)throw bad("Cantidad inválida");quantities.merge(i.getLibroId(),i.getCantidad(),Math::addExact);v.getCarritoIds().add(i.getId());}
   if(quantities.size()>100)throw bad("Máximo 100 productos por venta");
   BigDecimal total=BigDecimal.ZERO,discount=BigDecimal.ZERO;
   for(var entry:quantities.entrySet()){
    LibroDto l;try{l=book.listarLibro(entry.getKey()).getBody();}catch(FeignException e){throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"No se pudo consultar el catálogo; la venta no fue creada");}
    if(l==null||l.getPrecio()==null||l.getPrecio()<0)throw bad("Producto no disponible");
    BigDecimal original=BigDecimal.valueOf(l.getPrecio()).setScale(2,RoundingMode.HALF_UP);
    BigDecimal price=(entry.getValue()>=10?original.multiply(new BigDecimal("0.85")):original).setScale(2,RoundingMode.HALF_UP);
    VentaDetalle d=new VentaDetalle();d.setVenta(v);d.setLibroId(l.getId());d.setNombreProducto(l.getTitulo());d.setCantidad(entry.getValue());d.setPrecioOriginal(original.doubleValue());d.setPrecio(price.doubleValue());d.setLibro(l);v.getDetalles().add(d);
    total=total.add(price.multiply(BigDecimal.valueOf(entry.getValue())));discount=discount.add(original.subtract(price).multiply(BigDecimal.valueOf(entry.getValue())));
   }
   BigDecimal igv=total.multiply(new BigDecimal("0.18")).setScale(2,RoundingMode.HALF_UP);
   v.setTotal(total.doubleValue());v.setIgv(igv.doubleValue());v.setTotalConIgv(total.add(igv).doubleValue());v.setDescuentoTotal(discount.doubleValue());v.setEsMayorista(discount.signum()>0);
   return ventas.saveAndFlush(v);
  });
  return finish(sale,userId);
 }
 public Venta retry(Integer id,Integer userId){Venta sale=ventas.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));checkOwner(sale,userId);return finish(sale,userId);}
 public Venta pending(Integer userId){return ventas.findFirstByUserIdAndEstado(userId,"PENDIENTE").orElse(null);}
 private Venta finish(Venta sale,Integer userId){
  if("COMPLETADA".equals(sale.getEstado()))return sale;
  if(!"PENDIENTE".equals(sale.getEstado()))throw conflict("La venta fue cancelada; revisa el carrito");
  var items=sale.getDetalles().stream().map(d->new LibroFeign.Item(d.getLibroId(),d.getCantidad(),BigDecimal.valueOf(d.getPrecioOriginal()))).toList();
  try{book.reservar(new LibroFeign.Reserva(sale.getOperacionId(),userId,items));}
  catch(FeignException e){
   if(e.status()==400||e.status()==409){tx.executeWithoutResult(t->{Venta v=ventas.findOperationForUpdate(sale.getOperacionId()).orElseThrow();if("PENDIENTE".equals(v.getEstado())){v.setEstado("CANCELADA");ventas.save(v);}});throw conflict("Stock insuficiente, producto inactivo o precio cambiado. Revisa el carrito");}
   throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Venta pendiente de confirmación. Reintenta la misma venta; no se descontará stock dos veces");
  }
  return tx.execute(t->{
   Venta v=ventas.findOperationForUpdate(sale.getOperacionId()).orElseThrow();checkOwner(v,userId);
   if("COMPLETADA".equals(v.getEstado()))return v;
   if(!"PENDIENTE".equals(v.getEstado()))throw conflict("La venta no está pendiente");
   v.setEstado("COMPLETADA");ventas.saveAndFlush(v);
   var consumed=carrito.findAllById(v.getCarritoIds()).stream().filter(i->Objects.equals(i.getUserId(),userId)).toList();carrito.deleteAll(consumed);
   return v;
  });
 }
 private void checkOwner(Venta v,Integer id){if(!Objects.equals(v.getUserId(),id))throw new ResponseStatusException(HttpStatus.FORBIDDEN);}
 private ResponseStatusException bad(String m){return new ResponseStatusException(HttpStatus.BAD_REQUEST,m);}
 private ResponseStatusException conflict(String m){return new ResponseStatusException(HttpStatus.CONFLICT,m);}
}
