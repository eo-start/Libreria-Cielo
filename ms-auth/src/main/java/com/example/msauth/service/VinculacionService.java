/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msauth.service;

import com.example.msauth.entity.AuthUser;

public interface VinculacionService {
    AuthUser vincularCliente(Integer userId, Integer clienteId);
    AuthUser vincularVendedor(Integer userId, Integer vendedorId);
}
