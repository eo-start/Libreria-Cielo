/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msbook.config;

import com.example.msbook.entity.Categoria;
import com.example.msbook.entity.Libro;
import com.example.msbook.entity.Provedores;
import com.example.msbook.models.Formato;
import com.example.msbook.repository.CategoriaRepository;
import com.example.msbook.repository.LibroRepository;
import com.example.msbook.repository.ProvedoresRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name="app.seed-demo-data", havingValue="true")
@Component
public class DataInitializer implements ApplicationRunner {

    private final LibroRepository libroRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProvedoresRepository provedoresRepository;

    public DataInitializer(LibroRepository libroRepository,
                           CategoriaRepository categoriaRepository,
                           ProvedoresRepository provedoresRepository) {
        this.libroRepository = libroRepository;
        this.categoriaRepository = categoriaRepository;
        this.provedoresRepository = provedoresRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Si ya hay 20+ productos, no tocar nada — IDs deben mantenerse estables
        if (libroRepository.count() >= 20) {
            System.out.println("[DataInitializer ms-book] Ya existen " + libroRepository.count() + " productos — sin cambios.");
            return;
        }

        // Primer arranque o base vacía: limpiar y reinicializar
        libroRepository.deleteAll();
        System.out.println("[DataInitializer ms-book] Productos anteriores eliminados.");

        // 2. Eliminar categorías y proveedores
        categoriaRepository.deleteAll();
        provedoresRepository.deleteAll();

        // 3. Crear proveedor general
        Provedores prov = new Provedores();
        prov.setNombre("Proveedor General");
        prov.setTipo("DISTRIBUIDOR");
        prov.setEntrega(new java.util.Date());
        provedoresRepository.save(prov);

        // 4. Crear las 9 categorías necesarias
        Map<String, Categoria> cats = new HashMap<>();
        String[] nombres = {
            "Cuadernos",
            "Lapiceros y lápices",
            "Papel y hojas",
            "Correctores",
            "Tijeras y pegamentos",
            "Reglas y compases",
            "Mochilas y cartucheras",
            "Útiles de oficina",
            "Otros"
        };
        for (String n : nombres) {
            Categoria c = new Categoria();
            c.setNombre(n);
            c.setClasificacion("Escolar");
            c.setFormato(Formato.IMPRESO);
            c.setIdioma("ES");
            cats.put(n, categoriaRepository.save(c));
        }

        // 5. Insertar 20 productos con stock variado (stock realista para el inventario)
        // Columnas: titulo, categoría, precio, stock, stockMínimo
        Object[][] data = {
            {"Cuaderno Gloria 100 hojas",    "Cuadernos",              3.50,  50, 10},  // PRD-001 NORMAL
            {"Cuaderno cuadriculado A4",      "Cuadernos",              4.00,   8, 10},  // PRD-002 BAJO STOCK
            {"Lapicero azul Faber Castell",   "Lapiceros y lápices",    1.50, 100, 20},  // PRD-003 NORMAL
            {"Lapicero negro Pilot",          "Lapiceros y lápices",    2.00,  15, 20},  // PRD-004 BAJO STOCK
            {"Lápiz 2B Staedtler",            "Lapiceros y lápices",    1.00,  60, 15},  // PRD-005 NORMAL
            {"Papel bond A4 500 hojas",       "Papel y hojas",         12.50,   4,  5},  // PRD-006 BAJO STOCK
            {"Papel bond carta 500 hojas",    "Papel y hojas",         11.00,  25,  5},  // PRD-007 NORMAL
            {"Corrector líquido Artesco",     "Correctores",            2.50,  40, 10},  // PRD-008 NORMAL
            {"Corrector en cinta",            "Correctores",            4.00,   3,  8},  // PRD-009 BAJO STOCK
            {"Tijeras escolares",             "Tijeras y pegamentos",   3.00,  25,  5},  // PRD-010 NORMAL
            {"Pegamento en barra UHU",        "Tijeras y pegamentos",   2.00,  45, 10},  // PRD-011 NORMAL
            {"Regla 30cm metálica",           "Reglas y compases",      3.50,  30,  8},  // PRD-012 NORMAL
            {"Compás escolar",                "Reglas y compases",      8.00,   2,  5},  // PRD-013 BAJO STOCK
            {"Mochila escolar grande",        "Mochilas y cartucheras", 45.00, 15,  3},  // PRD-014 NORMAL
            {"Cartuchera doble cierre",       "Mochilas y cartucheras", 12.00, 20,  5},  // PRD-015 NORMAL
            {"Resaltador amarillo",           "Útiles de oficina",      2.50,  50, 10},  // PRD-016 NORMAL
            {"Post-it notas adhesivas",       "Útiles de oficina",      5.00,   7,  8},  // PRD-017 BAJO STOCK
            {"Engrapador pequeño",            "Útiles de oficina",      8.50,  15,  3},  // PRD-018 NORMAL
            {"Borrador blanco Faber",         "Otros",                  0.50,  80, 20},  // PRD-019 NORMAL
            {"Tajador doble",                 "Otros",                  1.00,  60, 15},  // PRD-020 NORMAL
        };

        int num = 1;
        for (Object[] row : data) {
            String titulo    = (String) row[0];
            String catNombre = (String) row[1];
            double precio    = (double) row[2];
            int    stock     = (int)    row[3];
            int    stockMin  = (int)    row[4];

            Libro libro = new Libro();
            libro.setCodigo("PRD-" + String.format("%03d", num++));
            libro.setTitulo(titulo);
            libro.setAutor("Genérico");
            libro.setStock(stock);
            libro.setStockMinimo(stockMin);
            libro.setPrecio(precio);
            libro.setCategoria(cats.get(catNombre));
            libro.setProvedores(prov);
            libro.setAnio(LocalDate.of(2024, 1, 1));
            libroRepository.save(libro);

            String estadoLabel = stock <= 0 ? "REPONER" : (stock < stockMin ? "BAJO STOCK" : "normal");
            System.out.printf("[DataInitializer ms-book] %-40s stock:%-3d min:%-3d [%s]%n",
                    titulo, stock, stockMin, estadoLabel);
        }

        System.out.println("[DataInitializer ms-book] 20 productos insertados correctamente.");
    }
}
