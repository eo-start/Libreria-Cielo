/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msauth.config;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
@Configuration
public class FeignSecurityConfig {
 @Bean RequestInterceptor bearerTokenRelay(){return template->{
  if(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs){
   String token=attrs.getRequest().getHeader("Authorization");
   if(token!=null)template.header("Authorization",token);
  }
 };}
}
