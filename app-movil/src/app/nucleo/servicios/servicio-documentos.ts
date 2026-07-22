import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ServicioDocumentos {
  private readonly urlBase = `${environment.urlApi}/documentos`;

  constructor(private readonly http: HttpClient) {}

  listarMisAsignaciones(): Observable<AsignacionDocumentoResumen[]> {
    return this.http.get<AsignacionDocumentoResumen[]>(`${this.urlBase}/mis-asignaciones`);
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

export interface EvidenciaFirmaResumen {
  asignacionId: number;
  fechaFirma: string;
  hashFirma: string;
  urlArchivoFirmado: string | null;
  direccionIp: string | null;
  dispositivo: string | null;
}

export interface SolicitudFirmarDocumento {
  imagenFirmaBase64?: string;
  dispositivo?: string;
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
