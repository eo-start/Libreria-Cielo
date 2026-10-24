/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msauth.security;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
@Configuration
public class SecurityConfig {
 @Bean JwtDecoder jwtDecoder(@Value("${jwt.secret}") String secret) {
  if(secret.getBytes(StandardCharsets.UTF_8).length<32) throw new IllegalArgumentException("JWT_SECRET debe tener al menos 32 bytes");
  return NimbusJwtDecoder.withSecretKey(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256")).macAlgorithm(MacAlgorithm.HS256).build();
 }
 @Bean SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
  JwtAuthenticationConverter converter=new JwtAuthenticationConverter();
  converter.setJwtGrantedAuthoritiesConverter(jwt -> {String role=jwt.getClaimAsString("rol");return role==null||!role.matches("ROLE_(ADMIN|CAJERO|ALMACEN)")?List.of():List.of(new SimpleGrantedAuthority(role));});
  return http.csrf(c->c.disable()).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .authorizeHttpRequests(a->{
    a.requestMatchers(HttpMethod.OPTIONS,"/**").permitAll();
    a.requestMatchers("/actuator/health","/actuator/health/**","/error").permitAll();
    a.requestMatchers(HttpMethod.POST,"/auth/login").permitAll();
    a.requestMatchers("/auth/create","/auth/usuarios/**","/usuario/**").hasRole("ADMIN");
    a.requestMatchers("/auth/validate","/auth/userId","/auth/userName").hasAnyRole("ADMIN","CAJERO","ALMACEN");
    a.anyRequest().denyAll();
   }).oauth2ResourceServer(o->o.jwt(j->j.jwtAuthenticationConverter(converter))).build();
 }
}
