/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import Swal from 'sweetalert2';
import { jsPDF } from 'jspdf';
import { VentaService } from '../../services/venta.service';
import { ServicioService } from '../../services/servicio.service';
import { Venta, Servicio } from '../../models';

@Component({
  selector: 'app-reportes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reportes.component.html',
})
export class ReportesComponent implements OnInit {
  // ── Datos crudos ─────────────────────────────────────────────────────────────
  todasLasVentas:    Venta[]    = [];
  todasLasServicios: Servicio[] = [];

  // ── Datos filtrados por fecha ─────────────────────────────────────────────────
  ventasFiltradas:    Venta[]    = [];
  serviciosFiltrados: Servicio[] = [];

  // ── Top productos (del endpoint real — historial completo) ────────────────────
  productosTop: { posicion: number; producto: string; cantidadVendida: number; total: number }[] = [];

  // ── Estado ────────────────────────────────────────────────────────────────────
  loading    = false;
  exportando = false;
  fechaInicio = '';
  fechaFin    = '';
  maxVenta    = 1;

  ventasPorDia: { dia: string; total: number }[] = [];

  constructor(
    private ventaSvc:    VentaService,
    private servicioSvc: ServicioService,
  ) {}

  ngOnInit() {
    const hoy = new Date();
    this.fechaFin = hoy.toISOString().split('T')[0];
    const ini = new Date(hoy);
    ini.setDate(ini.getDate() - 30);
    this.fechaInicio = ini.toISOString().split('T')[0];
    this.cargarYFiltrar();
  }

  cargarYFiltrar() {
    if (!this.fechaInicio || !this.fechaFin) {
      Swal.fire({ icon: 'warning', title: 'Selecciona fechas', timer: 1500, showConfirmButton: false });
      return;
    }
    this.loading = true;

    forkJoin({
      ventas:      this.ventaSvc.getAll(),
      servicios:   this.servicioSvc.getAll(),
      productosTop: this.ventaSvc.getProductosMasVendidos(),
    }).subscribe({
      next: ({ ventas, servicios, productosTop }) => {
        this.todasLasVentas = ventas.filter(v=>v.estado==='COMPLETADA');
        this.todasLasServicios = servicios;
        this.productosTop      = productosTop;
        this.aplicarFiltro();
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        Swal.fire({ icon: 'error', title: 'Error al cargar datos', text: 'Verifica que ms-venta este activo.' });
      }
    });
  }

  generar() { this.cargarYFiltrar(); }

  private aplicarFiltro() {
    this.ventasFiltradas = this.todasLasVentas.filter(v =>
      v.fecha >= this.fechaInicio && v.fecha <= this.fechaFin
    );
    this.serviciosFiltrados = this.todasLasServicios.filter(s =>
      s.fecha >= this.fechaInicio && s.fecha <= this.fechaFin
    );
    this.calcularVentasPorDia();
  }

  private calcularVentasPorDia() {
    const mapa = new Map<string, number>();
    this.ventasFiltradas.forEach(v => {
      mapa.set(v.fecha, (mapa.get(v.fecha) ?? 0) + v.total);
    });
    const entradas = [...mapa.entries()].sort((a, b) => a[0].localeCompare(b[0])).slice(-10);
    this.ventasPorDia = entradas.map(([dia, total]) => ({ dia: dia.slice(5), total }));
    this.maxVenta     = Math.max(...this.ventasPorDia.map(v => v.total), 1);
  }

  // ── Getters ventas ────────────────────────────────────────────────────────────
  get totalVentas():    number { return this.ventasFiltradas.reduce((s, v) => s + v.total, 0); }
  get nTransacciones(): number { return this.ventasFiltradas.length; }
  get ticketPromedio(): number { return this.nTransacciones > 0 ? this.totalVentas / this.nTransacciones : 0; }

  // ── Getters servicios ─────────────────────────────────────────────────────────
  get totalServicios(): number { return this.serviciosFiltrados.reduce((s, sv) => s + sv.total, 0); }
  get nServicios():     number { return this.serviciosFiltrados.length; }

  // ── Getter combinado ──────────────────────────────────────────────────────────
  get totalCombinado(): number { return this.totalVentas + this.totalServicios; }

  barHeight(valor: number): number {
    return Math.round((valor / this.maxVenta) * 100);
  }

  labelMetodo(m: string): string {
    return m === 'YAPE_PLIN' ? 'Yape/Plin' : m === 'TRANSFERENCIA' ? 'BCP' : 'Efectivo';
  }

