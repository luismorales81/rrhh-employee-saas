import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
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

export interface SolicitudValidarCertificado {
  diagnostico?: string;
  reposoDias?: number;
  medicoNombre?: string;
  medicoMatricula?: string;
  fechaEmision?: string;
  corregidoManualmente?: boolean;
  observaciones?: string;
}

export interface SolicitudRechazarCertificado {
  observaciones: string;
}

export interface ConfiguracionAuditoriaMedica {
  diasReposoAlertaLeve: number;
  maxLicenciasCortasPeriodo: number;
  periodoEvaluacionDias: number;
  requiereRevisionManualSiempre: boolean;
  iaConfigurada: boolean;
  proyectoGcp: string | null;
  regionVertex: string | null;
  modeloGemini: string | null;
  credencialesConfiguradas: boolean;
  maxIntentosExtraccion: number;
  modelosDisponibles: string[];
}

export interface SolicitudActualizarConfiguracionAuditoriaMedica {
  diasReposoAlertaLeve: number;
  maxLicenciasCortasPeriodo: number;
  periodoEvaluacionDias: number;
  requiereRevisionManualSiempre: boolean;
  proyectoGcp?: string | null;
  regionVertex?: string | null;
  modeloGemini?: string | null;
  credencialesServicioJson?: string | null;
  maxIntentosExtraccion?: number | null;
}

export interface ResumenAusentismo {
  empleadoId: number;
  desde: string;
  hasta: string;
  totalCertificadosValidados: number;
  diasReposoTotales: number;
  certificadosConReposo: number;
}

export interface RespuestaReporteAusentismo {
  empresaId: number;
  desde: string;
  hasta: string;
  cantidadEmpleadosConAusentismo: number;
  diasReposoTotalesEmpresa: number;
  resumenesPorEmpleado: ResumenAusentismo[];
}

export const ETIQUETAS_ESTADO_CERTIFICADO: Record<EstadoCertificado, string> = {
  CARGADO: 'Cargado',
  EN_PROCESAMIENTO: 'En procesamiento',
  EXTRAIDO: 'Extraído',
  EN_REVISION: 'En revisión',
  VALIDADO: 'Validado',
  RECHAZADO: 'Rechazado',
  REQUIERE_ACLARACION: 'Requiere aclaración'
};

export const ETIQUETAS_TIPO_EXAMEN: Record<TipoExamenCertificado, string> = {
  PREOCUPACIONAL: 'Preocupacional',
  PERIODICO: 'Periódico',
  CAMBIO_TAREA: 'Cambio de tarea',
  REINCORPORACION: 'Reincorporación',
  POSTOCUPACIONAL: 'Postocupacional',
  AUSENTISMO: 'Ausentismo'
};

export const ESTADOS_PENDIENTES_REVISION: EstadoCertificado[] = [
  'EN_REVISION',
  'REQUIERE_ACLARACION',
  'EN_PROCESAMIENTO',
  'EXTRAIDO'
];

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

  validar(id: number, solicitud: SolicitudValidarCertificado): Observable<CertificadoCompleto> {
    return this.http.patch<CertificadoCompleto>(`${this.urlBase}/${id}/validar`, solicitud);
  }

  rechazar(id: number, solicitud: SolicitudRechazarCertificado): Observable<CertificadoCompleto> {
    return this.http.patch<CertificadoCompleto>(`${this.urlBase}/${id}/rechazar`, solicitud);
  }

  reprocesar(id: number): Observable<CertificadoCompleto> {
    return this.http.patch<CertificadoCompleto>(`${this.urlBase}/${id}/reprocesar`, {});
  }

  listarBandejaRevision(incluirRequiereAclaracion = false): Observable<CertificadoResumen[]> {
    const params = incluirRequiereAclaracion ? '?incluirRequiereAclaracion=true' : '';
    return this.http.get<CertificadoResumen[]>(`${this.urlBase}/bandeja-revision${params}`);
  }

  obtenerConfiguracion(): Observable<ConfiguracionAuditoriaMedica> {
    return this.http.get<ConfiguracionAuditoriaMedica>(`${this.urlBase}/configuracion-auditoria`);
  }

  actualizarConfiguracion(
    solicitud: SolicitudActualizarConfiguracionAuditoriaMedica
  ): Observable<ConfiguracionAuditoriaMedica> {
    return this.http.put<ConfiguracionAuditoriaMedica>(
      `${this.urlBase}/configuracion-auditoria`,
      solicitud
    );
  }

  reporteAusentismo(desde: string, hasta: string): Observable<RespuestaReporteAusentismo> {
    const params = new HttpParams().set('desde', desde).set('hasta', hasta);
    return this.http.get<RespuestaReporteAusentismo>(`${environment.urlApi}/reportes/ausentismo`, {
      params
    });
  }

  esCompleto(certificado: CertificadoResumen | CertificadoCompleto): certificado is CertificadoCompleto {
    return 'diagnostico' in certificado;
  }
}
