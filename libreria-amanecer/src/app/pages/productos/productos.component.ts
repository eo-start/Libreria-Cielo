/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import Swal from 'sweetalert2';
import { jsPDF } from 'jspdf';
import { ProductoService, Categoria } from '../../services/producto.service';
import { Producto } from '../../models';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-productos',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './productos.component.html',
})
export class ProductosComponent implements OnInit {
  private fb  = inject(FormBuilder);
  private svc = inject(ProductoService);
  auth        = inject(AuthService);

  productos: Producto[] = [];
  filtrados:  Producto[] = [];
  categorias: Categoria[] = [];
  busqueda      = '';
  filtroCategoria = '';
  filtroEstado    = '';
  loading   = true;
  showModal = false;
  editando: Producto | null = null;
  imagenSeleccionada: File | null = null;

  form = this.fb.group({
    titulo:       ['', [Validators.required, Validators.minLength(2)]],
    precio:       [0,   [Validators.required, Validators.min(0.01)]],
    stock:        [0,   [Validators.required, Validators.min(0)]],
    stockMinimo:  [10,  [Validators.required, Validators.min(1)]],
    categoriaId:  [null as number | null, Validators.required],
    provedoresId: [1],
    estado:       ['ACTIVO'],
  });

  ngOnInit() { this.cargar(); }

  cargar() {
    this.loading = true;
    this.svc.getCategorias().subscribe(c => this.categorias = c);
    this.svc.getAll().subscribe(data => {
      this.productos = data;
      this.aplicarFiltros();
      this.loading = false;
    });
  }

  aplicarFiltros() {
    this.filtrados = this.productos.filter(p => {
      const q = this.busqueda.toLowerCase();
      const matchBusq = !q || p.nombre.toLowerCase().includes(q) || p.codigo.toLowerCase().includes(q);
      const matchCat  = !this.filtroCategoria || p.categoria === this.filtroCategoria;
      const matchEst  = !this.filtroEstado    || p.estado === this.filtroEstado;
      return matchBusq && matchCat && matchEst;
    });
  }

  abrirModal(producto?: Producto) {
    if (!producto && !this.auth.can('productos.crear')) return;
    this.editando          = producto || null;
    this.imagenSeleccionada = null;
    if (producto) {
      const cat = this.categorias.find(c => c.nombre === producto.categoria);
      this.form.patchValue({
        titulo:       producto.nombre,
        precio:       producto.precio,
        stock:        producto.stock,
        stockMinimo:  producto.stockMinimo,
        categoriaId:  cat?.id ?? null,
        provedoresId: 1,
        estado:       producto.estado === 'INACTIVO' ? 'INACTIVO' : 'ACTIVO',
      });
    } else {
      this.form.reset({ provedoresId: 1, estado: 'ACTIVO' });
    }
    this.showModal = true;
  }

  cerrarModal() { this.showModal = false; this.editando = null; this.imagenSeleccionada = null; }

  onImagenChange(event: Event) {
    const input = event.target as HTMLInputElement;
    this.imagenSeleccionada = input.files?.[0] ?? null;
  }

