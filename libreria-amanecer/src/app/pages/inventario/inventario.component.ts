/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { switchMap, catchError } from 'rxjs/operators';
import { of } from 'rxjs';
import Swal from 'sweetalert2';
import { InventarioService } from '../../services/inventario.service';
import { ProductoService } from '../../services/producto.service';
import { AuthService } from '../../services/auth.service';
import { StockInventario, MovimientoInventario, Producto } from '../../models';

@Component({
  selector: 'app-inventario',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './inventario.component.html',
})
export class InventarioComponent implements OnInit {
  private fb        = inject(FormBuilder);
  private svc       = inject(InventarioService);
  private productoSvc = inject(ProductoService);
  private authSvc   = inject(AuthService);

  stock: StockInventario[] = [];
  movimientos: MovimientoInventario[] = [];
  productos: Producto[] = [];
  loading = true;
  showModalEntrada = false;
  showModalSalida  = false;

  form = this.fb.group({
    productoId: [null as number | null, Validators.required],
    cantidad:   [1, [Validators.required, Validators.min(1)]],
    notas:      [''],
  });

  ngOnInit() {
    this.productoSvc.getAll().subscribe(p => this.productos = p);
    this.svc.getStock().subscribe(data => { this.stock = data; this.loading = false; });
    this.svc.getMovimientos().subscribe(m => this.movimientos = m);
  }

  get totalProductos(): number { return this.stock.length; }
  get stockBajoCount(): number { return this.stock.filter(s => s.estado !== 'NORMAL').length; }

  abrirEntrada() { this.form.reset({ cantidad: 1 }); this.showModalEntrada = true; }
  abrirSalida()  { this.form.reset({ cantidad: 1 }); this.showModalSalida  = true; }
  cerrarModales(){ this.showModalEntrada = false; this.showModalSalida = false; }

  registrarEntrada() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const { productoId, cantidad, notas } = this.form.value;

    const stockItem = this.stock.find(s => s.productoId === productoId);
    const nombreProducto = stockItem?.producto
      || this.productos.find(p => p.id === productoId)?.nombre
      || `Producto #${productoId}`;

    this.svc.registrarEntrada({ productoId: productoId!, cantidad: cantidad!, notas: notas || '' }).pipe(
      switchMap(() => this.svc.guardarMovimiento({
        libroId:        productoId!,
        nombreProducto,
        tipo:           'ENTRADA',
        cantidad:       cantidad!,
        notas:          notas || '',
        usuario:        this.authSvc.getUsuario(),
      }).pipe(catchError(() => of(null))))
    ).subscribe({
      next: () => {
        Swal.fire({ icon: 'success', title: 'Entrada registrada', timer: 1500, showConfirmButton: false });
        this.cerrarModales();
        this.recargar();
      },
      error: () => {
        Swal.fire({ icon: 'error', title: 'Error al registrar', text: 'Verifica que ms-book este activo.' });
      }
    });
  }

  registrarSalida() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const { productoId, cantidad, notas } = this.form.value;

    const stockItem = this.stock.find(s => s.productoId === productoId);
    if (stockItem && cantidad! > stockItem.stockActual) {
      Swal.fire({ icon: 'warning', title: 'Stock insuficiente', text: `Solo hay ${stockItem.stockActual} unidades.` });
      return;
    }

    const nombreProducto = stockItem?.producto
      || this.productos.find(p => p.id === productoId)?.nombre
      || `Producto #${productoId}`;

    this.svc.registrarSalida({ productoId: productoId!, cantidad: cantidad!, notas: notas || '' }).pipe(
      switchMap(() => this.svc.guardarMovimiento({
        libroId:        productoId!,
        nombreProducto,
        tipo:           'SALIDA',
        cantidad:       cantidad!,
        notas:          notas || '',
        usuario:        this.authSvc.getUsuario(),
      }).pipe(catchError(() => of(null))))
    ).subscribe({
      next: () => {
        Swal.fire({ icon: 'success', title: 'Salida registrada', timer: 1500, showConfirmButton: false });
        this.cerrarModales();
        this.recargar();
      },
      error: () => {
        Swal.fire({ icon: 'error', title: 'Error al registrar', text: 'Verifica que ms-book este activo.' });
      }
    });
  }

  private recargar() {
    this.loading = true;
    this.svc.getStock().subscribe(data => { this.stock = data; this.loading = false; });
    this.svc.getMovimientos().subscribe(m => this.movimientos = m);
  }

  actualizarEstado(item: StockInventario) {
    if (item.stockActual <= 0)                     item.estado = 'REPONER';
    else if (item.stockActual < item.stockMinimo)  item.estado = 'BAJO_STOCK';
    else                                           item.estado = 'NORMAL';
  }

  getBadge(estado: string): string {
    const m: Record<string, string> = { NORMAL: 'badge badge-green', BAJO_STOCK: 'badge badge-yellow', REPONER: 'badge badge-red' };
    return m[estado] || 'badge badge-gray';
  }

  getMovBadge(tipo: string): string {
    return tipo === 'ENTRADA' ? 'badge badge-green' : 'badge badge-red';
  }

  get f() { return this.form.controls; }
}
