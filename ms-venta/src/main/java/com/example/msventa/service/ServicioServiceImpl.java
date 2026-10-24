/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msventa.service;

import com.example.msventa.entity.Servicio;
import com.example.msventa.repository.ServicioRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ServicioServiceImpl implements ServicioService {

    private final ServicioRepository servicioRepository;

    public ServicioServiceImpl(ServicioRepository servicioRepository) {
        this.servicioRepository = servicioRepository;
    }

    @Override
    public Servicio guardar(Servicio servicio) {
        if (servicio.getFecha() == null) servicio.setFecha(new Date());
        if (servicio.getMetodoPago() == null || servicio.getMetodoPago().isBlank())
            servicio.setMetodoPago("EFECTIVO");
        return servicioRepository.save(servicio);
    }

    @Override
    public List<Servicio> listar() {
        return servicioRepository.findAllByOrderByFechaDesc();
    }
}
