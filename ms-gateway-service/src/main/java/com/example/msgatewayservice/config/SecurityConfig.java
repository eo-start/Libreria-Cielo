/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msgatewayservice.config;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import reactor.core.publisher.Flux;
@Configuration
public class SecurityConfig {
 @Bean ReactiveJwtDecoder jwtDecoder(@Value("${jwt.secret}") String secret){
  if(secret.getBytes(StandardCharsets.UTF_8).length<32)throw new IllegalArgumentException("JWT_SECRET debe tener al menos 32 bytes");
  return NimbusReactiveJwtDecoder.withSecretKey(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256")).macAlgorithm(MacAlgorithm.HS256).build();
 }
 @Bean SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http){
  ReactiveJwtAuthenticationConverter c=new ReactiveJwtAuthenticationConverter();
  c.setJwtGrantedAuthoritiesConverter(jwt->{String role=jwt.getClaimAsString("rol");return role==null||!role.matches("ROLE_(ADMIN|CAJERO|ALMACEN)")?Flux.empty():Flux.just(new SimpleGrantedAuthority(role));});
  return http.csrf(x->x.disable()).securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
   .authorizeExchange(a->a.pathMatchers(HttpMethod.OPTIONS,"/**").permitAll()
    .pathMatchers("/auth/login","/actuator/health","/actuator/health/**","/libros/uploads/**").permitAll()
    .pathMatchers("/auth/create","/auth/usuarios/**","/usuario/**").hasRole("ADMIN")
    .pathMatchers("/libros/**","/categoria/**","/provedores/**","/proveedores/**","/cliente/**","/clientes/**","/vendedor/**","/vendedores/**","/venta/**","/carrito/**","/servicio/**","/auth/userId","/auth/userName","/auth/validate").authenticated()
    .anyExchange().denyAll())
   .oauth2ResourceServer(o->o.jwt(j->j.jwtAuthenticationConverter(c))).build();
 }
}
