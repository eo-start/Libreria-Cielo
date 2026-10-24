/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msbook.service;

import com.example.msbook.entity.Provedores;

import java.util.List;
import java.util.Optional;

public interface ProvedoresService {

    List<Provedores> listar();

    Provedores guardar(Provedores provedores);

    Provedores editar(Provedores provedores, Integer id);

    void eliminar(Integer id);

    Optional<Provedores> listarPorId(Integer id);
}