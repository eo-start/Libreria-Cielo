/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msventa.service;

import com.example.msventa.entity.CarritoItem;
import com.example.msventa.repository.CarritoItemRepository;
import com.example.msventa.service.CarritoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CarritoServiceImpl implements CarritoService {

    private final CarritoItemRepository carritoItemRepository;
    private final com.example.msventa.repository.VentaRepository ventaRepository;

    public CarritoServiceImpl(CarritoItemRepository carritoItemRepository,
                              com.example.msventa.repository.VentaRepository ventaRepository) {
        this.carritoItemRepository = carritoItemRepository;
        this.ventaRepository = ventaRepository;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public CarritoItem agregarItem(Integer userId, Integer libroId, Integer cantidad) {
        carritoItemRepository.findForCheckout(userId);
        checkPending(userId);
        if(cantidad==null||cantidad<=0)throw new IllegalArgumentException("Cantidad inválida");
        CarritoItem item = carritoItemRepository.findByUserIdAndLibroId(userId,libroId).orElseGet(CarritoItem::new);
        cantidad=Math.addExact(item.getCantidad()==null?0:item.getCantidad(),cantidad);
        item.setUserId(userId);
        item.setLibroId(libroId);
        item.setCantidad(cantidad);
        return carritoItemRepository.save(item);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void eliminarItem(Integer userId, Integer itemId) {
        carritoItemRepository.findForCheckout(userId);
        checkPending(userId);
        CarritoItem item = carritoItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item no encontrado"));

        if (!item.getUserId().equals(userId)) {
            throw new SecurityException("Acceso denegado");
        }

        carritoItemRepository.delete(item);
    }

    @Override
    public List<CarritoItem> listarItems(Integer userId) {
        return carritoItemRepository.findByUserId(userId);
    }
    private void checkPending(Integer userId){
        if(ventaRepository.findFirstByUserIdAndEstado(userId,"PENDIENTE").isPresent())throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT,"Reintenta la venta pendiente antes de modificar el carrito");
    }
}
