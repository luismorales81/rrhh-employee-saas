const ETIQUETAS_ESTADO_SOLICITUD: Record<string, string> = {
  BORRADOR: 'Borrador',
  ENVIADA: 'Enviada',
  EN_APROBACION: 'En aprobación',
  EN_REVISION: 'En revisión',
  APROBADO: 'Aprobado',
  RECHAZADO: 'Rechazado',
  CANCELADO: 'Cancelado',
  CAMBIOS_SOLICITADOS: 'Hay que corregir'
};

export function formatearEstadoSolicitud(estado: string): string {
  const etiqueta = ETIQUETAS_ESTADO_SOLICITUD[estado];
  if (etiqueta) {
    return etiqueta;
  }

  return estado
    .toLowerCase()
    .split('_')
    .map((palabra) => palabra.charAt(0).toUpperCase() + palabra.slice(1))
    .join(' ');
}

export function esEstadoSolicitudPendiente(estado: string): boolean {
  return estado === 'EN_REVISION'
    || estado === 'ENVIADA'
    || estado === 'EN_APROBACION'
    || estado === 'BORRADOR'
    || estado === 'CAMBIOS_SOLICITADOS';
}
