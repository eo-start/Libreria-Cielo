/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msauth.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String userName;

    @Builder.Default
    private Roles rol = Roles.ROLE_USER;

    @JsonIgnore
    private String password;

    private Integer clienteId;
    private Integer vendedorId;

    public enum Roles {
        CLIENTE,
        VENDEDOR,
        ROLE_ADMIN,
        ROLE_CAJERO,
        ROLE_ALMACEN,
        ROLE_USER
    }
}