  guardar() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }

    const v = this.form.value;
    const fd = new FormData();
    fd.append('titulo',      v.titulo!);
    fd.append('autor',       'Sin autor');
    fd.append('stock',       String(v.stock ?? 0));
    fd.append('stockMinimo', String(v.stockMinimo ?? 10));
    fd.append('precio',      String(v.precio ?? 0));
    fd.append('anio',        String(new Date().getFullYear()));
    fd.append('estado',      v.estado ?? 'ACTIVO');
    if (v.categoriaId) fd.append('categoriaId', String(v.categoriaId));
    if (!this.editando) fd.append('provedoresId', String(v.provedoresId ?? 1));
    if (this.imagenSeleccionada) fd.append('imagen', this.imagenSeleccionada);

    const obs = this.editando
      ? this.svc.update(this.editando.id, fd)
      : this.svc.create(fd);

    obs.subscribe({
      next: () => {
        Swal.fire({ icon: 'success', title: this.editando ? 'Producto actualizado' : 'Producto creado', timer: 1500, showConfirmButton: false });
        this.cerrarModal();
        this.cargar();
      },
      error: (err) => {
        console.error('[Productos] Error al guardar:', err);
        Swal.fire({ icon: 'error', title: 'Error al guardar', text: `Status ${err.status}: ${err.error?.message || 'Verifica que ms-book esté corriendo.'}` });
      }
    });
  }

  eliminar(p: Producto) {
    Swal.fire({
      title: '¿Eliminar producto?', text: `Se eliminará "${p.nombre}".`,
      icon: 'warning', showCancelButton: true,
      confirmButtonText: 'Sí, eliminar', cancelButtonText: 'Cancelar',
      confirmButtonColor: '#dc2626', cancelButtonColor: '#6b7280'
    }).then(r => {
      if (!r.isConfirmed) return;
      this.svc.delete(p.id).subscribe({
        next: () => { Swal.fire({ icon: 'success', title: 'Eliminado', timer: 1200, showConfirmButton: false }); this.cargar(); },
        error: () => Swal.fire({ icon: 'error', title: 'Error al eliminar.' })
      });
    });
  }

  getBadge(estado: string): string {
    const m: Record<string, string> = { ACTIVO: 'badge badge-green', STOCK_BAJO: 'badge badge-yellow', INACTIVO: 'badge badge-red' };
    return m[estado] || 'badge badge-gray';
  }

  filtrarStockBajo() {
    this.filtroEstado = 'STOCK_BAJO';
    this.busqueda = '';
    this.filtroCategoria = '';
    this.aplicarFiltros();
  }

  exportarStockBajoPdf() {
    const stockBajo = this.productos.filter(p => p.stock > 0 && p.stock < p.stockMinimo);

    if (stockBajo.length === 0) {
      Swal.fire({ icon: 'info', title: 'Sin productos', text: 'No hay productos con stock bajo actualmente.', timer: 2000, showConfirmButton: false });
      return;
    }

    const doc = new jsPDF();
    const fecha = new Date().toLocaleDateString('es-PE', { day: '2-digit', month: '2-digit', year: 'numeric' });

    // Encabezado
    doc.setFontSize(16);
    doc.setFont('helvetica', 'bold');
    doc.text('Reporte de Stock Bajo - Librería Amanecer', 14, 18);

    doc.setFontSize(10);
    doc.setFont('helvetica', 'normal');
    doc.text(`Fecha: ${fecha}`, 14, 26);
    doc.text(`Total de productos con stock bajo: ${stockBajo.length}`, 14, 32);

    // Línea separadora
    doc.setDrawColor(37, 99, 235);
    doc.setLineWidth(0.5);
    doc.line(14, 36, 196, 36);

    // Encabezados de tabla
    const colX = [14, 38, 90, 122, 142, 162, 180];
    const headers = ['Código', 'Nombre', 'Categoría', 'Stock', 'Mínimo', 'Reponer'];

    doc.setFontSize(9);
    doc.setFont('helvetica', 'bold');
    doc.setFillColor(37, 99, 235);
    doc.rect(14, 40, 182, 7, 'F');
    doc.setTextColor(255, 255, 255);
    headers.forEach((h, i) => doc.text(h, colX[i], 45.5));

    // Filas
    doc.setFont('helvetica', 'normal');
    let y = 53;
    stockBajo.forEach((p, idx) => {
      const reponer = p.stockMinimo - p.stock;
      if (idx % 2 === 0) {
        doc.setFillColor(239, 246, 255);
        doc.rect(14, y - 4, 182, 7, 'F');
      }
      doc.setTextColor(220, 38, 38); // rojo para stock bajo
      doc.text(p.codigo,                          colX[0], y);
      doc.setTextColor(31, 41, 55);
      doc.text(p.nombre.substring(0, 28),         colX[1], y);
      doc.text(p.categoria.substring(0, 18),      colX[2], y);
      doc.text(String(p.stock),                   colX[3], y);
      doc.text(String(p.stockMinimo),             colX[4], y);
      doc.setTextColor(220, 38, 38);
      doc.text(`+${reponer}`,                     colX[5], y);
      doc.setTextColor(31, 41, 55);
      y += 8;

      if (y > 270) { doc.addPage(); y = 20; }
    });

    // Pie
    doc.setFontSize(8);
    doc.setTextColor(107, 114, 128);
    doc.text(`Generado el ${fecha} — Sistema de Gestión Librería Amanecer`, 14, 285);

    doc.save(`stock-bajo-${fecha.replace(/\//g, '-')}.pdf`);
  }

  get categoriasFiltro(): string[] {
    return [...new Set(this.productos.map(p => p.categoria))];
  }

  get productosStockBajo(): number {
    return this.productos.filter(p => p.stock > 0 && p.stock < p.stockMinimo).length;
  }

  get f() { return this.form.controls; }

  soloLetras(event: KeyboardEvent): void {
    const char = event.key;
    if (char.length > 1) return;
    if (/[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\s]/.test(char)) return;
    event.preventDefault();
  }
}
