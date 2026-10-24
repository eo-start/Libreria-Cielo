/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import Swal from 'sweetalert2';
import { ClienteService } from '../../services/cliente.service';
import { Cliente } from '../../models';

@Component({
  selector: 'app-clientes',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './clientes.component.html',
})
export class ClientesComponent implements OnInit {
  private fb  = inject(FormBuilder);
  private svc = inject(ClienteService);

  clientes:  Cliente[] = [];
  filtrados: Cliente[] = [];
  busqueda     = '';
  filtroEstado = '';
  loading   = true;
  showModal = false;
  editando: Cliente | null = null;

  // Solo los campos que acepta el backend: nombreCompleto, dni, telefono, direccion
  form = this.fb.group({
    nombreCompleto: ['', [Validators.required, Validators.minLength(5)]],
    dni:            ['', [Validators.required, Validators.minLength(8), Validators.maxLength(8)]],
    telefono:       [''],
    direccion:      [''],
  });

  constructor() {}

  ngOnInit() { this.cargar(); }

  cargar() {
    this.loading = true;
    this.svc.getAll().subscribe(data => {
      this.clientes = data;
      this.aplicarFiltros();
      this.loading = false;
    });
  }

  aplicarFiltros() {
    this.filtrados = this.clientes.filter(c => {
      const q = this.busqueda.toLowerCase();
      const match = !q
        || c.nombreCompleto.toLowerCase().includes(q)
        || c.dni.includes(q)
        || (c.telefono || '').includes(q)
        || (c.codigo || '').toLowerCase().includes(q);
      const matchEstado = !this.filtroEstado || c.estado === this.filtroEstado;
      return match && matchEstado;
    });
  }

  abrirModal(c?: Cliente) {
    this.editando = c || null;
    if (c) {
      this.form.patchValue({ nombreCompleto: c.nombreCompleto, dni: c.dni, telefono: c.telefono, direccion: c.direccion });
    } else {
      this.form.reset();
    }
    this.showModal = true;
  }

  cerrarModal() { this.showModal = false; this.editando = null; }

  guardar() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const v = this.form.value;
    const payload = {
      nombreCompleto: v.nombreCompleto!,
      dni:            v.dni!,
      telefono:       v.telefono || '',
      direccion:      v.direccion || '',
    };
    const obs = this.editando
      ? this.svc.update(this.editando.id, payload)
      : this.svc.create(payload);

    obs.subscribe({
      next: () => {
        Swal.fire({ icon: 'success', title: this.editando ? 'Cliente actualizado' : 'Cliente registrado', timer: 1500, showConfirmButton: false });
        this.cerrarModal();
        this.cargar();
      },
      error: (err) => Swal.fire({ icon: 'error', title: 'Error al guardar', text: err.error?.message || 'Verifica los datos.' })
    });
  }

  toggleEstado(c: Cliente) {
    const nuevo = c.estado === 'ACTIVO' ? 'INACTIVO' : 'ACTIVO';
    this.svc.update(c.id, { estado: nuevo }).subscribe({
      next: () => { c.estado = nuevo as any; Swal.fire({ icon: 'success', title: `Cliente ${nuevo.toLowerCase()}`, timer: 1200, showConfirmButton: false }); },
      error: () => { c.estado = nuevo as any; }
    });
  }

  getBadge(e: string): string { return e === 'ACTIVO' ? 'badge badge-green' : 'badge badge-red'; }
  get f() { return this.form.controls; }

  soloLetras(event: KeyboardEvent): void {
    const char = event.key;
    if (char.length > 1) return;
    if (/[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\s]/.test(char)) return;
    event.preventDefault();
  }

  soloDigitos(event: KeyboardEvent): void {
    const char = event.key;
    if (char.length > 1) return;
    if (/[0-9]/.test(char)) return;
    event.preventDefault();
  }
}
