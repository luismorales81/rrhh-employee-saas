import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ServicioFichajes {
  private readonly urlBase = `${environment.urlApi}/fichajes`;

  constructor(private readonly http: HttpClient) {}

  listarZonas(): Observable<ZonaFichajeResumen[]> {
    return this.http.get<ZonaFichajeResumen[]>(`${this.urlBase}/zonas`);
  }

  obtenerZona(id: number): Observable<ZonaFichajeResumen> {
    return this.http.get<ZonaFichajeResumen>(`${this.urlBase}/zonas/${id}`);
  }

  crearZona(solicitud: SolicitudCrearZonaFichaje): Observable<ZonaFichajeResumen> {
    return this.http.post<ZonaFichajeResumen>(`${this.urlBase}/zonas`, solicitud);
  }

  actualizarZona(id: number, solicitud: SolicitudActualizarZonaFichaje): Observable<ZonaFichajeResumen> {
    return this.http.put<ZonaFichajeResumen>(`${this.urlBase}/zonas/${id}`, solicitud);
  }

  eliminarZona(id: number): Observable<void> {
    return this.http.delete<void>(`${this.urlBase}/zonas/${id}`);
  }

  agregarAsignacion(zonaId: number, solicitud: SolicitudAsignacionZona): Observable<AsignacionZonaResumen> {
    return this.http.post<AsignacionZonaResumen>(`${this.urlBase}/zonas/${zonaId}/asignaciones`, solicitud);
  }

  eliminarAsignacion(zonaId: number, asignacionId: number): Observable<void> {
    return this.http.delete<void>(`${this.urlBase}/zonas/${zonaId}/asignaciones/${asignacionId}`);
  }

  listarConfiguraciones(): Observable<ConfiguracionFichajeEmpleadoResumen[]> {
    return this.http.get<ConfiguracionFichajeEmpleadoResumen[]>(`${this.urlBase}/configuracion/empleados`);
  }

  actualizarConfiguracion(
    empleadoId: number,
    solicitud: SolicitudConfiguracionFichaje
  ): Observable<ConfiguracionFichajeEmpleadoResumen> {
    return this.http.put<ConfiguracionFichajeEmpleadoResumen>(
      `${this.urlBase}/configuracion/empleados/${empleadoId}`,
      solicitud
    );
  }

  obtenerPolitica(): Observable<PoliticaFichajeResumen> {
    return this.http.get<PoliticaFichajeResumen>(`${this.urlBase}/politica`);
  }

  actualizarPolitica(solicitud: SolicitudActualizarPolitica): Observable<PoliticaFichajeResumen> {
    return this.http.put<PoliticaFichajeResumen>(`${this.urlBase}/politica`, solicitud);
  }

  listarMarcas(filtros?: FiltrosMarcas): Observable<MarcaFichajeResumen[]> {
    let params = new HttpParams();
    if (filtros?.empleadoId) {
      params = params.set('empleadoId', filtros.empleadoId);
    }
    if (filtros?.estado) {
      params = params.set('estado', filtros.estado);
    }
    if (filtros?.zonaId) {
      params = params.set('zonaId', filtros.zonaId);
    }
    if (filtros?.desde) {
      params = params.set('desde', filtros.desde);
    }
    if (filtros?.hasta) {
      params = params.set('hasta', filtros.hasta);
    }
    return this.http.get<MarcaFichajeResumen[]>(`${this.urlBase}/marcas`, { params });
  }

  listarPendientesRevision(): Observable<MarcaFichajeResumen[]> {
    return this.http.get<MarcaFichajeResumen[]>(`${this.urlBase}/marcas/pendientes-revision`);
  }

  aprobarMarca(id: number): Observable<MarcaFichajeResumen> {
    return this.http.post<MarcaFichajeResumen>(`${this.urlBase}/marcas/${id}/aprobar`, {});
  }

  rechazarMarca(id: number, notaRevision: string): Observable<MarcaFichajeResumen> {
    return this.http.post<MarcaFichajeResumen>(`${this.urlBase}/marcas/${id}/rechazar`, { notaRevision });
  }

  obtenerResumen(desde?: string, hasta?: string): Observable<ResumenFichajeEmpleado[]> {
    let params = new HttpParams();
    if (desde) {
      params = params.set('desde', desde);
    }
    if (hasta) {
      params = params.set('hasta', hasta);
    }
    return this.http.get<ResumenFichajeEmpleado[]>(`${this.urlBase}/reportes/resumen`, { params });
  }

  exportarCsv(desde?: string, hasta?: string, empleadoId?: number): Observable<Blob> {
    let params = new HttpParams();
    if (desde) {
      params = params.set('desde', desde);
    }
    if (hasta) {
      params = params.set('hasta', hasta);
    }
    if (empleadoId) {
      params = params.set('empleadoId', empleadoId);
    }
    return this.http.get(`${this.urlBase}/reportes/exportar`, { params, responseType: 'blob' });
  }
}

