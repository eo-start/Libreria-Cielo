/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msventa.service;

import com.example.msventa.dto.LibroDto;
import com.example.msventa.dto.ProductoMasVendidoDto;
import com.example.msventa.entity.CarritoItem;
import com.example.msventa.entity.Venta;
import com.example.msventa.entity.VentaDetalle;
import com.example.msventa.feign.AuthFeign;
import com.example.msventa.feign.LibroFeign;
import com.example.msventa.repository.CarritoItemRepository;
import com.example.msventa.repository.VentaRepository;
import com.example.msventa.service.VentaService;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VentaServiceImpl implements VentaService {

    private static final double IGV = 0.18;
    static final int    MAYORISTA_CANTIDAD_MINIMA = 10;
    static final double MAYORISTA_DESCUENTO       = 0.15;

    private final VentaRepository ventaRepository;
    private final CarritoItemRepository carritoItemRepository;
    private final AuthFeign authFeign;
    private final LibroFeign libroFeign;

    public VentaServiceImpl(
            VentaRepository ventaRepository,
            CarritoItemRepository carritoItemRepository,
            AuthFeign authFeign,
            LibroFeign libroFeign) {
        this.ventaRepository = ventaRepository;
        this.carritoItemRepository = carritoItemRepository;
        this.authFeign = authFeign;
        this.libroFeign = libroFeign;
    }

    @Override
    public List<Venta> listarVentas() {
        return ventaRepository.findAll();
    }

    @Override
    public Venta obtenerVentaPorId(Integer id) {
        return ventaRepository.findById(id).orElse(null);
    }

    @Override
    public List<ProductoMasVendidoDto> getProductosMasVendidos() {
        List<Venta> ventas = ventaRepository.findAll();

        Map<Integer, double[]> acumulado = new HashMap<>();
        for (Venta venta : ventas) {
            if (!"COMPLETADA".equals(venta.getEstado())) continue;
            for (VentaDetalle detalle : venta.getDetalles()) {
                if (detalle.getLibroId() == null) continue;
                double[] datos = acumulado.getOrDefault(detalle.getLibroId(), new double[]{0, 0});
                datos[0] += detalle.getCantidad() != null ? detalle.getCantidad() : 0;
                datos[1] += (detalle.getPrecio() != null ? detalle.getPrecio() : 0.0)
                          * (detalle.getCantidad() != null ? detalle.getCantidad() : 0);
                acumulado.put(detalle.getLibroId(), datos);
            }
        }

        List<ProductoMasVendidoDto> resultado = new ArrayList<>();
        acumulado.forEach((libroId, datos) -> {
            String nombre;
            try {
                ResponseEntity<LibroDto> resp = libroFeign.listarLibro(libroId);
                LibroDto libro = resp.getBody();
                nombre = (libro != null && libro.getTitulo() != null && !libro.getTitulo().isBlank())
                       ? libro.getTitulo() : "Producto #" + libroId;
            } catch (Exception e) {
                nombre = "Producto #" + libroId;
            }
            resultado.add(new ProductoMasVendidoDto(null, nombre, (int) datos[0], datos[1]));
        });

        resultado.sort((a, b) -> b.getCantidadVendida() - a.getCantidadVendida());

        List<ProductoMasVendidoDto> top5 = resultado.stream().limit(5).collect(Collectors.toList());
        for (int i = 0; i < top5.size(); i++) {
            top5.get(i).setPosicion(i + 1);
        }
        return top5;
    }

    @Override
    public Venta obtenerVentaConDetalles(Integer ventaId) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada"));

        for (VentaDetalle detalle : venta.getDetalles()) {
            LibroDto libro = obtenerLibroPorId(detalle.getLibroId());
            detalle.setLibro(libro);
        }

        return venta;
    }

    private Integer obtenerUserId(String token) {
        ResponseEntity<Integer> response = authFeign.getUserId(token);

        if (response == null || !response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("No se pudo obtener el userId desde el token");
        }

        Integer userId = response.getBody();
        if (userId == null) {
            throw new IllegalStateException("El userId recibido desde el token es nulo");
        }

        return userId;
    }

    private String obtenerUserName(String token) {
        ResponseEntity<String> response = authFeign.getUserName(token);

        if (response == null || !response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("No se pudo obtener el nombre del usuario desde el token");
        }

        String userName = response.getBody();
        if (userName == null || userName.isBlank()) {
            throw new IllegalStateException("El nombre del usuario recibido desde el token es inválido");
        }

        return userName;
    }

    private LibroDto obtenerLibroPorId(Integer libroId) {
        ResponseEntity<LibroDto> response = libroFeign.listarLibro(libroId);

        if (response == null || !response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException(
                "El producto con ID " + libroId + " ya no existe en el catalogo de libros. " +
                "Limpia el carrito y vuelve a seleccionar productos actuales.");
        }

        LibroDto libro = response.getBody();
        if (libro == null) {
            throw new IllegalStateException(
                "El producto con ID " + libroId + " devolvio una respuesta vacia. " +
                "Verifica que ms-book este activo.");
        }

        return libro;
    }
}