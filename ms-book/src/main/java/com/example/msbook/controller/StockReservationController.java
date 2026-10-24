/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msbook.controller;
import com.example.msbook.service.StockReservationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/internal/stock")
public class StockReservationController {
 private final StockReservationService service;
 public StockReservationController(StockReservationService service){this.service=service;}
 @PostMapping("/reservas") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
 public void reserve(@RequestBody StockReservationService.Request request,@AuthenticationPrincipal Jwt jwt){
 Integer userId=((Number)jwt.getClaim("id")).intValue();
 try{service.reserve(request,userId);}catch(org.springframework.dao.DataIntegrityViolationException concurrent){service.reserve(request,userId);}
}
}