export type AlcanceZona = 'GLOBAL' | 'ASIGNADA';
export type TipoMarca = 'ENTRADA' | 'SALIDA';
export type EstadoMarca = 'VALIDADA' | 'PENDIENTE_REVISION' | 'RECHAZADA';
export type MotivoEstado = 'FUERA_DE_ZONA' | 'SIN_ZONA_HABILITADA' | 'PRECISION_BAJA';

export interface ZonaFichajeResumen {
  id: number;
  nombre: string;
  descripcion: string | null;
  coordenadas: number[][];
  alcance: AlcanceZona;
  activa: boolean;
  asignaciones: AsignacionZonaResumen[];
}

export interface AsignacionZonaResumen {
  id: number;
  empleadoId: number | null;
  empleadoNombre: string | null;
  gerenciaId: number | null;
  gerenciaNombre: string | null;
  areaId: number | null;
  areaNombre: string | null;
  sectorId: number | null;
  sectorNombre: string | null;
  tipo: string;
}

export interface SolicitudCrearZonaFichaje {
  nombre: string;
  descripcion?: string;
  coordenadas: number[][];
  alcance: AlcanceZona;
  activa: boolean;
}

export interface SolicitudActualizarZonaFichaje {
  nombre?: string;
  descripcion?: string;
  coordenadas?: number[][];
  alcance?: AlcanceZona;
  activa?: boolean;
}

export interface SolicitudAsignacionZona {
  empleadoId?: number;
  gerenciaId?: number;
  areaId?: number;
  sectorId?: number;
  tipo: 'INCLUYE';
}

export interface ConfiguracionFichajeEmpleadoResumen {
  empleadoId: number;
  empleadoNombre: string;
  fichajeMovilHabilitado: boolean;
  restringirSoloZonasAsignadas: boolean;
  consentimientoAceptadoAt: string | null;
  versionPoliticaAceptada: number | null;
  requiereConsentimiento: boolean;
}

export interface SolicitudConfiguracionFichaje {
  fichajeMovilHabilitado: boolean;
  restringirSoloZonasAsignadas: boolean;
}

export interface PoliticaFichajeResumen {
  textoTransparencia: string;
  version: number;
  permitirFichajeSinGps: boolean;
  precisionMaximaMetros: number;
}

export interface SolicitudActualizarPolitica {
  textoTransparencia: string;
  permitirFichajeSinGps: boolean;
  precisionMaximaMetros: number;
}

export interface MarcaFichajeResumen {
  id: number;
  empleadoId: number;
  empleadoNombre: string;
  tipo: TipoMarca;
  fechaHoraServidor: string;
  fechaHoraDispositivo: string | null;
  latitud: number | null;
  longitud: number | null;
  precisionMetros: number | null;
  idDispositivo: string | null;
  modeloDispositivo: string | null;
  sistemaOperativo: string | null;
  versionApp: string | null;
  zonaFichajeId: number | null;
  zonaFichajeNombre: string | null;
  estado: EstadoMarca;
  motivoEstado: MotivoEstado | null;
  notaRevision: string | null;
  revisadoAt: string | null;
}

export interface ResumenFichajeEmpleado {
  empleadoId: number;
  empleadoNombre: string;
  fecha: string;
  cantidadEntradas: number;
  cantidadSalidas: number;
  cantidadPendientes: number;
  horasEstimadas: string;
}

export interface FiltrosMarcas {
  empleadoId?: number;
  estado?: EstadoMarca;
  zonaId?: number;
  desde?: string;
  hasta?: string;
}
