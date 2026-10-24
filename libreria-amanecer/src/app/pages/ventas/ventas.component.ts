/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import Swal from 'sweetalert2';
import { ProductoService } from '../../services/producto.service';
import { ClienteService } from '../../services/cliente.service';
import { VentaService } from '../../services/venta.service';
import { Producto, Cliente, ItemCarrito } from '../../models';

export type MetodoPago = 'EFECTIVO' | 'YAPE_PLIN' | 'TRANSFERENCIA';

// Constantes mayorista — cambiar aquí para ajustar la política de descuento
const MAYORISTA_MIN  = 10;
const MAYORISTA_PORC = 0.15;

@Component({
  selector: 'app-ventas',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ventas.component.html',
})
export class VentasComponent implements OnInit {
  readonly MAYORISTA_MIN  = MAYORISTA_MIN;
  readonly MAYORISTA_PORC = MAYORISTA_PORC;

  productos: Producto[] = [];
  clientes:  Cliente[]  = [];
  carrito:   ItemCarrito[] = [];

  productoSeleccionado: Producto | null = null;
  cantidad = 1;

  // ── Cliente ────────────────────────────────────────────────────────────────
  busquedaCliente    = '';
  clientesFiltrados: Cliente[] = [];
  clienteRegistrado: Cliente | null = null;
  clienteCasual      = '';
  tipoBadge: 'registrado' | 'casual' | null = null;
  showDropdown       = false;

  // ── Pago ───────────────────────────────────────────────────────────────────
  metodoPago: MetodoPago = 'EFECTIVO';

  // Efectivo
  montoRecibido: number | null = null;

  // Transferencia
  nroOperacion = '';

  // Yape/Plin
  yapePlinConfirmado = false;

  procesando = false;
  ventaPendiente: any = null;

  constructor(
    private productoSvc: ProductoService,
    private clienteSvc:  ClienteService,
    private ventaSvc:    VentaService,
  ) {}

  ngOnInit() {
    this.cargarCarrito();
    this.ventaSvc.getPendiente().subscribe(v=>this.ventaPendiente=v);
    this.productoSvc.getAll().subscribe(d => this.productos = d.filter(p => p.stock > 0 && p.estado !== 'INACTIVO'));
    this.clienteSvc.getAll().subscribe(d => this.clientes  = d.filter(c => c.estado === 'ACTIVO'));
  }

  // ── Métodos de pago ────────────────────────────────────────────────────────

  seleccionarMetodo(m: MetodoPago) {
    this.metodoPago         = m;
    this.montoRecibido      = null;
    this.nroOperacion       = '';
    this.yapePlinConfirmado = false;
  }

  get vuelto(): number {
    return (this.montoRecibido ?? 0) - this.total;
  }

  get montoInsuficiente(): boolean {
    return this.montoRecibido !== null && this.montoRecibido < this.total;
  }

  get puedeRegistrar(): boolean {
    if (this.carrito.length === 0 || !this.tieneCliente) return false;
    if (this.metodoPago === 'TRANSFERENCIA') return this.nroOperacion.trim().length >= 4;
    return true; // EFECTIVO y YAPE_PLIN siempre habilitados si hay carrito y cliente
  }

  // ── Cliente ────────────────────────────────────────────────────────────────

  onBusquedaChange() {
    const q = this.busquedaCliente.trim().toLowerCase();
    this.clienteRegistrado = null;
    this.tipoBadge         = null;
    if (q.length < 2) { this.clientesFiltrados = []; this.showDropdown = false; return; }
    this.clientesFiltrados = this.clientes.filter(c =>
      c.nombreCompleto.toLowerCase().includes(q) || c.dni.includes(q)
    ).slice(0, 8);
    this.showDropdown = this.clientesFiltrados.length > 0;
  }

  seleccionarCliente(c: Cliente) {
    this.clienteRegistrado = c;
    this.busquedaCliente   = c.nombreCompleto;
    this.clienteCasual     = '';
    this.tipoBadge         = 'registrado';
    this.showDropdown      = false;
    this.clientesFiltrados = [];
  }

