/**
 * Resuelve la ruta de la app móvil a partir del tipo de notificación.
 */
export function rutaDesdeNotificacion(
  tipo: string | null | undefined,
  referenciaId: number | null | undefined
): string {
  const t = (tipo ?? '').toUpperCase();

  if (t.includes('VACACION')) {
    return '/tabs/vacaciones';
  }
  if (t.includes('RECIBO')) {
    return referenciaId ? `/tabs/recibos/${referenciaId}` : '/tabs/recibos';
  }
  if (t.includes('DOCUMENTO')) {
    return '/tabs/documentos';
  }
  if (t.includes('COMUNICACION') || t.includes('AVISO')) {
    return '/tabs/comunicaciones';
  }
  if (t.includes('SOLICITUD')) {
    return '/tabs/solicitudes';
  }
  if (t.includes('FICHAJE') || t.includes('ASISTENCIA')) {
    return '/tabs/fichaje';
  }
  if (t.includes('CERTIFICADO') || t.includes('AUSEN')) {
    return '/tabs/ausentismo';
  }

  return '/tabs/notificaciones';
}

export function etiquetaTipoNotificacion(tipo: string | null | undefined): string {
  const t = (tipo ?? '').toUpperCase();
  if (t.includes('VACACION')) return 'Vacaciones';
  if (t.includes('RECIBO')) return 'Recibo';
  if (t.includes('DOCUMENTO')) return 'Documento';
  if (t.includes('COMUNICACION')) return 'Aviso';
  if (t.includes('SOLICITUD')) return 'Solicitud';
  if (t.includes('FICHAJE')) return 'Fichaje';
  if (!tipo) return 'Aviso';
  return tipo
    .toLowerCase()
    .split('_')
    .map((p) => p.charAt(0).toUpperCase() + p.slice(1))
    .join(' ');
}