  // ── Exportar PDF ──────────────────────────────────────────────────────────────
  exportarPdf() {
    if (this.ventasFiltradas.length === 0 && this.serviciosFiltrados.length === 0) {
      Swal.fire({ icon: 'info', title: 'Sin datos', text: 'No hay registros en el rango de fechas seleccionado.', timer: 2000, showConfirmButton: false });
      return;
    }

    this.exportando = true;
    try {
      const doc = new jsPDF();
      const fechaGen = new Date().toLocaleDateString('es-PE', { day: '2-digit', month: '2-digit', year: 'numeric' });

      // ── Encabezado ─────────────────────────────────────────────────────────────
      doc.setFontSize(15);
      doc.setFont('helvetica', 'bold');
      doc.text('Reporte de Ventas y Servicios - Libreria Amanecer', 14, 18);

      doc.setFontSize(9);
      doc.setFont('helvetica', 'normal');
      doc.text(`Generado: ${fechaGen}`, 14, 26);
      doc.text(`Periodo: ${this.fechaInicio}  al  ${this.fechaFin}`, 14, 32);
      doc.text(
        `Ventas: S/ ${this.totalVentas.toFixed(2)} (${this.nTransacciones})` +
        `   Servicios: S/ ${this.totalServicios.toFixed(2)} (${this.nServicios})` +
        `   Total combinado: S/ ${this.totalCombinado.toFixed(2)}`,
        14, 38
      );

      doc.setDrawColor(37, 99, 235);
      doc.setLineWidth(0.5);
      doc.line(14, 42, 196, 42);

      // ── Tabla ventas ───────────────────────────────────────────────────────────
      if (this.ventasFiltradas.length > 0) {
        const colsV = [14, 40, 72, 100, 150, 176];
        const headersV = ['N Venta', 'Fecha', 'Hora', 'Cliente', 'Total S/', 'Metodo'];

        doc.setFontSize(9);
        doc.setFont('helvetica', 'bold');
        doc.setFillColor(37, 99, 235);
        doc.rect(14, 46, 182, 7, 'F');
        doc.setTextColor(255, 255, 255);
        headersV.forEach((h, i) => doc.text(h, colsV[i], 51.5));

        doc.setFont('helvetica', 'normal');
        let y = 60;
        this.ventasFiltradas.forEach((v, idx) => {
          if (y > 270) { doc.addPage(); y = 20; }
          if (idx % 2 === 0) {
            doc.setFillColor(239, 246, 255);
            doc.rect(14, y - 4, 182, 7, 'F');
          }
          doc.setTextColor(31, 41, 55);
          doc.text(v.numeroVenta,                             colsV[0], y);
          doc.text(v.fecha,                                   colsV[1], y);
          doc.text((v.hora || '').substring(0, 8),            colsV[2], y);
          doc.text((v.clienteNombre || '').substring(0, 22),  colsV[3], y);
          doc.text(`S/ ${v.total.toFixed(2)}`,                colsV[4], y);
          doc.text((v.metodoPago || '').substring(0, 10),     colsV[5], y);
          y += 8;
        });
      }

      // ── Tabla servicios ────────────────────────────────────────────────────────
      if (this.serviciosFiltrados.length > 0) {
        let y2: number = 20;
        if (this.ventasFiltradas.length === 0) {
          y2 = 46;
        } else {
          doc.addPage();
        }

        const colsS = [14, 55, 105, 130, 160, 183];
        const headersS = ['Tipo', 'Cliente', 'Cant.', 'Total S/', 'Metodo', 'Fecha'];

        doc.setFontSize(10);
        doc.setFont('helvetica', 'bold');
        doc.setTextColor(79, 70, 229);
        doc.text('Servicios realizados', 14, y2);
        y2 += 6;

        doc.setFontSize(9);
        doc.setFillColor(79, 70, 229);
        doc.rect(14, y2, 182, 7, 'F');
        doc.setTextColor(255, 255, 255);
        headersS.forEach((h, i) => doc.text(h, colsS[i], y2 + 5.5));

        doc.setFont('helvetica', 'normal');
        y2 += 14;
        this.serviciosFiltrados.forEach((s, idx) => {
          if (y2 > 270) { doc.addPage(); y2 = 20; }
          if (idx % 2 === 0) {
            doc.setFillColor(245, 243, 255);
            doc.rect(14, y2 - 4, 182, 7, 'F');
          }
          doc.setTextColor(31, 41, 55);
          doc.text((s.tipoServicio || '').substring(0, 14),  colsS[0], y2);
          doc.text((s.cliente || '').substring(0, 18),       colsS[1], y2);
          doc.text(String(s.cantidad),                        colsS[2], y2);
          doc.text(`S/ ${s.total.toFixed(2)}`,               colsS[3], y2);
          doc.text(this.labelMetodo(s.metodoPago || ''),     colsS[4], y2);
          doc.text(s.fecha || '',                             colsS[5], y2);
          y2 += 8;
        });
      }

      // ── Pie de pagina ──────────────────────────────────────────────────────────
      doc.setFontSize(8);
      doc.setTextColor(107, 114, 128);
      const totalPages = (doc as any).internal.getNumberOfPages();
      for (let p = 1; p <= totalPages; p++) {
        doc.setPage(p);
        doc.text(`Generado el ${fechaGen} - Sistema de Gestion Libreria Amanecer`, 14, 290);
      }

      // ── Descargar ──────────────────────────────────────────────────────────────
      const blob = doc.output('blob');
      const url  = URL.createObjectURL(blob);
      const a    = document.createElement('a');
      a.href     = url;
      a.download = `reporte-ventas-${this.fechaInicio}-${this.fechaFin}.pdf`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      setTimeout(() => URL.revokeObjectURL(url), 1000);

    } catch (err) {
      Swal.fire({ icon: 'error', title: 'Error al generar PDF', text: 'No se pudo generar el archivo.' });
    } finally {
      this.exportando = false;
    }
  }
}
