/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msventa.config;

import com.example.msventa.entity.Servicio;
import com.example.msventa.entity.Venta;
import com.example.msventa.entity.VentaDetalle;
import com.example.msventa.repository.CarritoItemRepository;
import com.example.msventa.repository.ServicioRepository;
import com.example.msventa.repository.VentaRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name="app.seed-demo-data", havingValue="true")
@Component
public class DataInitializer implements ApplicationRunner {

    private final VentaRepository ventaRepository;
    private final ServicioRepository servicioRepository;
    private final CarritoItemRepository carritoItemRepository;

    private static final String[] CLIENTES = {
        "Juan Pérez", "María García", "Carlos López", "Ana Torres", "Pedro Ruiz",
        "Rosa Mamani", "Luis Quispe", "Elena Vargas", "Marco Silva", "Sofía Castro",
        "Diego Flores", "Carmen Ramos", "Roberto Chávez", "Patricia Huanca", "Miguel Condori"
    };

    private static final String[] METODOS_PAGO = {"EFECTIVO", "YAPE", "TRANSFERENCIA"};

    private static final String[] TIPOS_SERVICIO = {"FOTOCOPIADO", "IMPRESION", "ANILLADO", "ENCUADERNADO"};

    private static final double[] PRECIOS_SERVICIO = {0.10, 0.50, 5.00, 15.00};

    private static final int[][] CANTIDADES_SERVICIO = {
        {10, 200}, // FOTOCOPIADO
        {1,  50},  // IMPRESION
        {1,  10},  // ANILLADO
        {1,   5},  // ENCUADERNADO
    };

    public DataInitializer(VentaRepository ventaRepository,
                           ServicioRepository servicioRepository,
                           CarritoItemRepository carritoItemRepository) {
        this.ventaRepository = ventaRepository;
        this.servicioRepository = servicioRepository;
        this.carritoItemRepository = carritoItemRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (ventaRepository.count() == 0) {
            insertarVentasEjemplo();
        }
        if (servicioRepository.count() == 0) {
            insertarServiciosEjemplo();
        }
    }

    private Date fechaAleatoria(int diasAtras, int horaBase) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -diasAtras);
        c.set(Calendar.HOUR_OF_DAY, horaBase);
        c.set(Calendar.MINUTE, (diasAtras * 7 + horaBase * 3) % 60);
        c.set(Calendar.SECOND, 0);
        return c.getTime();
    }

    private double totalAleatorio() {
        // Entre 5.00 y 200.00, redondeado a 2 decimales
        double raw = 5.0 + Math.random() * 195.0;
        return Math.round(raw * 100.0) / 100.0;
    }

    private Venta crearVentaSimple(int clienteIdx, String userName,
                                    Date fecha, int libroId, int cantidad, double precio) {
        Venta v = new Venta();
        v.setUserId(1);
        v.setUserName(userName);
        v.setFecha(fecha);

        VentaDetalle d = new VentaDetalle();
        d.setVenta(v);
        d.setLibroId(libroId);
        d.setCantidad(cantidad);
        d.setPrecio(precio);
        v.getDetalles().add(d);

        double subtotal = Math.round(precio * cantidad * 100.0) / 100.0;
        double igv = Math.round(subtotal * 0.18 * 100.0) / 100.0;
        v.setTotal(subtotal);
        v.setIgv(igv);
        v.setTotalConIgv(Math.round((subtotal + igv) * 100.0) / 100.0);
        return v;
    }

    private void insertarVentasEjemplo() {
        // IDs de libros disponibles en el DataInitializer de ms-book
        int[] libroIds  = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
        double[] precios = {3.50, 4.00, 1.50, 2.00, 8.00, 12.50, 6.00, 2.50, 45.00, 15.00,
                            3.00, 3.50, 25.00, 45.00, 18.00, 2.50, 5.00, 30.00, 7.50, 12.00};

        List<Venta> ventas = new ArrayList<>();
        int ventaIdx = 0;

        // 100 ventas distribuidas en 7 días (~14-15 por día)
        // Horas del día distribuidas para simular actividad real (8:00 a 19:00)
        int[] horasDia = {8, 9, 9, 10, 10, 11, 11, 12, 13, 14, 14, 15, 16, 17, 18};

        for (int dia = 0; dia < 7; dia++) {
            int ventasPorDia = (dia == 6) ? 16 : 12; // último día tiene 16, resto ~12 → total ~100
            for (int h = 0; h < ventasPorDia; h++) {
                int clienteIdx = ventaIdx % CLIENTES.length;
                int libroIdx   = ventaIdx % libroIds.length;
                int hora       = horasDia[h % horasDia.length];
                int cantidad   = 1 + (ventaIdx % 4);

                Date fecha = fechaAleatoria(dia, hora);

                Venta v = crearVentaSimple(
                    clienteIdx,
                    CLIENTES[clienteIdx],
                    fecha,
                    libroIds[libroIdx],
                    cantidad,
                    precios[libroIdx]
                );
                ventas.add(v);
                ventaIdx++;
            }
        }

        ventaRepository.saveAll(ventas);
        System.out.println("[DataInitializer ms-venta] " + ventas.size() + " ventas de ejemplo insertadas.");
    }

    private void insertarServiciosEjemplo() {
        List<Servicio> servicios = new ArrayList<>();

        for (int i = 0; i < 50; i++) {
            int tipoIdx    = i % TIPOS_SERVICIO.length;
            int clienteIdx = i % CLIENTES.length;
            int metodoPagoIdx = i % METODOS_PAGO.length;
            int dia        = i % 7;
            int hora       = 8 + (i % 10);

            int[] rango    = CANTIDADES_SERVICIO[tipoIdx];
            int cantidad   = rango[0] + (int)(Math.random() * (rango[1] - rango[0] + 1));
            double precio  = PRECIOS_SERVICIO[tipoIdx];
            double total   = Math.round(cantidad * precio * 100.0) / 100.0;

            Servicio s = new Servicio();
            s.setTipoServicio(TIPOS_SERVICIO[tipoIdx]);
            s.setCliente(CLIENTES[clienteIdx]);
            s.setCantidad(cantidad);
            s.setPrecioUnitario(precio);
            s.setTotal(total);
            s.setMetodoPago(METODOS_PAGO[metodoPagoIdx]);
            s.setFecha(fechaAleatoria(dia, hora));
            servicios.add(s);
        }

        servicioRepository.saveAll(servicios);
        System.out.println("[DataInitializer ms-venta] " + servicios.size() + " servicios de ejemplo insertados.");
    }
}
