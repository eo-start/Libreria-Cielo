/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msauth.service.impl;

import com.example.msauth.dto.ClienteDto;
import com.example.msauth.entity.AuthUser;
import com.example.msauth.feign.ClienteFeign;
import com.example.msauth.repository.AuthUserRepository;
import com.example.msauth.service.AuthUserService;
import com.example.msauth.service.VinculacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class VinculacionServiceImpl implements VinculacionService {
    @Autowired
    private AuthUserRepository authRepository;
    @Autowired
    private ClienteFeign clienteFeign;

    @Override
    public AuthUser vincularCliente(Integer userId, Integer clienteId) {
        Optional<AuthUser> authUserOpt = authRepository.findById(userId);
        if (!authUserOpt.isPresent()) {
            return null; // Usuario no encontrado
        }

        ClienteDto clienteDto = clienteFeign.ListbyId(clienteId).getBody();
        if (clienteDto == null) {
            return null; // Cliente no encontrado
        }

        AuthUser authUser = authUserOpt.get();
        // Desafiliar cliente y vendedor actuales
        authUser.setClienteId(null);
        authUser.setVendedorId(null);
        // Afiliar nuevo cliente
        authUser.setClienteId(clienteId);
        // Actualizar rol
        authUser.setRol(AuthUser.Roles.CLIENTE);

        return authRepository.save(authUser);
    }

    @Override
    public AuthUser vincularVendedor(Integer userId, Integer vendedorId) {
        Optional<AuthUser> authUserOpt = authRepository.findById(userId);
        if (!authUserOpt.isPresent()) {
            return null; // Usuario no encontrado
        }

        ClienteDto vendedorDto = clienteFeign.ListbyId(vendedorId).getBody();
        if (vendedorDto == null) {
            return null; // Vendedor no encontrado
        }

        AuthUser authUser = authUserOpt.get();
        // Desafiliar cliente y vendedor actuales
        authUser.setClienteId(null);
        authUser.setVendedorId(null);
        // Afiliar nuevo vendedor
        authUser.setVendedorId(vendedorId);
        // Actualizar rol
        authUser.setRol(AuthUser.Roles.VENDEDOR);

        return authRepository.save(authUser);
    }
}
