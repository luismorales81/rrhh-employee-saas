import { SolicitudResumen } from '../servicios/servicio-solicitudes';
import { SolicitudVacacionesResumen } from '../servicios/servicio-vacaciones';

export function enriquecerSolicitudesConVacaciones(
  solicitudes: SolicitudResumen[],
  vacaciones: SolicitudVacacionesResumen[]
): SolicitudResumen[] {
  const vacacionesPorSolicitud = new Map(vacaciones.map((vacacion) => [vacacion.solicitudId, vacacion]));

  return solicitudes.map((solicitud) => {
    const vacacion = vacacionesPorSolicitud.get(solicitud.id);
    if (!vacacion) {
      return solicitud;
    }

    return {
      ...solicitud,
      fechaInicioVacaciones: solicitud.fechaInicioVacaciones ?? vacacion.fechaInicio,
      fechaFinVacaciones: solicitud.fechaFinVacaciones ?? vacacion.fechaFin,
      motivo: solicitud.motivo ?? vacacion.motivo,
      solicitudVacacionesId: solicitud.solicitudVacacionesId ?? vacacion.solicitudVacacionesId
    };
  });
}
