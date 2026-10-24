/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of , throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { Servicio } from '../models';
import { environment } from '../../environments/environment';

// Conecta con ms-venta via API Gateway
// POST /servicio/guardar
// GET  /servicio/listar

interface ServicioBackend {
  id: number;
  tipoServicio: string;
  cliente: string;
  cantidad: number;
  precioUnitario: number;
  total: number;
  metodoPago?: string;
  fecha: string | number;
}

function mapServicio(s: ServicioBackend): Servicio {
  const d     = new Date(s.fecha);
  const valid = !isNaN(d.getTime());
  return {
    id:             s.id,
    tipoServicio:   s.tipoServicio,
    cliente:        s.cliente,
    cantidad:       s.cantidad,
    precioUnitario: s.precioUnitario,
    total:          s.total,
    metodoPago:     s.metodoPago || 'EFECTIVO',
    fecha: valid ? d.toISOString().split('T')[0] : String(s.fecha).split('T')[0],
    hora:  valid ? d.toLocaleTimeString('es-PE', { hour: '2-digit', minute: '2-digit' }) : '--:--',
  };
}

@Injectable({ providedIn: 'root' })
export class ServicioService {
  private base = environment.apiUrl;

  readonly TIPOS_SERVICIO = [
    { nombre: 'Fotocopiado',  precioBase: 0.10,  icono: 'fa-copy'      },
    { nombre: 'Impresión',    precioBase: 0.50,  icono: 'fa-print'     },
    { nombre: 'Anillado',     precioBase: 5.00,  icono: 'fa-book-open' },
    { nombre: 'Encuadernado', precioBase: 15.00, icono: 'fa-book'      },
  ];

  constructor(private http: HttpClient) {}

  // GET /servicio/listar
  getAll(): Observable<Servicio[]> {
    return this.http.get<ServicioBackend[]>(`${this.base}/servicio/listar`).pipe(
      map(list => list.map(mapServicio)),
      catchError(error => throwError(() => error))
    );
  }

  // POST /servicio/guardar
  registrar(data: { tipoServicio: string; cliente: string; cantidad: number; precioUnitario: number; total: number; metodoPago: string }): Observable<Servicio> {
    return this.http.post<ServicioBackend>(`${this.base}/servicio/guardar`, data).pipe(
      map(mapServicio)
    );
  }
}
