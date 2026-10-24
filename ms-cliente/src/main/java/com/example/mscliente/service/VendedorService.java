/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.mscliente.service;

import com.example.mscliente.entity.Vendedor;

import java.util.List;

public interface VendedorService {
    public List<Vendedor> listar();
    public Vendedor guardar(Vendedor vendedor);
    public Vendedor editar(Vendedor vendedor, Integer id);
    public void eliminar(Integer id);
}
