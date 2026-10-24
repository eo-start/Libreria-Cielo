/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of , throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ReporteData } from '../models';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ReporteService {
  private base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // GET /reportes/general?fechaInicio=&fechaFin= → pendiente en backend
  getGeneral(fechaInicio: string, fechaFin: string): Observable<ReporteData> {
    return this.http.get<ReporteData>(`${this.base}/reportes/general`, {
      params: { fechaInicio, fechaFin }
    }).pipe(catchError(error => throwError(() => error)));
  }

  // GET /reportes/ventas → pendiente en backend
  getVentas(params: any): Observable<any> {
    return this.http.get(`${this.base}/reportes/ventas`, { params }).pipe(
      catchError(error => throwError(() => error))
    );
  }

  // GET /reportes/pdf → pendiente en backend
  getPdf(params: any): Observable<Blob> {
    return this.http.get(`${this.base}/reportes/pdf`, {
      params, responseType: 'blob'
    });
  }
}
