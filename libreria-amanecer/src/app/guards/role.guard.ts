/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
import { inject } from '@angular/core';
import { CanActivateFn, ActivatedRouteSnapshot, Router } from '@angular/router';
import Swal from 'sweetalert2';

export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const router = inject(Router);
  const rol = localStorage.getItem('rol') || '';
  const allowedRoles: string[] = route.data['roles'] || [];

  if (allowedRoles.length === 0 || allowedRoles.includes(rol)) return true;

  Swal.fire({
    icon: 'warning',
    title: 'Acceso denegado',
    text: 'No tienes permiso para acceder a esta sección.',
    confirmButtonText: 'Entendido',
    confirmButtonColor: '#2563eb',
    timer: 3000,
  });
  router.navigate(['/dashboard']);
  return false;
};
