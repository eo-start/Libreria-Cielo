/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { roleGuard } from './guards/role.guard';

const ADMIN         = 'ROLE_ADMIN';
const CAJERO        = 'ROLE_CAJERO';
const ALMACEN       = 'ROLE_ALMACEN';
const ADMIN_CAJERO  = [ADMIN, CAJERO];
const ADMIN_ALMACEN = [ADMIN, ALMACEN];
const ALL_ROLES     = [ADMIN, CAJERO, ALMACEN];

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    canActivate: [authGuard, roleGuard], data: { roles: [ADMIN] },
    loadComponent: () => import('./auth/register/register.component').then(m => m.RegisterComponent)
  },
  {
    path: '',
    loadComponent: () => import('./layout/main-layout/main-layout.component').then(m => m.MainLayoutComponent),
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./pages/dashboard/dashboard.component').then(m => m.DashboardComponent),
        canActivate: [roleGuard], data: { roles: ALL_ROLES }
      },
      {
        path: 'productos',
        loadComponent: () => import('./pages/productos/productos.component').then(m => m.ProductosComponent),
        canActivate: [roleGuard], data: { roles: ALL_ROLES }
      },
      {
        path: 'ventas',
        loadComponent: () => import('./pages/ventas/ventas.component').then(m => m.VentasComponent),
        canActivate: [roleGuard], data: { roles: ADMIN_CAJERO }
      },
      {
        path: 'historial-ventas',
        loadComponent: () => import('./pages/historial-ventas/historial-ventas.component').then(m => m.HistorialVentasComponent),
        canActivate: [roleGuard], data: { roles: ADMIN_CAJERO }
      },
      {
        path: 'servicios',
        loadComponent: () => import('./pages/servicios/servicios.component').then(m => m.ServiciosComponent),
        canActivate: [roleGuard], data: { roles: ADMIN_CAJERO }
      },
      {
        path: 'inventario',
        loadComponent: () => import('./pages/inventario/inventario.component').then(m => m.InventarioComponent),
        canActivate: [roleGuard], data: { roles: ADMIN_ALMACEN }
      },
      {
        path: 'clientes',
        loadComponent: () => import('./pages/clientes/clientes.component').then(m => m.ClientesComponent),
        canActivate: [roleGuard], data: { roles: ADMIN_CAJERO }
      },
      {
        path: 'usuarios',
        loadComponent: () => import('./pages/usuarios/usuarios.component').then(m => m.UsuariosComponent),
        canActivate: [roleGuard], data: { roles: [ADMIN] }
      },
      {
        path: 'reportes',
        loadComponent: () => import('./pages/reportes/reportes.component').then(m => m.ReportesComponent),
        canActivate: [roleGuard], data: { roles: [ADMIN] }
      },
      { path: '**', redirectTo: 'dashboard' }
    ]
  }
];
