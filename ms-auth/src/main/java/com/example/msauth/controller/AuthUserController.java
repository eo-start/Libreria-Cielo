/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msauth.controller;

import com.example.msauth.dto.AuthUserDto;
import com.example.msauth.entity.AuthUser;
import com.example.msauth.entity.TokenDto;
import com.example.msauth.service.AuthUserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Autenticación",
        description = "Operaciones de autenticación, validación JWT y administración de usuarios."
)
@RestController
@RequestMapping("/auth")
public class AuthUserController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthUserService authUserService;

    public AuthUserController(AuthUserService authUserService) {
        this.authUserService = authUserService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@RequestBody AuthUserDto authUserDto) {
        return okOrBadRequest(authUserService.login(authUserDto));
    }

    @PostMapping("/validate")
    public ResponseEntity<TokenDto> validate(@RequestParam String token) {
        return okOrBadRequest(authUserService.validate(token));
    }

    @PostMapping("/create")
    public ResponseEntity<AuthUser> create(@RequestBody AuthUserDto authUserDto) {
        return okOrBadRequest(authUserService.save(authUserDto));
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<AuthUser>> listarUsuarios() {
        return ResponseEntity.ok(authUserService.listarUsuarios());
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Integer id) {
        if (authUserService.eliminarUsuario(id)) {
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/userId")
    public ResponseEntity<Integer> getUserId(@RequestHeader("Authorization") String token) {
        String jwt = cleanBearerToken(token);
        return okOrBadRequest(authUserService.getUserIdFromToken(jwt));
    }

    @GetMapping("/userName")
    public ResponseEntity<String> getUserName(@RequestHeader("Authorization") String token) {
        String jwt = cleanBearerToken(token);
        return okOrBadRequest(authUserService.getUserNameFromToken(jwt));
    }

    private <T> ResponseEntity<T> okOrBadRequest(T body) {
        if (body == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(body);
    }

    private String cleanBearerToken(String token) {
        if (token != null && token.startsWith(BEARER_PREFIX)) {
            return token.substring(BEARER_PREFIX.length());
        }

        return token;
    }
}