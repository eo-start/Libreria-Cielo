/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msventa.service;

import com.example.msventa.entity.Servicio;

import java.util.List;

public interface ServicioService {
    Servicio guardar(Servicio servicio);
    List<Servicio> listar();
}
