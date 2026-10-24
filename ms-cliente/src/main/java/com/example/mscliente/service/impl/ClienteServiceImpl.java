/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.mscliente.service.impl;

import com.example.mscliente.entity.Cliente;
import com.example.mscliente.repository.ClienteRepository;
import com.example.mscliente.service.ClienteService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteServiceImpl(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public List<Cliente> listar() {
        return clienteRepository.findAll();
    }

    @Override
    public Cliente guardar(Cliente cliente) {
        long count = clienteRepository.count();
        cliente.setCodigo("CLI-" + String.format("%03d", count + 1));
        if (cliente.getEstado() == null || cliente.getEstado().isBlank()) {
            cliente.setEstado("ACTIVO");
        }
        return clienteRepository.save(cliente);
    }

    @Override
    public Cliente buscarPorId(Integer id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
    }

    @Override
    public Cliente editar(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    @Override
    public void eliminar(Integer id) {
        clienteRepository.deleteById(id);
    }
}
