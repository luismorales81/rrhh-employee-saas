export function calcularDiasSolicitados(fechaInicio: string, fechaFin: string): number {
  if (!fechaInicio || !fechaFin) {
    return 0;
  }
  const inicio = new Date(`${fechaInicio}T00:00:00`);
  const fin = new Date(`${fechaFin}T00:00:00`);
  const diff = Math.round((fin.getTime() - inicio.getTime()) / 86_400_000) + 1;
  return diff > 0 ? diff : 0;
}

export function validarPeriodoVacaciones(
  fechaInicio: string,
  fechaFin: string,
  diasMinimos: number,
  diasDisponibles: number | null | undefined,
  solicitudesExistentes: PeriodoVacacionesExistente[] = []
): string[] {
  const errores: string[] = [];

  if (!fechaInicio || !fechaFin) {
    errores.push('Indicá fecha de inicio y fin.');
    return errores;
  }

  if (fechaFin < fechaInicio) {
    errores.push('La fecha fin no puede ser anterior a la fecha de inicio.');
  }

  const dias = calcularDiasSolicitados(fechaInicio, fechaFin);

  if (dias > 0 && dias < diasMinimos) {
    errores.push(`Las vacaciones se piden de a ${diasMinimos} días corridos como mínimo.`);
  }

  if (diasDisponibles != null && dias > 0 && dias > diasDisponibles) {
    errores.push(`No te alcanzan los días: te quedan ${diasDisponibles}, pedís ${dias}.`);
  }

  const solapamiento = validarSolapamientoVacaciones(fechaInicio, fechaFin, solicitudesExistentes);
  if (solapamiento) {
    errores.push(solapamiento);
  }

  return errores;
}

export interface PeriodoVacacionesExistente {
  fechaInicio: string;
  fechaFin: string;
  estado: string;
}

export function validarSolapamientoVacaciones(
  fechaInicio: string,
  fechaFin: string,
  solicitudesExistentes: PeriodoVacacionesExistente[]
): string | null {
  const estadosActivos = new Set(['BORRADOR', 'EN_REVISION', 'APROBADO', 'CAMBIOS_SOLICITADOS']);

  for (const solicitud of solicitudesExistentes) {
    if (!estadosActivos.has(solicitud.estado)) {
      continue;
    }
    if (fechaFin < solicitud.fechaInicio || fechaInicio > solicitud.fechaFin) {
      continue;
    }
    return 'El período solicitado se superpone con otra solicitud pendiente o aprobada';
  }

  return null;
}

export function extraerErroresVacaciones(error: unknown): string[] {
  const cuerpo = (error as { error?: { errores?: string[]; mensaje?: string } })?.error;
  if (cuerpo?.errores?.length) {
    return cuerpo.errores;
  }
  if (cuerpo?.mensaje) {
    return [cuerpo.mensaje];
  }
  return ['No se pudo enviar la solicitud de vacaciones.'];
}
