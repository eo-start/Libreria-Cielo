/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msventa.service;

import com.example.msventa.dto.LibroDto;
import com.example.msventa.entity.Venta;
import com.example.msventa.entity.VentaDetalle;
import com.example.msventa.feign.LibroFeign;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class PdfService {

    private static final BaseColor AZUL       = new BaseColor(37, 99, 235);
    private static final BaseColor VERDE      = new BaseColor(21, 128, 61);
    private static final BaseColor GRIS_CLARO = new BaseColor(243, 244, 246);

    private final LibroFeign libroFeign;

    public PdfService(LibroFeign libroFeign) {
        this.libroFeign = libroFeign;
    }

    // ── Boleta individual ────────────────────────────────────────────────────────

    public byte[] generarReciboPdf(Venta venta) throws DocumentException {
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        document.open();

        Font titleFont  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font boldMed    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font bold10     = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font normal10   = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
        Font greenBold  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, VERDE);
        Font totalFont  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font smallGray  = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY);

        // ── Encabezado ──────────────────────────────────────────────────────────
        document.add(new Paragraph("Libreria Amanecer", titleFont));
        document.add(new Paragraph("Comprobante académico de demostración", normal10));
        document.add(new Paragraph(" "));

        String tipoBoleta = venta.isEsMayorista()
                ? "COMPROBANTE DE VENTA - MAYORISTA"
                : "COMPROBANTE DE VENTA";
        document.add(new Paragraph(tipoBoleta, boldMed));
        document.add(new Paragraph(" "));

        // ── Datos de la venta ────────────────────────────────────────────────────
        PdfPTable tableInfo = new PdfPTable(2);
        tableInfo.setWidthPercentage(100);
        tableInfo.setWidths(new float[]{3f, 5f});
        tableInfo.setSpacingBefore(4f);
        tableInfo.setSpacingAfter(4f);

        agregarFilaInfo(tableInfo, "N Boleta:", String.format("B-%03d", venta.getId()), bold10, normal10);
        agregarFilaInfo(tableInfo, "Fecha:", formatearFecha(venta.getFecha()), bold10, normal10);
        agregarFilaInfo(tableInfo, "Cliente:", venta.getUserName() != null ? venta.getUserName() : "Sin nombre", bold10, normal10);

        document.add(tableInfo);
        document.add(new Paragraph(" "));

        // ── Detalle de productos ─────────────────────────────────────────────────
        PdfPTable tableDetalles = new PdfPTable(5);
        tableDetalles.setWidthPercentage(100);
        tableDetalles.setWidths(new float[]{0.5f, 4.5f, 1f, 2f, 2f});
        tableDetalles.setSpacingBefore(4f);
        tableDetalles.setSpacingAfter(4f);

        for (String h : new String[]{"#", "Descripcion", "Cant.", "P. Unit.", "Subtotal"}) {
            PdfPCell hCell = new PdfPCell(new Phrase(h, headerFont));
            hCell.setBackgroundColor(AZUL);
            hCell.setPadding(4f);
            tableDetalles.addCell(hCell);
        }

        int idx = 1;
        for (VentaDetalle detalle : venta.getDetalles()) {
            boolean conDescuento = detalle.getPrecioOriginal() != null
                    && detalle.getPrecio() != null
                    && detalle.getPrecioOriginal() > detalle.getPrecio();

            String nombre = (detalle.getNombreProducto() != null && !detalle.getNombreProducto().isBlank())
                    ? detalle.getNombreProducto()
                    : "Producto #" + detalle.getLibroId();
            String descLabel = conDescuento ? nombre + " [MAYORISTA -15%]" : nombre;

            tableDetalles.addCell(datoCell(String.valueOf(idx++), normal10));
            tableDetalles.addCell(datoCell(descLabel, normal10));
            tableDetalles.addCell(datoCell(String.valueOf(detalle.getCantidad()), normal10));

            String precioStr;
            if (conDescuento) {
                precioStr = String.format("Orig: S/%.2f\nDto:  S/%.2f",
                        detalle.getPrecioOriginal(), detalle.getPrecio());
            } else {
                double p = detalle.getPrecio() != null ? detalle.getPrecio() : 0.0;
                precioStr = String.format("S/%.2f", p);
            }
            tableDetalles.addCell(datoCell(precioStr, normal10));

            double sub = (detalle.getPrecio() != null ? detalle.getPrecio() : 0.0)
                    * (detalle.getCantidad() != null ? detalle.getCantidad() : 0);
            tableDetalles.addCell(datoCell(String.format("S/%.2f", sub), normal10));
        }

        document.add(tableDetalles);

        // ── Totales ──────────────────────────────────────────────────────────────
        PdfPTable tableTotales = new PdfPTable(2);
        tableTotales.setWidthPercentage(50);
        tableTotales.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tableTotales.setSpacingBefore(10f);
        tableTotales.setWidths(new float[]{4f, 2f});

        agregarFilaTotales(tableTotales, "Subtotal:", String.format("S/ %.2f", venta.getTotal()), bold10, normal10);

        if (venta.isEsMayorista() && venta.getDescuentoTotal() > 0) {
            agregarFilaTotales(tableTotales,
                    "Descuento mayorista:",
                    String.format("-S/ %.2f", venta.getDescuentoTotal()),
                    greenBold, greenBold);
        }

        agregarFilaTotales(tableTotales, "IGV (18%):", String.format("S/ %.2f", venta.getIgv()), bold10, normal10);
        agregarFilaTotales(tableTotales, "TOTAL:", String.format("S/ %.2f", venta.getTotalConIgv()), totalFont, totalFont);

        document.add(tableTotales);

        document.add(new Paragraph(" "));
        document.add(new Paragraph("Gracias por su compra - Libreria Amanecer", smallGray));

        document.close();
        return baos.toByteArray();
    }

    // ── Reporte general de ventas ────────────────────────────────────────────────

    public byte[] generarRegistroVentasPdf(List<Venta> ventas) throws DocumentException {
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        document.open();

        Font bold18  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font normal  = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Font bold16  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);

        document.add(new Paragraph("Libreria Amanecer", bold18));
        document.add(new Paragraph("Comprobante académico de demostración", normal));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Registro de Ventas", bold16));
        document.add(new Paragraph(" "));

        PdfPTable tableVentas = new PdfPTable(6);
        tableVentas.setWidthPercentage(100);
        tableVentas.setSpacingBefore(10f);
        tableVentas.setSpacingAfter(10f);
        tableVentas.setWidths(new float[]{1f, 2f, 2f, 2f, 2f, 2f});

        tableVentas.addCell("ID Venta");
        tableVentas.addCell("Fecha");
        tableVentas.addCell("Cliente");
        tableVentas.addCell("Total");
        tableVentas.addCell("IGV");
        tableVentas.addCell("Total con IGV");

        for (Venta venta : ventas) {
            tableVentas.addCell(String.valueOf(venta.getId()));
            tableVentas.addCell(formatearFecha(venta.getFecha()));
            tableVentas.addCell(venta.getUserName() != null ? venta.getUserName() : "-");
            tableVentas.addCell(String.format("%.2f", venta.getTotal()));
            tableVentas.addCell(String.format("%.2f", venta.getIgv()));
            tableVentas.addCell(String.format("%.2f", venta.getTotalConIgv()));

            PdfPTable tableDetalles = new PdfPTable(5);
            tableDetalles.setWidthPercentage(100);
            tableDetalles.setSpacingBefore(5f);
            tableDetalles.setWidths(new float[]{1f, 3f, 2f, 1f, 2f});

            tableDetalles.addCell("Item");
            tableDetalles.addCell("Descripcion");
            tableDetalles.addCell("Cantidad");
            tableDetalles.addCell("Precio");
            tableDetalles.addCell("Precio con IGV");

            int itemCounter = 1;
            for (VentaDetalle detalle : venta.getDetalles()) {
                LibroDto libro = obtenerLibroDesdeDetalle(detalle);
                String titulo = libro.getTitulo();
                String autor  = libro.getAutor();
                tableDetalles.addCell(String.valueOf(itemCounter++));
                tableDetalles.addCell(titulo + " - " + autor);
                tableDetalles.addCell(String.valueOf(detalle.getCantidad()));
                tableDetalles.addCell(String.format("%.2f", detalle.getPrecio()));
                tableDetalles.addCell(String.format("%.2f", detalle.getPrecio() * 1.18));
            }

            PdfPCell detallesCell = new PdfPCell(tableDetalles);
            detallesCell.setColspan(6);
            tableVentas.addCell(detallesCell);
        }

        document.add(tableVentas);
        document.close();
        return baos.toByteArray();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private void agregarFilaInfo(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell lc = new PdfPCell(new Phrase(label, labelFont));
        lc.setBorder(Rectangle.NO_BORDER);
        lc.setPadding(3f);
        table.addCell(lc);

        PdfPCell vc = new PdfPCell(new Phrase(value, valueFont));
        vc.setBorder(Rectangle.NO_BORDER);
        vc.setPadding(3f);
        table.addCell(vc);
    }

    private void agregarFilaTotales(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell lc = new PdfPCell(new Phrase(label, labelFont));
        lc.setBorder(Rectangle.NO_BORDER);
        lc.setHorizontalAlignment(Element.ALIGN_RIGHT);
        lc.setPadding(3f);
        table.addCell(lc);

        PdfPCell vc = new PdfPCell(new Phrase(value, valueFont));
        vc.setBorder(Rectangle.NO_BORDER);
        vc.setHorizontalAlignment(Element.ALIGN_RIGHT);
        vc.setPadding(3f);
        table.addCell(vc);
    }

    private PdfPCell datoCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(3f);
        return cell;
    }

    private String formatearFecha(Date fecha) {
        if (fecha == null) return "-";
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(fecha);
    }

    private LibroDto obtenerLibroDesdeDetalle(VentaDetalle detalle) {
        if (detalle.getLibro() != null) {
            return detalle.getLibro();
        }

        ResponseEntity<LibroDto> response = libroFeign.listarLibro(detalle.getLibroId());

        if (response != null && response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            detalle.setLibro(response.getBody());
            return response.getBody();
        }

        // Libro eliminado del catálogo: usar placeholder para no bloquear la generación del PDF
        LibroDto placeholder = new LibroDto();
        placeholder.setId(detalle.getLibroId());
        placeholder.setTitulo("Producto #" + detalle.getLibroId() + " (eliminado)");
        placeholder.setAutor("-");
        placeholder.setStock(0);
        placeholder.setPrecio(detalle.getPrecio() != null ? detalle.getPrecio() : 0.0);
        detalle.setLibro(placeholder);
        return placeholder;
    }
}