  onBlurCliente() {
    setTimeout(() => {
      this.showDropdown = false;
      if (!this.clienteRegistrado && this.busquedaCliente.trim().length >= 2) {
        this.clienteCasual = this.busquedaCliente.trim();
        this.tipoBadge     = 'casual';
      } else if (!this.busquedaCliente.trim()) {
        this.tipoBadge = null; this.clienteCasual = '';
      }
    }, 200);
  }

  get tieneCliente(): boolean {
    return !!this.clienteRegistrado || this.clienteCasual.length >= 2;
  }

  // ── Carrito ────────────────────────────────────────────────────────────────

  agregarAlCarrito() {
    if (!this.productoSeleccionado) {
      Swal.fire({ icon: 'warning', title: 'Selecciona un producto', timer: 1500, showConfirmButton: false }); return;
    }
    if (this.cantidad <= 0) {
      Swal.fire({ icon: 'warning', title: 'Cantidad inválida', timer: 1500, showConfirmButton: false }); return;
    }
    const item          = this.carrito.find(i => i.producto.id === this.productoSeleccionado!.id);
    const cantidadTotal = (item?.cantidad || 0) + this.cantidad;
    if (cantidadTotal > this.productoSeleccionado.stock) {
      Swal.fire({ icon: 'warning', title: 'Stock insuficiente', text: `Solo hay ${this.productoSeleccionado.stock} unidades.` }); return;
    }

    const prod = this.productoSeleccionado;
    const qty  = this.cantidad;

    this.ventaSvc.agregarAlCarrito(prod.id, qty).subscribe(()=>this.cargarCarrito());

    this.productoSeleccionado = null;
    this.cantidad = 1;
  }

  eliminarDelCarrito(idx:number){
    const id=this.carrito[idx].carritoItemId;
    if(id)this.ventaSvc.eliminarDelCarrito(id).subscribe(()=>this.cargarCarrito());
  }
  cargarCarrito(){
    forkJoin({items:this.ventaSvc.getCarrito(),productos:this.productoSvc.getAll()}).subscribe(({items,productos})=>{
      this.carrito=items.map(i=>({carritoItemId:i.id,producto:productos.find(p=>p.id===i.libroId)!,cantidad:i.cantidad})).filter(i=>!!i.producto);
    });
  }
  reintentarPendiente(){
    if(!this.ventaPendiente||this.procesando)return;
    this.procesando=true;
    this.ventaSvc.reintentar(this.ventaPendiente.id).subscribe({next:()=>{this.procesando=false;this.ventaPendiente=null;this.cargarCarrito();Swal.fire({icon:'success',title:'Venta confirmada',text:'Consulta el comprobante en el historial.'});},error:()=>{this.procesando=false;this.ventaSvc.getPendiente().subscribe(v=>this.ventaPendiente=v);}});
  }

  // ── Lógica mayorista ──────────────────────────────────────────────────────

  esMayoristaItem(item: ItemCarrito): boolean {
    return item.cantidad >= MAYORISTA_MIN;
  }

  precioFinalItem(item: ItemCarrito): number {
    return Math.round(((this.esMayoristaItem(item) ? item.producto.precio * (1 - MAYORISTA_PORC) : item.producto.precio) + Number.EPSILON) * 100) / 100;
  }

  subtotalItem(item: ItemCarrito): number {
    return this.precioFinalItem(item) * item.cantidad;
  }

  get esMayoristaVenta(): boolean {
    return this.carrito.some(i => this.esMayoristaItem(i));
  }

  get descuentoTotal(): number {
    return this.carrito.reduce((s, i) => {
      if (!this.esMayoristaItem(i)) return s;
      return s + ((i.producto.precio - this.precioFinalItem(i)) * i.cantidad);
    }, 0);
  }

  get subtotal(): number { return this.carrito.reduce((s, i) => s + this.subtotalItem(i), 0); }
  get igv():      number { return Math.round((this.subtotal * 0.18 + Number.EPSILON)*100)/100; }
  get total():    number { return this.subtotal + this.igv; }

  // ── Registrar venta ────────────────────────────────────────────────────────

