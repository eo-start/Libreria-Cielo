/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of , throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { Producto } from '../models';
import { environment } from '../../environments/environment';

// Conecta con: ms-book via API Gateway
// El backend usa multipart/form-data con campos: titulo, autor, stock, precio,
// categoriaId, provedoresId, anio, imagen (archivo).
// GET /libros devuelve objetos Libro con categoria y provedores anidados.

interface LibroBackend {
  id: number;
  codigo?: string;
  titulo: string;
  autor: string;
  stock: number;
  stockMinimo?: number;
  precio: number;
  anio: string;
  imagenUrl?: string;
  estado?: string;
  categoria?: { id: number; nombre: string };
  provedores?: { id: number; nombre: string };
}

export interface Categoria {
  id: number;
  nombre: string;
}

function mapLibro(l: LibroBackend): Producto {
  const stockMinimo = l.stockMinimo ?? 10;

  // Si el backend devuelve estado explícito (ACTIVO / INACTIVO), lo usamos.
  // Si no, lo derivamos del stock.
  let estado: 'ACTIVO' | 'INACTIVO' | 'STOCK_BAJO';
  if (l.estado === 'INACTIVO') {
    estado = 'INACTIVO';
  } else if (l.stock <= 0) {
    estado = 'INACTIVO';
  } else if (l.stock < stockMinimo) {
    estado = 'STOCK_BAJO';
  } else {
    estado = 'ACTIVO';
  }

  return {
    id: l.id,
    codigo: l.codigo || `PRD-${String(l.id).padStart(3, '0')}`,
    nombre: l.titulo,
    categoria: l.categoria?.nombre || 'Sin categoría',
    precio: l.precio,
    stock: l.stock,
    stockMinimo,
    estado,
    imagenUrl: l.imagenUrl,
  };
}

@Injectable({ providedIn: 'root' })
export class ProductoService {
  private base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // GET /libros
  getAll(): Observable<Producto[]> {
    return this.http.get<LibroBackend[]>(`${this.base}/libros`).pipe(
      map(libros => libros.map(mapLibro)),
      catchError(error => throwError(() => error))
    );
  }

  // GET /categoria/listar (fallback: /categoria)
  getCategorias(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(`${this.base}/categoria/listar`).pipe(
      catchError(() => this.http.get<Categoria[]>(`${this.base}/categoria`)),
      catchError(error => throwError(() => error))
    );
  }

  // POST /libros  — multipart/form-data requerido por el backend
  create(formData: FormData): Observable<Producto> {
    return this.http.post<LibroBackend>(`${this.base}/libros`, formData).pipe(
      map(mapLibro)
    );
  }

  // PUT /libros/{id}  — multipart/form-data requerido por el backend
  update(id: number, formData: FormData): Observable<Producto> {
    return this.http.put<LibroBackend>(`${this.base}/libros/${id}`, formData).pipe(
      map(mapLibro)
    );
  }

  // DELETE /libros/{id}
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/libros/${id}`);
  }
}
