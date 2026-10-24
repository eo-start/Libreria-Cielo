/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.mscliente.service.impl;

import com.example.mscliente.entity.Vendedor;
import com.example.mscliente.repository.VendedorRepository;
import com.example.mscliente.service.VendedorService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VendedorServiceImpl implements VendedorService {

    private final VendedorRepository vendedorRepository;

    public VendedorServiceImpl(VendedorRepository vendedorRepository) {
        this.vendedorRepository = vendedorRepository;
    }

    @Override
    public List<Vendedor> listar() {
        return vendedorRepository.findAll();
    }

    @Override
    public Vendedor guardar(Vendedor vendedor) {
        return vendedorRepository.save(vendedor);
    }

    @Override
    public Vendedor editar(Vendedor vendedor, Integer id) {
        if (vendedorRepository.existsById(id)) {
            vendedor.setId(id);
            return vendedorRepository.save(vendedor);
        }
        return null;
    }

    @Override
    public void eliminar(Integer id) {
        vendedorRepository.deleteById(id);
    }
}