  registrarVenta() {
    if(this.procesando||this.ventaPendiente){Swal.fire({icon:'warning',title:'Hay una venta pendiente',text:'Reintenta su confirmación antes de crear otra.'});return;}
    if (this.carrito.length === 0) {
      Swal.fire({ icon: 'warning', title: 'Carrito vacío' }); return;
    }
    if (!this.tieneCliente) {
      Swal.fire({ icon: 'warning', title: 'Sin cliente' }); return;
    }
    if (this.metodoPago === 'EFECTIVO' && this.montoInsuficiente) {
      Swal.fire({ icon: 'warning', title: 'Monto insuficiente', text: `El monto recibido no cubre el total de S/ ${this.total.toFixed(2)}` }); return;
    }
    if (this.metodoPago === 'TRANSFERENCIA' && this.nroOperacion.trim().length < 4) {
      Swal.fire({ icon: 'warning', title: 'Ingresa el N° de operación' }); return;
    }

    this.procesando = true;
    const nombreCliente = this.clienteRegistrado?.nombreCompleto || this.clienteCasual || '';
    const pendientes    = this.carrito.filter(i => !i.carritoItemId);
    const agregar$      = pendientes.length > 0
      ? forkJoin(pendientes.map(i => this.ventaSvc.agregarAlCarrito(i.producto.id, i.cantidad)))
      : of([]);

    agregar$.pipe(
      switchMap(() => this.ventaSvc.realizarVenta(nombreCliente,this.metodoPago,this.nroOperacion,this.clienteRegistrado?.id))
    ).subscribe({
      next: (ventaResp: any) => {
        this.procesando = false;
        const ventaId = ventaResp?.id as number | undefined;
        this.limpiarFormulario();

        if (!ventaId) {
          Swal.fire({ icon: 'success', title: '¡Venta registrada!', confirmButtonColor: '#2563eb' });
          return;
        }

        this.ventaSvc.getReciboPdf(ventaId).subscribe({
          next: (blob) => {
            const url = URL.createObjectURL(blob);
            const a   = document.createElement('a');
            a.href     = url;
            a.download = `boleta-${ventaId}.pdf`;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            setTimeout(() => URL.revokeObjectURL(url), 1000);

            Swal.fire({
              icon: 'success',
              title: '¡Venta registrada!',
              text: 'La boleta se descargó automáticamente.',
              confirmButtonColor: '#2563eb',
            });
          },
          error: () => {
            Swal.fire({
              icon: 'success',
              title: '¡Venta registrada!',
              text: 'No se pudo descargar la boleta. Intenta desde el historial.',
              confirmButtonColor: '#2563eb',
            });
          },
        });
      },
      error: (err) => {
        this.procesando = false;
        this.ventaSvc.getPendiente().subscribe(v=>this.ventaPendiente=v);
        const msg: string = err.error?.message || '';

        if (msg.includes('ya no existe') || msg.includes('todos los productos ya no existen')) {
          this.carrito = [];
          this.productoSvc.getAll().subscribe(d =>
            this.productos = d.filter(p => p.stock > 0 && p.estado !== 'INACTIVO')
          );
          Swal.fire({
            icon: 'warning',
            title: 'Productos no disponibles',
            text: 'Los productos del carrito ya no están disponibles. Por favor selecciona los productos nuevamente.',
            confirmButtonColor: '#2563eb',
          });
        } else {
          Swal.fire({ icon: 'error', title: 'Error al registrar', text: msg || 'Verifica que ms-venta esté activo.' });
        }
      }
    });
  }

  limpiarCarrito() {
    Swal.fire({
      title: '¿Cancelar venta?', icon: 'question', showCancelButton: true,
      confirmButtonText: 'Sí, cancelar', cancelButtonText: 'No',
      confirmButtonColor: '#dc2626', cancelButtonColor: '#6b7280'
    }).then(r => { if (r.isConfirmed) this.limpiarFormulario(); });
  }

  private limpiarFormulario() {
    this.carrito           = [];
    this.clienteRegistrado = null;
    this.clienteCasual     = '';
    this.busquedaCliente   = '';
    this.tipoBadge         = null;
    this.metodoPago         = 'EFECTIVO';
    this.montoRecibido      = null;
    this.nroOperacion       = '';
    this.yapePlinConfirmado = false;
  }
}
