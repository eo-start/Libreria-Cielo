/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msauth.config;
import com.example.msauth.entity.AuthUser;
import com.example.msauth.repository.AuthUserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
@Component
@ConditionalOnProperty(name="app.bootstrap.enabled",havingValue="true")
public class DataInitializer implements ApplicationRunner {
 private final AuthUserRepository repo; private final PasswordEncoder encoder;
 @Value("${app.bootstrap.admin-password}") private String admin;
 @Value("${app.bootstrap.cajero-password}") private String cajero;
 @Value("${app.bootstrap.almacen-password}") private String almacen;
 public DataInitializer(AuthUserRepository repo,PasswordEncoder encoder){this.repo=repo;this.encoder=encoder;}
 public void run(ApplicationArguments args){
  create("admin",admin,AuthUser.Roles.ROLE_ADMIN);
  create("cajero",cajero,AuthUser.Roles.ROLE_CAJERO);
  create("almacen",almacen,AuthUser.Roles.ROLE_ALMACEN);
 }
 private void create(String name,String password,AuthUser.Roles role){
  if(password==null||password.length()<12)throw new IllegalArgumentException("Configura contraseñas iniciales de al menos 12 caracteres");
  if(repo.findByUserName(name).isEmpty())repo.save(AuthUser.builder().userName(name).password(encoder.encode(password)).rol(role).build());
 }
}
