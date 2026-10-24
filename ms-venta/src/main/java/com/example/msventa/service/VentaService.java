/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msventa.service;

import com.example.msventa.dto.ProductoMasVendidoDto;
import com.example.msventa.entity.Venta;

import java.util.List;

public interface VentaService {


    List<Venta> listarVentas();

    Venta obtenerVentaPorId(Integer id);

    Venta obtenerVentaConDetalles(Integer ventaId);

    List<ProductoMasVendidoDto> getProductosMasVendidos();
}