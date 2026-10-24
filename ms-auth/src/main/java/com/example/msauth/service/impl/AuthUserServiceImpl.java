/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msauth.service.impl;

import com.example.msauth.dto.AuthUserDto;
import com.example.msauth.entity.AuthUser;
import com.example.msauth.entity.TokenDto;

import com.example.msauth.repository.AuthUserRepository;
import com.example.msauth.repository.AuthUserRepository;
import com.example.msauth.security.JwtProvider;
import com.example.msauth.service.AuthUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;


@Service
public class AuthUserServiceImpl implements AuthUserService {
    @Autowired
    AuthUserRepository authRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    JwtProvider jwtProvider;

    @Override
    public AuthUser save(AuthUserDto authUserDto) {
        Optional<AuthUser> user = authRepository.findByUserName(authUserDto.getUserName());

        if (user.isPresent()) {
            return null;
        }

        String password = passwordEncoder.encode(authUserDto.getPassword());

        AuthUser.Roles rol = authUserDto.getRol() != null
                ? authUserDto.getRol()
                : AuthUser.Roles.ROLE_USER;

        AuthUser authUser = AuthUser.builder()
                .userName(authUserDto.getUserName())
                .password(password)
                .rol(rol)
                .clienteId(authUserDto.getClienteId())
                .vendedorId(authUserDto.getVendedorId())
                .build();

        return authRepository.save(authUser);
    }


    @Override
    public TokenDto login(AuthUserDto authUserDto) {
        Optional<AuthUser> user = authRepository.findByUserName(authUserDto.getUserName());
        if (!user.isPresent())
            return null;
        if (passwordEncoder.matches(authUserDto.getPassword(), user.get().getPassword()))
            return new TokenDto(jwtProvider.createToken(user.get()));
        return null;
    }


    @Override
    public TokenDto validate(String token) {
        if (!jwtProvider.validate(token))
            return null;
        String username = jwtProvider.getUserNameFromToken(token);
        if (!authRepository.findByUserName(username).isPresent())
            return null;
        return new TokenDto(token);
    }

    @Override
    public Integer getUserIdFromToken(String token) {
        if (!jwtProvider.validate(token))
            return null;
        String username = jwtProvider.getUserNameFromToken(token);
        Optional<AuthUser> user = authRepository.findByUserName(username);
        return user.map(AuthUser::getId).orElse(null);
    }
    @Override
    public String getUserNameFromToken(String token) {
        if (!jwtProvider.validate(token))
            return null;
        String username = jwtProvider.getUserNameFromToken(token);
        Optional<AuthUser> user = authRepository.findByUserName(username);
        return user.map(AuthUser::getUserName).orElse(null);
    }
    @Override
    public List<AuthUser> listarUsuarios() {
        return authRepository.findAll();
    }

    @Override
    public boolean eliminarUsuario(Integer id) {
        if (!authRepository.findById(id).isPresent()) {
            return false; // Usuario no encontrado
        }
        authRepository.deleteById(id);
        return true;
    }
}
