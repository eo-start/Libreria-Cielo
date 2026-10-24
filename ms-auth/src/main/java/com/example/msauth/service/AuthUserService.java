/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msauth.service;

import com.example.msauth.dto.AuthUserDto; // DTO para transportar los datos del usuario
import com.example.msauth.entity.AuthUser; // Entidad que representa al usuario
import com.example.msauth.entity.TokenDto; // DTO para representar el token JWT

import java.util.List; // Lista de usuarios

public interface AuthUserService {

    /**
     * Guarda un nuevo usuario en el sistema.
     *
     * @param authUserDto Datos del usuario a guardar
     * @return AuthUser El usuario creado con los datos proporcionados
     */
    public AuthUser save(AuthUserDto authUserDto);

    /**
     * Realiza el login de un usuario, validando sus credenciales y generando un token JWT.
     *
     * @param authUserDto Datos de login del usuario
     * @return TokenDto El token JWT generado si el login es exitoso
     */
    public TokenDto login(AuthUserDto authUserDto);

    /**
     * Valida un token JWT.
     *
     * @param token El token JWT a validar
     * @return TokenDto Un objeto que contiene la información sobre la validez del token
     */
    public TokenDto validate(String token);

    /**
     * Obtiene el ID del usuario a partir de su token JWT.
     *
     * @param token El token JWT del usuario
     * @return Integer El ID del usuario extraído del token
     */
    Integer getUserIdFromToken(String token);

    /**
     * Obtiene el nombre del usuario a partir de su token JWT.
     *
     * @param token El token JWT del usuario
     * @return String El nombre del usuario extraído del token
     */
    String getUserNameFromToken(String token);

    /**
     * Lista todos los usuarios registrados en el sistema.
     *
     * @return List<AuthUser> Lista de usuarios registrados
     */
    List<AuthUser> listarUsuarios();

    /**
     * Elimina un usuario del sistema dado su ID.
     *
     * @param id El ID del usuario a eliminar
     * @return boolean Retorna true si el usuario fue eliminado correctamente, false si no se encontró
     */
    boolean eliminarUsuario(Integer id);
}
