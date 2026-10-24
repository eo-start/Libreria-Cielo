/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of , throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { Cliente } from '../models';
import { environment } from '../../environments/environment';

// Backend usa: nombreCompleto, dni, telefono, direccion (POST /cliente)
// La respuesta incluye: id, codigo, nombreCompleto, dni, telefono, correo, direccion, estado
@Injectable({ providedIn: 'root' })
export class ClienteService {
  private base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // GET /cliente
  getAll(): Observable<Cliente[]> {
    return this.http.get<Cliente[]>(`${this.base}/cliente`).pipe(
      catchError(error => throwError(() => error))
    );
  }

  // GET /cliente/{id}
  getById(id: number): Observable<Cliente> {
    return this.http.get<Cliente>(`${this.base}/cliente/${id}`);
  }

  // POST /cliente — envía solo los campos que acepta el backend
  create(c: Pick<Cliente, 'nombreCompleto' | 'dni' | 'telefono' | 'direccion'>): Observable<Cliente> {
    return this.http.post<Cliente>(`${this.base}/cliente`, {
      nombreCompleto: c.nombreCompleto,
      dni:            c.dni,
      telefono:       c.telefono,
      direccion:      c.direccion,
      // estado y codigo los genera el backend automáticamente
    });
  }

  // PUT /cliente/{id}
  update(id: number, c: Partial<Cliente>): Observable<Cliente> {
    return this.http.put<Cliente>(`${this.base}/cliente/${id}`, {
      nombreCompleto: c.nombreCompleto,
      dni:            c.dni,
      telefono:       c.telefono,
      direccion:      c.direccion,
    });
  }

  // DELETE /cliente/{id}
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/cliente/${id}`);
  }
}
