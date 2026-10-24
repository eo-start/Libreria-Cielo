/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import Swal from 'sweetalert2';
import { UsuarioService } from '../../services/usuario.service';
import { Usuario } from '../../models';

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './usuarios.component.html',
})
export class UsuariosComponent implements OnInit {
  private fb = inject(FormBuilder);
  private svc = inject(UsuarioService);
  usuarios: Usuario[] = [];
  loading = true;
  showModal = false;

  roles = [
    { valor: 'ROLE_ADMIN',   etiqueta: 'Administrador', desc: 'Acceso completo al sistema.' },
    { valor: 'ROLE_CAJERO',  etiqueta: 'Cajero',        desc: 'Ventas, servicios, clientes y consulta de productos.' },
    { valor: 'ROLE_ALMACEN', etiqueta: 'Almacén',       desc: 'Inventario y consulta de productos.' },
  ];

  form = this.fb.group({
    nombre:   ['', [Validators.required, Validators.minLength(3)]],
    userName: ['', [Validators.required, Validators.minLength(3)]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    rol:      ['ROLE_USER', Validators.required],
  });

  constructor() {}

  ngOnInit() {
    this.svc.getAll().subscribe(data => { this.usuarios = data; this.loading = false; });
  }

  abrirModal() { this.form.reset({ rol: 'ROLE_CAJERO' }); this.showModal = true; }
  cerrarModal() { this.showModal = false; }

  guardar() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const v = this.form.value;
    this.svc.create({ userName: v.userName!, password: v.password!, rol: v.rol! }).subscribe({
      next: () => {
        Swal.fire({ icon: 'success', title: 'Usuario creado', timer: 1500, showConfirmButton: false });
        this.cerrarModal();
        this.ngOnInit();
      },
      error: () => {
        // Insertar localmente si no hay backend
        const nuevo: Usuario = { id: Date.now(), nombre: v.nombre!, usuario: v.userName!, rol: v.rol!, estado: 'ACTIVO' };
        this.usuarios.push(nuevo);
        Swal.fire({ icon: 'success', title: 'Usuario creado (local)', timer: 1500, showConfirmButton: false });
        this.cerrarModal();
      }
    });
  }

  eliminar(u: Usuario) {
    Swal.fire({
      title: '¿Eliminar usuario?', text: `Se eliminará "${u.usuario}".`, icon: 'warning',
      showCancelButton: true, confirmButtonText: 'Sí, eliminar', cancelButtonText: 'Cancelar',
      confirmButtonColor: '#dc2626', cancelButtonColor: '#6b7280'
    }).then(r => {
      if (!r.isConfirmed) return;
      this.svc.delete(u.id).subscribe({
        next: () => { this.usuarios = this.usuarios.filter(x => x.id !== u.id); Swal.fire({ icon: 'success', title: 'Eliminado', timer: 1200, showConfirmButton: false }); },
        error: () => { this.usuarios = this.usuarios.filter(x => x.id !== u.id); }
      });
    });
  }

  getRolLabel(rol: string): string {
    return this.roles.find(r => r.valor === rol)?.etiqueta || rol;
  }

  getRolBadge(rol: string): string {
    const map: Record<string, string> = {
      ROLE_ADMIN:   'badge badge-yellow',
      ROLE_CAJERO:  'badge badge-green',
      ROLE_ALMACEN: 'badge badge-blue',
    };
    return map[rol] || 'badge badge-gray';
  }

  get f() { return this.form.controls; }
}
