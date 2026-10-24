/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, AbstractControl } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import Swal from 'sweetalert2';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './register.component.html',
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  form = this.fb.group({
    nombreCompleto: ['', [Validators.required, Validators.minLength(5)]],
    userName: ['', [Validators.required, Validators.minLength(3)]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    confirmarPassword: ['', Validators.required],
    rol: ['ROLE_CAJERO', Validators.required],
  }, { validators: this.passwordMatch });
  loading = false;
  showPass = false;

  constructor() {}

  passwordMatch(group: AbstractControl) {
    const p = group.get('password')?.value;
    const c = group.get('confirmarPassword')?.value;
    return p === c ? null : { noMatch: true };
  }

  submit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading = true;
    const v = this.form.value;
    this.auth.register({ nombreCompleto: v.nombreCompleto!, userName: v.userName!, password: v.password!, rol: v.rol! }).subscribe({
      next: () => {
        this.loading = false;
        Swal.fire({ icon: 'success', title: '¡Registro exitoso!', text: 'Tu cuenta fue creada. Inicia sesión.', confirmButtonText: 'Ir al login' })
          .then(() => this.router.navigate(['/login']));
      },
      error: (err) => {
        this.loading = false;
        Swal.fire({ icon: 'error', title: 'Error al registrar', text: err.error?.message || 'No se pudo crear la cuenta.' });
      }
    });
  }

  get f() { return this.form.controls; }
}
