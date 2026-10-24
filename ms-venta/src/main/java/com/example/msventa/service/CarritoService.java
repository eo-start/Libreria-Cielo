/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msventa.service;

import com.example.msventa.entity.CarritoItem;

import java.util.List;

public interface CarritoService {
    CarritoItem agregarItem(Integer userId, Integer libroId, Integer cantidad);
    void eliminarItem(Integer userId, Integer itemId);
    List<CarritoItem> listarItems(Integer userId);
}