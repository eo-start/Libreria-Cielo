/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msventa.exception;

public class TokenAuthenticationException extends RuntimeException {

    public TokenAuthenticationException(String message) {
        super(message);
    }
}