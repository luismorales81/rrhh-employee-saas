import { Injectable } from '@angular/core';

import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';



@Injectable({ providedIn: 'root' })

export class ServicioOrganizacion {

  private readonly urlBase = `${environment.urlApi}/organizacion`;



  constructor(private readonly http: HttpClient) {}



  listarGerencias(): Observable<GerenciaResumen[]> {

    return this.http.get<GerenciaResumen[]>(`${this.urlBase}/gerencias`);

  }



  crearGerencia(solicitud: SolicitudCrearGerencia): Observable<GerenciaResumen> {

    return this.http.post<GerenciaResumen>(`${this.urlBase}/gerencias`, solicitud);

  }



  actualizarGerencia(id: number, solicitud: SolicitudActualizarGerencia): Observable<GerenciaResumen> {

    return this.http.put<GerenciaResumen>(`${this.urlBase}/gerencias/${id}`, solicitud);

  }



  eliminarGerencia(id: number): Observable<void> {

    return this.http.delete<void>(`${this.urlBase}/gerencias/${id}`);

  }



  listarAreas(): Observable<AreaResumen[]> {

    return this.http.get<AreaResumen[]>(`${this.urlBase}/areas`);

  }



  crearArea(solicitud: SolicitudCrearArea): Observable<AreaResumen> {

    return this.http.post<AreaResumen>(`${this.urlBase}/areas`, solicitud);

  }



  actualizarArea(id: number, solicitud: SolicitudActualizarArea): Observable<AreaResumen> {

    return this.http.put<AreaResumen>(`${this.urlBase}/areas/${id}`, solicitud);

  }



  eliminarArea(id: number): Observable<void> {

    return this.http.delete<void>(`${this.urlBase}/areas/${id}`);

  }



  listarPuestos(): Observable<PuestoResumen[]> {

    return this.http.get<PuestoResumen[]>(`${this.urlBase}/puestos`);

  }



  listarSectores(): Observable<SectorResumen[]> {

    return this.http.get<SectorResumen[]>(`${this.urlBase}/sectores`);

  }



  crearPuesto(solicitud: SolicitudCrearPuesto): Observable<PuestoResumen> {

    return this.http.post<PuestoResumen>(`${this.urlBase}/puestos`, solicitud);

  }



  actualizarPuesto(id: number, solicitud: SolicitudActualizarPuesto): Observable<PuestoResumen> {

    return this.http.put<PuestoResumen>(`${this.urlBase}/puestos/${id}`, solicitud);

  }



  eliminarPuesto(id: number): Observable<void> {

    return this.http.delete<void>(`${this.urlBase}/puestos/${id}`);

  }

}



export interface GerenciaResumen {

  id: number;

  nombre: string;

  descripcion: string | null;

}



export interface AreaResumen {

  id: number;

  nombre: string;

  descripcion: string | null;

  gerenciaId: number | null;

  gerenciaNombre: string | null;

}



export interface PuestoResumen {

  id: number;

  nombre: string;

  descripcion: string | null;

  nivel: string | null;

}



export interface SectorResumen {

  id: number;

  nombre: string;

  areaId: number | null;

}



export interface SolicitudCrearGerencia {

  nombre: string;

  descripcion?: string;

}



export interface SolicitudActualizarGerencia {

  nombre?: string;

  descripcion?: string;

}



export interface SolicitudCrearArea {

  nombre: string;

  descripcion?: string;

  gerenciaId?: number;

}



export interface SolicitudActualizarArea {

  nombre?: string;

  descripcion?: string;

  gerenciaId?: number;

}



export interface SolicitudCrearPuesto {

  nombre: string;

  descripcion?: string;

  nivel?: string;

}



export interface SolicitudActualizarPuesto {

  nombre?: string;

  descripcion?: string;

  nivel?: string;

}

