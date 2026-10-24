/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import Swal from 'sweetalert2';
export const tokenInterceptor: HttpInterceptorFn = (req, next) => {
 const router=inject(Router);
 if(!req.url.startsWith('/api/')) return next(req);
 const token=localStorage.getItem('token');
 const request=token&&!req.url.endsWith('/auth/login')?req.clone({setHeaders:{Authorization:`Bearer ${token}`}}):req;
 return next(request).pipe(catchError((error:HttpErrorResponse)=>{
  if(error.status===401&&token){
   localStorage.removeItem('token');localStorage.removeItem('usuario');localStorage.removeItem('rol');
   router.navigate(['/login']);
  }
  if(!req.url.endsWith('/auth/login')) Swal.fire({icon:'error',title:error.status===403?'Acceso no autorizado':'No se pudo completar la operación',text:error.error?.message||'El servicio no está disponible. Reintenta en unos momentos.'});
  return throwError(()=>error);
 }));
};
