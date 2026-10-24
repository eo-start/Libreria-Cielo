/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msauth.feign;

import com.example.msauth.dto.ClienteDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-cliente-service")
public interface ClienteFeign {
    @GetMapping(value = "/cliente/{Id}")
    ResponseEntity<ClienteDto> ListbyId(@PathVariable(required = true) Integer Id);


}
