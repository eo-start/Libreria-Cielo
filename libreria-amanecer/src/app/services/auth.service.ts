/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { LoginRequest, LoginResponse, RegisterRequest } from '../models';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private base = environment.apiUrl;

  static readonly ROL_ADMIN   = 'ROLE_ADMIN';
  static readonly ROL_CAJERO  = 'ROLE_CAJERO';
  static readonly ROL_ALMACEN = 'ROLE_ALMACEN';

  constructor(private http: HttpClient, private router: Router) {}

  login(data: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.base}/auth/login`, data).pipe(
      tap(res => {
        if (!res?.token) {
          console.error('[Auth] Login: respuesta sin token', res);
          return;
        }
        localStorage.setItem('token', res.token);
        const payload = this.decodeJwt(res.token);

        if (payload) {
          localStorage.setItem('usuario', payload['sub'] || '');
          localStorage.setItem('rol', payload['rol'] || '');
        }

      })
    );
  }

  register(data: RegisterRequest): Observable<any> {
    return this.http.post(`${this.base}/auth/create`, {
      userName: data.userName,
      password: data.password,
      rol: data.rol
    });
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('usuario');
    localStorage.removeItem('rol');
    this.router.navigate(['/login']);
  }

  private decodeJwt(token: string): Record<string, any> | null {
    try {
      return JSON.parse(atob(token.split('.')[1]));
    } catch {
      return null;
    }
  }

  getToken(): string | null { return localStorage.getItem('token'); }
  getUsuario(): string      { return localStorage.getItem('usuario') || ''; }
  getRol(): string          { return localStorage.getItem('rol') || ''; }
  isLoggedIn(): boolean { const token=this.getToken();const payload=token?this.decodeJwt(token):null;return !!payload&&Number(payload['exp'])*1000>Date.now(); }

  isAdmin(): boolean   { return this.getRol() === AuthService.ROL_ADMIN; }
  isCajero(): boolean  { return this.getRol() === AuthService.ROL_CAJERO; }
  isAlmacen(): boolean { return this.getRol() === AuthService.ROL_ALMACEN; }

  // Puede ver/acceder a algo (admin tiene todo)
  can(action: 'ventas' | 'historial' | 'servicios' | 'inventario' | 'clientes' | 'usuarios' | 'reportes' | 'productos.eliminar' | 'productos.crear'): boolean {
    const rol = this.getRol();
    const permisos: Record<string, string[]> = {
      'ventas':             [AuthService.ROL_ADMIN, AuthService.ROL_CAJERO],
      'historial':          [AuthService.ROL_ADMIN, AuthService.ROL_CAJERO],
      'servicios':          [AuthService.ROL_ADMIN, AuthService.ROL_CAJERO],
      'inventario':         [AuthService.ROL_ADMIN, AuthService.ROL_ALMACEN],
      'clientes':           [AuthService.ROL_ADMIN, AuthService.ROL_CAJERO],
      'usuarios':           [AuthService.ROL_ADMIN],
      'reportes':           [AuthService.ROL_ADMIN],
      'productos.eliminar': [AuthService.ROL_ADMIN],
      'productos.crear':    [AuthService.ROL_ADMIN],
    };
    return permisos[action]?.includes(rol) ?? false;
  }

  getRolLabel(): string {
    const map: Record<string, string> = {
      ROLE_ADMIN:   'Administrador',
      ROLE_CAJERO:  'Cajero',
      ROLE_ALMACEN: 'Almacén',
    };
    return map[this.getRol()] || this.getRol();
  }
}
