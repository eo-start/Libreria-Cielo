/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of , throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { StockInventario, MovimientoInventario } from '../models';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class InventarioService {
  private base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // GET /libros → mapea a StockInventario
  getStock(): Observable<StockInventario[]> {
    return this.http.get<any[]>(`${this.base}/libros`).pipe(
      map(libros => libros.map(l => this.mapLibroToStock(l))),
      catchError(error => throwError(() => error))
    );
  }

  getStockBajo(): Observable<StockInventario[]> {
    return this.getStock().pipe(
      map(items => items.filter(s => s.estado !== 'NORMAL'))
    );
  }

  // PUT /libros/{id}/inventario?cantidad=N&tipo=ENTRADA
  registrarEntrada(data: { productoId: number; cantidad: number; notas: string }): Observable<any> {
    const params = new HttpParams()
      .set('cantidad', String(data.cantidad))
      .set('tipo', 'ENTRADA');
    return this.http.put(`${this.base}/libros/${data.productoId}/inventario`, null, { params });
  }

  // PUT /libros/{id}/inventario?cantidad=N&tipo=SALIDA
  registrarSalida(data: { productoId: number; cantidad: number; notas: string }): Observable<any> {
    const params = new HttpParams()
      .set('cantidad', String(data.cantidad))
      .set('tipo', 'SALIDA');
    return this.http.put(`${this.base}/libros/${data.productoId}/inventario`, null, { params });
  }

  // POST /libros/movimiento/guardar
  guardarMovimiento(data: {
    libroId: number;
    nombreProducto: string;
    tipo: string;
    cantidad: number;
    notas: string;
    usuario: string;
  }): Observable<MovimientoInventario> {
    return this.http.post<any>(`${this.base}/libros/movimiento/guardar`, data).pipe(
      map(m => this.mapMovimiento(m)),
      catchError(() => of({} as MovimientoInventario))
    );
  }

  // GET /libros/movimiento/listar — últimos 10 movimientos
  getMovimientos(): Observable<MovimientoInventario[]> {
    return this.http.get<any[]>(`${this.base}/libros/movimiento/listar`).pipe(
      map(list => list.map(m => this.mapMovimiento(m))),
      catchError(error => throwError(() => error))
    );
  }

  private mapMovimiento(m: any): MovimientoInventario {
    const d = new Date(m.fecha);
    const valid = !isNaN(d.getTime());
    return {
      id:             m.id,
      productoId:     m.libroId,
      producto:       m.nombreProducto || '',
      tipoMovimiento: (m.tipo as 'ENTRADA' | 'SALIDA' | 'AJUSTE') || 'ENTRADA',
      cantidad:       m.cantidad,
      notas:          m.notas || '',
      fecha:          valid ? d.toISOString().split('T')[0] : String(m.fecha || '').split('T')[0],
      usuario:        m.usuario || '',
    };
  }

  private mapLibroToStock(l: any): StockInventario {
    const stockActual = l.stock    ?? 0;
    const stockMinimo = l.stockMinimo ?? 10;
    let estado: 'NORMAL' | 'BAJO_STOCK' | 'REPONER';

    if (stockActual <= 0)              estado = 'REPONER';
    else if (stockActual < stockMinimo) estado = 'BAJO_STOCK';
    else                               estado = 'NORMAL';

    return {
      productoId: l.id,
      codigo:     l.codigo  || `PRD-${String(l.id).padStart(3, '0')}`,
      producto:   l.titulo  || l.nombre || '',
      stockActual,
      stockMinimo,
      estado,
    };
  }
}
