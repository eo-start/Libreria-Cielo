/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
import { Component, HostListener, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, RouterLinkActive } from '@angular/router';
import Swal from 'sweetalert2';
import { AuthService } from '../../services/auth.service';

interface NavItem {
  label: string;
  icon: string;
  route: string;
  roles: string[];
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule, RouterLinkActive],
  templateUrl: './sidebar.component.html',
})
export class SidebarComponent implements OnInit {
  collapsed = false;
  isMobile  = false;

  private A  = AuthService.ROL_ADMIN;
  private C  = AuthService.ROL_CAJERO;
  private AL = AuthService.ROL_ALMACEN;

  navItems: NavItem[] = [
    { label: 'Dashboard',        icon: 'fas fa-tachometer-alt', route: '/dashboard',       roles: [this.A, this.C, this.AL] },
    { label: 'Productos',        icon: 'fas fa-box-open',       route: '/productos',        roles: [this.A, this.C, this.AL] },
    { label: 'Ventas',           icon: 'fas fa-shopping-cart',  route: '/ventas',           roles: [this.A, this.C] },
    { label: 'Historial Ventas', icon: 'fas fa-history',        route: '/historial-ventas', roles: [this.A, this.C] },
    { label: 'Servicios',        icon: 'fas fa-concierge-bell', route: '/servicios',        roles: [this.A, this.C] },
    { label: 'Inventario',       icon: 'fas fa-warehouse',      route: '/inventario',       roles: [this.A, this.AL] },
    { label: 'Clientes',         icon: 'fas fa-users',          route: '/clientes',         roles: [this.A, this.C] },
    { label: 'Usuarios',         icon: 'fas fa-user-cog',       route: '/usuarios',         roles: [this.A] },
    { label: 'Reportes',         icon: 'fas fa-chart-bar',      route: '/reportes',         roles: [this.A] },
  ];

  constructor(public auth: AuthService) {}

  ngOnInit() {
    this.checkMobile();
    const saved = localStorage.getItem('sidebarCollapsed');
    if (saved !== null) {
      this.collapsed = saved === 'true';
    } else {
      this.collapsed = this.isMobile;
    }
  }

  @HostListener('window:resize')
  checkMobile() {
    const wasMobile = this.isMobile;
    this.isMobile = window.innerWidth < 768;
    if (this.isMobile && !wasMobile) {
      this.collapsed = true;
      localStorage.setItem('sidebarCollapsed', 'true');
    }
  }

  toggle() {
    this.collapsed = !this.collapsed;
    localStorage.setItem('sidebarCollapsed', String(this.collapsed));
  }

  closeMobile() {
    if (this.isMobile && !this.collapsed) {
      this.collapsed = true;
      localStorage.setItem('sidebarCollapsed', 'true');
    }
  }

  get visibleItems(): NavItem[] {
    const rol = this.auth.getRol();
    return this.navItems.filter(item => item.roles.includes(rol));
  }

  logout() {
    Swal.fire({
      title: '¿Cerrar sesión?',
      text: '¿Estás seguro que deseas salir del sistema?',
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: 'Sí, salir',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#2563eb',
      cancelButtonColor: '#6b7280',
    }).then(r => { if (r.isConfirmed) this.auth.logout(); });
  }
}
