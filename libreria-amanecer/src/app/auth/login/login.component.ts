/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import Swal from 'sweetalert2';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './login.component.html',
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  form = this.fb.group({
    userName: ['', [Validators.required, Validators.minLength(3)]],
    password: ['', [Validators.required, Validators.minLength(4)]],
  });
  loading = false;
  showPassword = false;

  constructor() {
    if (this.auth.isLoggedIn()) this.router.navigate(['/dashboard']);
  }

  submit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading = true;
    this.auth.login(this.form.value as any).subscribe({
      next: () => {
        this.loading = false;
        Swal.fire({
          icon: 'success',
          title: `¡Bienvenido, ${this.auth.getUsuario()}!`,
          text: `Rol: ${this.auth.getRolLabel()}`,
          timer: 1500,
          showConfirmButton: false
        });
        setTimeout(() => this.router.navigate(['/dashboard']), 1600);
      },
      error: (err) => {
        this.loading = false;
        let msg = 'No se pudo conectar con el servidor. Verifica que los servicios estén corriendo.';
        if (err.status === 400 || err.status === 401) {
          msg = 'Usuario o contraseña incorrectos.';
        } else if (err.status === 503 || err.status === 0) {
          msg = 'El servidor no está disponible. Verifica que ms-auth y el gateway estén activos.';
        }
        Swal.fire({ icon: 'error', title: 'Error de acceso', text: msg });
      }
    });
  }

  get f() { return this.form.controls; }
}
