const ETIQUETAS_ESTADO_SOLICITUD: Record<string, string> = {
  BORRADOR: 'Borrador',
  EN_REVISION: 'En revisión',
  APROBADO: 'Aprobado',
  RECHAZADO: 'Rechazado',
  CANCELADO: 'Cancelado',
  CAMBIOS_SOLICITADOS: 'Cambios solicitados'
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
