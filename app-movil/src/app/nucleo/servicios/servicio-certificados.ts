import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export type EstadoCertificado =
  | 'CARGADO'
  | 'EN_PROCESAMIENTO'
  | 'EXTRAIDO'
  | 'EN_REVISION'
  | 'VALIDADO'
  | 'RECHAZADO'
  | 'REQUIERE_ACLARACION';

export type TipoExamenCertificado =
  | 'PREOCUPACIONAL'
  | 'PERIODICO'
  | 'CAMBIO_TAREA'
  | 'REINCORPORACION'
  | 'POSTOCUPACIONAL'
  | 'AUSENTISMO';

export type OrigenCargaCertificado = 'EMPLEADO' | 'MEDICO_LABORAL';

export interface CertificadoResumen {
  id: number;
  empleadoId: number;
  tipoExamen: TipoExamenCertificado;
  estado: EstadoCertificado;
  reposoDias: number | null;
  fechaEmision: string | null;
  origenCarga: OrigenCargaCertificado;
  archivoUrl: string;
  createdAt: string;
  revisionPrioritaria: boolean;
  motivosAlerta: string[];
}

export interface CertificadoCompleto extends CertificadoResumen {
  archivoHashSha256: string;
  diagnostico: string | null;
  medicoNombre: string | null;
  medicoMatricula: string | null;
  confianzaExtraccion: number | null;
  corregidoManualmente: boolean;
}

export interface SolicitudCargarCertificado {
  archivoId: number;
  tipoExamen: TipoExamenCertificado;
  origenCarga: OrigenCargaCertificado;
  empleadoId?: number;
}

export const ETIQUETAS_ESTADO_CERTIFICADO: Record<EstadoCertificado, string> = {
  CARGADO: 'Recibido',
  EN_PROCESAMIENTO: 'Procesando',
  EXTRAIDO: 'En revisión',
  EN_REVISION: 'En revisión',
  VALIDADO: 'Aprobado',
  RECHAZADO: 'Rechazado',
  REQUIERE_ACLARACION: 'Necesitamos algo más'
};

export const ETIQUETAS_TIPO_EXAMEN: Record<TipoExamenCertificado, string> = {
  PREOCUPACIONAL: 'Preocupacional',
  PERIODICO: 'Periódico',
  CAMBIO_TAREA: 'Cambio de tarea',
  REINCORPORACION: 'Reincorporación',
  POSTOCUPACIONAL: 'Postocupacional',
  AUSENTISMO: 'Ausentismo'
};

@Injectable({ providedIn: 'root' })
export class ServicioCertificados {
  private readonly urlBase = `${environment.urlApi}/certificados`;

  constructor(private readonly http: HttpClient) {}

  cargar(solicitud: SolicitudCargarCertificado): Observable<CertificadoResumen | CertificadoCompleto> {
    return this.http.post<CertificadoResumen | CertificadoCompleto>(this.urlBase, solicitud);
  }

  obtener(id: number): Observable<CertificadoResumen | CertificadoCompleto> {
    return this.http.get<CertificadoResumen | CertificadoCompleto>(`${this.urlBase}/${id}`);
  }

  listarPorEmpleado(empleadoId: number): Observable<CertificadoResumen[]> {
    return this.http.get<CertificadoResumen[]>(`${this.urlBase}/empleado/${empleadoId}`);
  }

  esCompleto(certificado: CertificadoResumen | CertificadoCompleto): certificado is CertificadoCompleto {
    return 'diagnostico' in certificado;
  }
}
