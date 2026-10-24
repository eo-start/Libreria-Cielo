/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Usuario } from '../models';
import { environment } from '../../environments/environment';
@Injectable({providedIn:'root'})
export class UsuarioService {
 private base=environment.apiUrl;
 constructor(private http:HttpClient){}
 getAll():Observable<Usuario[]>{return this.http.get<any[]>(`${this.base}/auth/usuarios`).pipe(map(rows=>rows.map(u=>({id:u.id,nombre:u.userName,usuario:u.userName,rol:u.rol,estado:'ACTIVO' as const}))));}
 create(u:{userName:string;password:string;rol:string}):Observable<any>{return this.http.post(`${this.base}/auth/create`,u);}
 delete(id:number):Observable<void>{return this.http.delete<void>(`${this.base}/auth/usuarios/${id}`);}
}
