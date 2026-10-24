/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of , throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { Venta } from '../models';
import { environment } from '../../environments/environment';

//
// FLUJO DE VENTA:
//  1. Por cada item del carrito local: POST /carrito/agregar { libroId, cantidad }
//  2. POST /venta/realizar (sin body — lee el carrito del usuario por su token JWT)
//  3. La venta exitosa limpia el carrito del backend automáticamente
//
// Esquema real de GET /venta/listar:
//  { id, fecha (Date), userId, total (=subtotal), igv, totalConIgv, userName, detalles[] }
//  detalles: { id, libroId, cantidad, precio, libro?: { id, titulo, autor, stock, precio } }

interface VentaDetalleBackend {
  id: number;
  libroId: number;
  cantidad: number;
  precio: number;
  nombreProducto?: string;
  libro?: { id: number; titulo: string; autor: string; stock: number; precio: number };
}

interface VentaBackend {
  id: number;
  fecha: string | number;
  userId: number;
  total: number;        // subtotal before IGV
  igv: number;
  totalConIgv: number;  // total including IGV
  userName: string;
  detalles: VentaDetalleBackend[];
  metodoPago?: Venta['metodoPago'];
  estado?: Venta['estado'];
  clienteId?: number;
}

function mapVenta(v: VentaBackend): Venta {
  const d = new Date(v.fecha);
  const valid = !isNaN(d.getTime());
  const fecha = valid ? d.toISOString().split('T')[0] : String(v.fecha).split('T')[0];
  const hora  = valid ? d.toLocaleTimeString('es-PE', { hour: '2-digit', minute: '2-digit' }) : '--:--';

  return {
    id:            v.id,
    numeroVenta:   `V-${String(v.id).padStart(3, '0')}`,
    clienteId:     v.clienteId ?? v.userId,
    clienteNombre: v.userName || 'Sin nombre',
    fecha,
    hora,
    metodoPago:    v.metodoPago || 'EFECTIVO',
    subtotal:      v.total,
    igv:           v.igv,
    total:         v.totalConIgv,
    estado:        v.estado || 'COMPLETADA',
    detalles: (v.detalles || []).map(det => ({
      productoId:      det.libroId,
      codigo:          `PRD-${String(det.libroId).padStart(3, '0')}`,
      producto:        det.nombreProducto || det.libro?.titulo || `Producto #${det.libroId}`,
      cantidad:        det.cantidad,
      precioUnitario:  det.precio,
      subtotal:        det.precio * det.cantidad,
    })),
  };
}

@Injectable({ providedIn: 'root' })
export class VentaService {
  private base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // ── Carrito backend ──────────────────────────────────────────────────────────

  // POST /carrito/agregar  → devuelve { id, userId, libroId, cantidad }
  agregarAlCarrito(libroId: number, cantidad: number): Observable<{ id: number }> {
    return this.http.post<{ id: number }>(`${this.base}/carrito/agregar`, { libroId, cantidad });
  }

  // DELETE /carrito/eliminar/{id}
  eliminarDelCarrito(itemId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/carrito/eliminar/${itemId}`);
  }

  // GET /carrito/listar
  getCarrito(): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/carrito/listar`).pipe(
      catchError(error => throwError(() => error))
    );
  }

  // ── Ventas ───────────────────────────────────────────────────────────────────

  // POST /venta/realizar — sin body; el backend lee el carrito del usuario por JWT.
  // clienteNombre se pasa como @RequestParam para que el backend lo guarde como nombre real del cliente.
  realizarVenta(clienteNombre:string,metodoPago:string='EFECTIVO',nroOperacion:string='',clienteId?:number):Observable<any>{
    const key=`checkout:${localStorage.getItem('usuario')}`;
    let operation=sessionStorage.getItem(key);
    if(!operation){operation=crypto.randomUUID();sessionStorage.setItem(key,operation);}
    const params:any={clienteNombre,metodoPago};
    if(nroOperacion)params.nroOperacion=nroOperacion;
    if(clienteId)params.clienteId=clienteId;
    return this.http.post(`${this.base}/venta/realizar`,null,{params,headers:{'Idempotency-Key':operation}}).pipe(
      map(v=>{sessionStorage.removeItem(key);return v;}),
      catchError(error=>{if(error.status===400||error.status===409)sessionStorage.removeItem(key);return throwError(()=>error);})
    );
  }
  getPendiente():Observable<any>{return this.http.get(`${this.base}/venta/pendiente`);}
  reintentar(id:number):Observable<any>{return this.http.post(`${this.base}/venta/${id}/reintentar`,null).pipe(map(v=>{sessionStorage.removeItem(`checkout:${localStorage.getItem('usuario')}`);return v;}));}

  // GET /venta/listar — mapea respuesta real del backend al modelo Venta del frontend
  getAll(): Observable<Venta[]> {
    return this.http.get<VentaBackend[]>(`${this.base}/venta/listar`).pipe(
      map(ventas => ventas.map(mapVenta)),
      catchError(error => throwError(() => error))
    );
  }

  // GET /venta/productos-mas-vendidos
  getProductosMasVendidos(): Observable<{ posicion: number; producto: string; cantidadVendida: number; total: number }[]> {
    return this.http.get<any[]>(`${this.base}/venta/productos-mas-vendidos`).pipe(
      catchError(error => throwError(() => error))
    );
  }

  // GET /venta/{id}
  getById(id: number): Observable<Venta> {
    return this.http.get<VentaBackend>(`${this.base}/venta/${id}`).pipe(
      map(mapVenta)
    );
  }

  // GET /venta/{id}/recibo → PDF blob
  getReciboPdf(id: number): Observable<Blob> {
    return this.http.get(`${this.base}/venta/${id}/recibo`, { responseType: 'blob' });
  }

  // PUT /libros/{id}/inventario?cantidad=N&tipo=SALIDA
  registrarSalidaInventario(libroId: number, cantidad: number): Observable<any> {
    const params = new HttpParams()
      .set('cantidad', String(cantidad))
      .set('tipo', 'SALIDA');
    return this.http.put(`${this.base}/libros/${libroId}/inventario`, null, { params }).pipe(
      catchError(error => throwError(() => error))
    );
  }
}
