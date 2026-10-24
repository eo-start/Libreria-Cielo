/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import Swal from 'sweetalert2';
import { jsPDF } from 'jspdf';
import { ServicioService } from '../../services/servicio.service';
import { Servicio } from '../../models';

export type MetodoPagoServicio = 'EFECTIVO' | 'YAPE_PLIN' | 'TRANSFERENCIA';

@Component({
  selector: 'app-servicios',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './servicios.component.html',
})
export class ServiciosComponent implements OnInit {
  private fb  = inject(FormBuilder);
  private svc = inject(ServicioService);

  recientes: Servicio[] = [];
  loading    = true;
  procesando = false;

  metodoPago: MetodoPagoServicio = 'EFECTIVO';
  nroOperacion = '';

  get tiposServicio() { return this.svc.TIPOS_SERVICIO; }

  form = this.fb.group({
    tipoServicio:   ['', Validators.required],
    cliente:        ['', [Validators.required, Validators.minLength(2)]],
    cantidad:       [1,  [Validators.required, Validators.min(1)]],
    precioUnitario: [0,  [Validators.required, Validators.min(0.01)]],
  });

  ngOnInit() {
    this.svc.getAll().subscribe({
      next:  data  => { this.recientes = data; this.loading = false; },
      error: ()    => { this.recientes = [];   this.loading = false;
        Swal.fire({ icon: 'warning', title: 'Backend no disponible', text: 'Verifica que ms-venta esté activo.', timer: 2500, showConfirmButton: false });
      }
    });
  }

  seleccionarMetodo(m: MetodoPagoServicio) {
    this.metodoPago  = m;
    this.nroOperacion = '';
  }

  get puedeRegistrar(): boolean {
    if (this.form.invalid) return false;
    if (this.metodoPago === 'TRANSFERENCIA') return this.nroOperacion.trim().length >= 4;
    return true;
  }

  onTipoChange() {
    const tipo = this.tiposServicio.find(t => t.nombre === this.form.value.tipoServicio);
    if (tipo) this.form.patchValue({ precioUnitario: tipo.precioBase });
  }

  get totalCalculado(): number {
    return (this.form.value.cantidad || 0) * (this.form.value.precioUnitario || 0);
  }

  registrar() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    if (this.metodoPago === 'TRANSFERENCIA' && this.nroOperacion.trim().length < 4) {
      Swal.fire({ icon: 'warning', title: 'Ingresa el N° de operación', timer: 1500, showConfirmButton: false }); return;
    }

    this.procesando = true;
    const v = this.form.value;
    const payload = {
      tipoServicio:   v.tipoServicio!,
      cliente:        v.cliente!,
      cantidad:       v.cantidad!,
      precioUnitario: v.precioUnitario!,
      total:          this.totalCalculado,
      metodoPago:     this.metodoPago,
    };

    this.svc.registrar(payload).subscribe({
      next: (nuevo) => {
        this.procesando = false;
        this.recientes.unshift(nuevo);
        this.generarReciboPDF(nuevo);
        Swal.fire({
          icon: 'success', title: '¡Servicio registrado!',
          text: 'El recibo se descargo automaticamente.',
          timer: 2500, showConfirmButton: false
        });
        this.form.reset({ cantidad: 1, precioUnitario: 0 });
        this.metodoPago   = 'EFECTIVO';
        this.nroOperacion = '';
      },
      error: (err) => {
        this.procesando = false;
        Swal.fire({ icon: 'error', title: 'Error al registrar', text: err.error?.message || 'Verifica que ms-venta esté activo.' });
      }
    });
  }

  get f() { return this.form.controls; }

  soloLetras(event: KeyboardEvent): void {
    const char = event.key;
    if (char.length > 1) return;
    if (/[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\s]/.test(char)) return;
    event.preventDefault();
  }

  private generarReciboPDF(s: Servicio): void {
    const doc = new jsPDF();

    // Encabezado empresa
    doc.setFontSize(14);
    doc.setFont('helvetica', 'bold');
    doc.text('LIBRERIA AMANECER', 105, 18, { align: 'center' });
    doc.setFontSize(9);
    doc.setFont('helvetica', 'normal');
    doc.text('RUC: 20123456789', 105, 24, { align: 'center' });
    doc.text('Jr. Los Libros 123 Juliaca - Puno', 105, 29, { align: 'center' });
    doc.text('Tel: 999-888-777', 105, 34, { align: 'center' });

    // Línea separadora azul
    doc.setDrawColor(37, 99, 235);
    doc.setLineWidth(0.5);
    doc.line(14, 38, 196, 38);

    // Título
    doc.setFontSize(13);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(31, 41, 55);
    doc.text('RECIBO DE SERVICIO', 105, 46, { align: 'center' });

    // Datos del recibo
    doc.setFontSize(10);
    doc.setFont('helvetica', 'normal');
    doc.text(`N. Recibo: RS-${s.id}`, 14, 56);
    doc.text(`Fecha: ${s.fecha}   Hora: ${s.hora}`, 14, 63);
    doc.text(`Cliente: ${s.cliente}`, 14, 70);

    // Cabecera tabla azul
    doc.setFillColor(37, 99, 235);
    doc.rect(14, 76, 182, 8, 'F');
    doc.setTextColor(255, 255, 255);
    doc.setFont('helvetica', 'bold');
    doc.setFontSize(9);
    doc.text('Servicio',  16,  81.5);
    doc.text('Cantidad',  95,  81.5);
    doc.text('P. Unit.', 130,  81.5);
    doc.text('Total',    165,  81.5);

    // Fila de datos
    doc.setTextColor(31, 41, 55);
    doc.setFont('helvetica', 'normal');
    doc.setFillColor(239, 246, 255);
    doc.rect(14, 84, 182, 8, 'F');
    doc.text(s.tipoServicio.substring(0, 38),       16,  89.5);
    doc.text(String(s.cantidad),                    95,  89.5);
    doc.text(`S/ ${s.precioUnitario.toFixed(2)}`,  130,  89.5);
    doc.text(`S/ ${s.total.toFixed(2)}`,           165,  89.5);

    // Total en negrita
    doc.setFontSize(11);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(31, 41, 55);
    doc.text(`Total a pagar: S/ ${s.total.toFixed(2)}`, 196, 102, { align: 'right' });

    // Método de pago
    doc.setFontSize(9);
    doc.setFont('helvetica', 'normal');
    const metodoLabel = s.metodoPago === 'YAPE_PLIN'
      ? 'YAPE/PLIN'
      : s.metodoPago === 'TRANSFERENCIA'
        ? 'TRANSFERENCIA BCP'
        : 'EFECTIVO';
    doc.text(`Metodo de pago: ${metodoLabel}`, 14, 110);

    // Línea pie
    doc.setDrawColor(37, 99, 235);
    doc.line(14, 116, 196, 116);

    // Pie de página
    doc.setFontSize(9);
    doc.setTextColor(107, 114, 128);
    doc.text('Gracias por su preferencia - Libreria Amanecer', 105, 123, { align: 'center' });

    // Descargar
    const blob = doc.output('blob');
    const url  = URL.createObjectURL(blob);
    const a    = document.createElement('a');
    a.href     = url;
    a.download = `recibo-servicio-${s.id}.pdf`;
    a.click();
    URL.revokeObjectURL(url);
  }
}
