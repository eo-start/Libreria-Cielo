/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msauth.security;

import com.example.msauth.entity.AuthUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtProvider {

    // Se lee la clave secreta desde el archivo de propiedades (en este caso, se espera que esté en jwt.secret)
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Inicializa la clave secreta codificándola en Base64
     * Este método es ejecutado automáticamente después de que los valores de las propiedades sean inyectados.
     */
    @PostConstruct
    protected void init() {
        // Codifica la clave secreta en Base64 para asegurar que sea compatible con el algoritmo de firma
        secret = Base64.getEncoder().encodeToString(secret.getBytes());
    }

    /**
     * Crea un token JWT a partir de los datos del usuario.
     *
     * @param authUser El objeto que contiene la información del usuario.
     * @return El token JWT generado.
     */
    public String createToken(AuthUser authUser) {
        // Se crea un mapa de "claims" para almacenar los datos del usuario en el token.
        Map<String, Object> claims = new HashMap<>();
        claims = Jwts.claims().setSubject(authUser.getUserName());
        claims.put("id", authUser.getId());
        claims.put("rol", authUser.getRol().name()); // rol incluido para que el frontend lo lea sin llamada extra

        // Fecha actual y la fecha de expiración (1 hora después de la creación del token)
        Date now = new Date();
        Date exp = new Date(now.getTime() + 60 * 60 * 1000 ); // Expiración en 1 hora (3600000 milisegundos)

        // Se construye el token JWT usando la clave secreta y el algoritmo HS256
        return Jwts.builder()
                .setClaims(claims) // Se añaden los "claims"
                .setIssuedAt(now) // Se establece la fecha de emisión
                .setExpiration(exp) // Se establece la fecha de expiración
                .signWith(SignatureAlgorithm.HS256, secret) // Firma el token usando el algoritmo HS256 y la clave secreta
                .compact(); // Se construye el token en formato compactado
    }

    /**
     * Valida si un token JWT es válido o no.
     *
     * @param token El token JWT a validar.
     * @return true si el token es válido, false en caso contrario.
     */
    public boolean validate(String token) {
        try {
            // Intenta parsear el token usando la clave secreta. Si es válido, no lanza excepción.
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // Si ocurre alguna excepción, significa que el token es inválido.
            return false;
        }
    }

    /**
     * Extrae el nombre de usuario desde un token JWT.
     *
     * @param token El token JWT del que se extraerá el nombre de usuario.
     * @return El nombre de usuario extraído del token.
     */
    public String getUserNameFromToken(String token) {
        try {
            // Intenta obtener el nombre de usuario del token
            return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().getSubject();
        } catch (Exception e) {
            // Si ocurre algún error, devuelve un mensaje indicando que el token es incorrecto.
            return "bad token";
        }
    }
}
