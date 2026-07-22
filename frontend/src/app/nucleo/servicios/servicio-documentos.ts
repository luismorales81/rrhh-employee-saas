import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ServicioDocumentos {
  private readonly urlBase = `${environment.urlApi}/documentos`;

  constructor(private readonly http: HttpClient) {}

  listarCategorias(): Observable<CategoriaDocumentoResumen[]> {
    return this.http.get<CategoriaDocumentoResumen[]>(`${this.urlBase}/categorias`);
  }

  crearCategoria(solicitud: SolicitudCrearCategoria): Observable<CategoriaDocumentoResumen> {
    return this.http.post<CategoriaDocumentoResumen>(`${this.urlBase}/categorias`, solicitud);
  }

  listarDocumentos(): Observable<DocumentoResumen[]> {
    return this.http.get<DocumentoResumen[]>(this.urlBase);
  }

  crearDocumento(solicitud: SolicitudCrearDocumento): Observable<DocumentoResumen> {
    return this.http.post<DocumentoResumen>(this.urlBase, solicitud);
  }

  listarAsignaciones(): Observable<AsignacionDocumentoResumen[]> {
    return this.http.get<AsignacionDocumentoResumen[]>(`${this.urlBase}/asignaciones`);
  }

  listarMisAsignaciones(): Observable<AsignacionDocumentoResumen[]> {
    return this.http.get<AsignacionDocumentoResumen[]>(`${this.urlBase}/mis-asignaciones`);
  }

  asignar(documentoId: number, solicitud: SolicitudAsignarDocumento): Observable<AsignacionDocumentoResumen[]> {
    return this.http.post<AsignacionDocumentoResumen[]>(`${this.urlBase}/${documentoId}/asignar`, solicitud);
  }

  confirmarLectura(asignacionId: number): Observable<AsignacionDocumentoResumen> {
    return this.http.post<AsignacionDocumentoResumen>(
      `${this.urlBase}/asignaciones/${asignacionId}/confirmar-lectura`,
      {}
    );
  }

  firmar(asignacionId: number, solicitud?: SolicitudFirmarDocumento): Observable<AsignacionDocumentoResumen> {
    return this.http.post<AsignacionDocumentoResumen>(
      `${this.urlBase}/asignaciones/${asignacionId}/firmar`,
      solicitud ?? {}
    );
  }

  obtenerEvidenciaFirma(asignacionId: number): Observable<EvidenciaFirmaResumen> {
    return this.http.get<EvidenciaFirmaResumen>(
      `${this.urlBase}/asignaciones/${asignacionId}/evidencia-firma`
    );
  }
}

export interface SolicitudFirmarDocumento {
  imagenFirmaBase64?: string;
  dispositivo?: string;
}

export interface EvidenciaFirmaResumen {
  asignacionId: number;
  fechaFirma: string;
  hashFirma: string;
  urlArchivoFirmado: string | null;
  direccionIp: string | null;
  dispositivo: string | null;
}

export interface CategoriaDocumentoResumen {
  id: number;
  nombre: string;
  descripcion: string | null;
  activa: boolean;
}

export interface DocumentoResumen {
  id: number;
  categoriaId: number;
  categoriaNombre: string;
  titulo: string;
  version: string;
  urlArchivo?: string | null;
  requiereFirma: boolean;
  activo: boolean;
}

export interface AsignacionDocumentoResumen {
  id: number;
  empleadoId: number;
  nombreEmpleado: string;
  documentoId: number;
  tituloDocumento: string;
  urlArchivo?: string | null;
  estado: string;
  requiereFirma: boolean;
  firmado: boolean;
}

export interface SolicitudCrearCategoria {
  nombre: string;
  descripcion?: string;
}

export interface SolicitudCrearDocumento {
  categoriaId: number;
  titulo: string;
  descripcion?: string;
  version?: string;
  urlArchivo: string;
  requiereFirma?: boolean;
}

export interface SolicitudAsignarDocumento {
  empleadoIds: number[];
  fechaVencimiento?: string;
}
