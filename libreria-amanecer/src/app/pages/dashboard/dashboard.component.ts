/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { VentaService } from '../../services/venta.service';
import { ProductoService } from '../../services/producto.service';
import { InventarioService } from '../../services/inventario.service';
import { ServicioService } from '../../services/servicio.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent implements OnInit {
  stats = { ventasHoy: 0, productosActivos: 0, serviciosHoy: 0, stockBajo: 0 };
  ventasRecientes: any[] = [];
  alertasStock: any[] = [];
  loading = true;

  ventasPorDia: {dia:string;valor:number}[] = [];
  maxVenta = 1;

  constructor(
    public auth: AuthService,
    private ventaService: VentaService,
    private productoService: ProductoService,
    private inventarioService: InventarioService,
    private servicioService: ServicioService,
  ) {}

  ngOnInit() {
    this.cargarDatos();
  }

  cargarDatos() {
    this.ventaService.getAll().subscribe(ventas => {
      const hoy = new Date().toISOString().split('T')[0];
      const completadas=ventas.filter(v=>v.estado==='COMPLETADA');
      const ventasHoy = completadas.filter(v => v.fecha === hoy);
      const totals=new Map<string,number>();
      completadas.forEach(v=>totals.set(v.fecha,(totals.get(v.fecha)||0)+v.total));
      this.ventasPorDia=Array.from(totals,([dia,valor])=>({dia,valor})).sort((a,b)=>a.dia.localeCompare(b.dia)).slice(-7);
      this.maxVenta=Math.max(1,...this.ventasPorDia.map(v=>v.valor));
      this.stats.ventasHoy = ventasHoy.reduce((s, v) => s + v.total, 0);
      this.ventasRecientes = ventas.slice(0, 5);
    });
    this.productoService.getAll().subscribe(productos => {
      this.stats.productosActivos = productos.filter(p => p.estado === 'ACTIVO').length;
    });
    this.inventarioService.getStockBajo().subscribe(stock => {
      this.stats.stockBajo = stock.length;
      this.alertasStock = stock.slice(0, 4);
      this.loading = false;
    });
    this.servicioService.getAll().subscribe(s => {
      const hoy = new Date().toISOString().split('T')[0];
      this.stats.serviciosHoy = s.filter(sv => sv.fecha === hoy).length;
    });
  }

  barHeight(valor: number): number {
    return this.maxVenta > 0 ? Math.round((valor / this.maxVenta) * 100) : 0;
  }

  getEstadoBadge(estado: string): string {
    const map: Record<string, string> = {
      COMPLETADA: 'badge badge-green', CANCELADA: 'badge badge-red', PENDIENTE: 'badge badge-yellow'
    };
    return map[estado] || 'badge badge-gray';
  }

  getStockBadge(estado: string): string {
    const map: Record<string, string> = {
      NORMAL: 'badge badge-green', BAJO_STOCK: 'badge badge-yellow', REPONER: 'badge badge-red'
    };
    return map[estado] || 'badge badge-gray';
  }
}
