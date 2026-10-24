/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import Swal from 'sweetalert2';
import { VentaService } from '../../services/venta.service';
import { ServicioService } from '../../services/servicio.service';
import { Venta, Servicio } from '../../models';

interface RegistroCombinado {
  tipo: 'VENTA' | 'SERVICIO';
  id: number;
  numero: string;
  fecha: string;
  hora: string;
  cliente: string;
  total: number;
  metodoPago: string;
  estado: string;
  ventaRef?: Venta;
}

@Component({
  selector: 'app-historial-ventas',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './historial-ventas.component.html',
})
export class HistorialVentasComponent implements OnInit {
  // ── Tab activo ──────────────────────────────────────────────────────────────
  tabActual: 'ventas' | 'servicios' | 'todos' = 'ventas';

  // ── Ventas ──────────────────────────────────────────────────────────────────
  ventas:          Venta[] = [];
  ventasFiltradas: Venta[] = [];
  loadingVentas = true;
  busquedaV     = '';
  fechaInicioV  = '';
  fechaFinV     = '';
  ventaDetalle: Venta | null = null;

  // ── Servicios ───────────────────────────────────────────────────────────────
  servicios:          Servicio[] = [];
  serviciosFiltrados: Servicio[] = [];
  loadingServicios = true;
  busquedaS        = '';
  fechaInicioS     = '';
  fechaFinS        = '';
  filtroMetodoS    = '';

  // ── Todos ───────────────────────────────────────────────────────────────────
  todos:          RegistroCombinado[] = [];
  todosFiltrados: RegistroCombinado[] = [];
  loadingTodos = true;
  busquedaT    = '';
  fechaInicioT = '';
  fechaFinT    = '';
  filtroTipoT  = '';

  metodosServicio = ['EFECTIVO', 'YAPE_PLIN', 'TRANSFERENCIA'];

  constructor(
    private ventaSvc:    VentaService,
    private servicioSvc: ServicioService,
  ) {}

  ngOnInit() {
    forkJoin({
      ventas:    this.ventaSvc.getAll(),
      servicios: this.servicioSvc.getAll(),
    }).subscribe({
      next: ({ ventas, servicios }) => {
        this.ventas          = ventas;
        this.ventasFiltradas = ventas;
        this.loadingVentas   = false;

        this.servicios          = servicios;
        this.serviciosFiltrados = servicios;
        this.loadingServicios   = false;

        this.todos          = this.combinarYOrdenar(ventas, servicios);
        this.todosFiltrados = [...this.todos];
        this.loadingTodos   = false;
      },
      error: () => {
        this.loadingVentas    = false;
        this.loadingServicios = false;
        this.loadingTodos     = false;
      }
    });
  }

  private combinarYOrdenar(ventas: Venta[], servicios: Servicio[]): RegistroCombinado[] {
    const ventasMapped: RegistroCombinado[] = ventas.map(v => ({
      tipo:      'VENTA' as const,
      id:        v.id,
      numero:    v.numeroVenta,
      fecha:     v.fecha,
      hora:      v.hora,
      cliente:   v.clienteNombre,
      total:     v.total,
      metodoPago: v.metodoPago,
      estado:    v.estado,
      ventaRef:  v,
    }));

    const serviciosMapped: RegistroCombinado[] = servicios.map(s => ({
      tipo:      'SERVICIO' as const,
      id:        s.id,
      numero:    `S-${String(s.id).padStart(3, '0')}`,
      fecha:     s.fecha,
      hora:      s.hora,
      cliente:   s.cliente,
      total:     s.total,
      metodoPago: s.metodoPago,
      estado:    'COMPLETADA',
    }));

    return [...ventasMapped, ...serviciosMapped].sort((a, b) => {
      if (b.fecha !== a.fecha) return b.fecha.localeCompare(a.fecha);
      return b.id - a.id;
    });
  }

  // ── Filtros ventas ──────────────────────────────────────────────────────────
  filtrarVentas() {
    this.ventasFiltradas = this.ventas.filter(v => {
      const matchBusq = !this.busquedaV ||
        v.numeroVenta.toLowerCase().includes(this.busquedaV.toLowerCase()) ||
        v.clienteNombre.toLowerCase().includes(this.busquedaV.toLowerCase());
      const matchIni = !this.fechaInicioV || v.fecha >= this.fechaInicioV;
      const matchFin = !this.fechaFinV    || v.fecha <= this.fechaFinV;
      return matchBusq && matchIni && matchFin;
    });
  }

  get totalVentas():     number { return this.ventasFiltradas.reduce((s, v) => s + v.total, 0); }
  get ticketPromedioV(): number { return this.ventasFiltradas.length > 0 ? this.totalVentas / this.ventasFiltradas.length : 0; }

  verDetalle(v: Venta)  { this.ventaDetalle = v; }
  cerrarDetalle()        { this.ventaDetalle = null; }

  descargarRecibo(id: number): void {
    this.ventaSvc.getReciboPdf(id).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a   = document.createElement('a');
        a.href     = url;
        a.download = `boleta-venta-${id}.pdf`;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: () => Swal.fire({ icon: 'error', title: 'Error', text: 'No se pudo descargar la boleta.', timer: 2000, showConfirmButton: false }),
    });
  }

  // ── Filtros servicios ───────────────────────────────────────────────────────
  filtrarServicios() {
    this.serviciosFiltrados = this.servicios.filter(s => {
      const matchBusq   = !this.busquedaS || s.cliente.toLowerCase().includes(this.busquedaS.toLowerCase()) || s.tipoServicio.toLowerCase().includes(this.busquedaS.toLowerCase());
      const matchIni    = !this.fechaInicioS || s.fecha >= this.fechaInicioS;
      const matchFin    = !this.fechaFinS    || s.fecha <= this.fechaFinS;
      const matchMetodo = !this.filtroMetodoS || s.metodoPago === this.filtroMetodoS;
      return matchBusq && matchIni && matchFin && matchMetodo;
    });
  }

  get totalServicios():  number { return this.serviciosFiltrados.reduce((s, sv) => s + sv.total, 0); }
  get ticketPromedioS(): number { return this.serviciosFiltrados.length > 0 ? this.totalServicios / this.serviciosFiltrados.length : 0; }

  // ── Filtros todos ───────────────────────────────────────────────────────────
  filtrarTodos() {
    this.todosFiltrados = this.todos.filter(r => {
      const matchBusq = !this.busquedaT ||
        r.numero.toLowerCase().includes(this.busquedaT.toLowerCase()) ||
        r.cliente.toLowerCase().includes(this.busquedaT.toLowerCase());
      const matchIni  = !this.fechaInicioT || r.fecha >= this.fechaInicioT;
      const matchFin  = !this.fechaFinT    || r.fecha <= this.fechaFinT;
      const matchTipo = !this.filtroTipoT  || r.tipo === this.filtroTipoT;
      return matchBusq && matchIni && matchFin && matchTipo;
    });
  }

  get totalTodos():      number { return this.todosFiltrados.reduce((s, r) => s + r.total, 0); }
  get ticketPromedioT(): number { return this.todosFiltrados.length > 0 ? this.totalTodos / this.todosFiltrados.length : 0; }

  getBadge(estado: string): string {
    const m: Record<string, string> = { COMPLETADA: 'badge badge-green', CANCELADA: 'badge badge-red', PENDIENTE: 'badge badge-yellow' };
    return m[estado] || 'badge badge-gray';
  }

  labelMetodo(m: string): string {
    return m === 'YAPE_PLIN' ? 'Yape/Plin' : m === 'TRANSFERENCIA' ? 'BCP' : 'Efectivo';
  }
}
