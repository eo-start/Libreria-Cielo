/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msauth.security;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class AuthorizationTest {
 @Autowired MockMvc mvc;
 @Test void anonymousCannotRegisterAdministratorOrListOrDeleteUsers() throws Exception {
  mvc.perform(post("/auth/create").contentType(MediaType.APPLICATION_JSON).content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(java.util.Map.of("userName","attacker","password","pass","rol","ROLE_ADMIN")))).andExpect(status().isUnauthorized());
  mvc.perform(get("/auth/usuarios")).andExpect(status().isUnauthorized());mvc.perform(delete("/auth/usuarios/1")).andExpect(status().isUnauthorized());
 }
 @Test void cashierCannotManageUsers() throws Exception {mvc.perform(get("/auth/usuarios").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CAJERO")))).andExpect(status().isForbidden());}
 @Test void adminCanListUsers() throws Exception {mvc.perform(get("/auth/usuarios").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))).andExpect(status().isOk());}
}
