/**
 * Diccionario de estados para UI móvil.
 * Nunca mostrar enums crudos (PENDIENTE, EN_PROCESAMIENTO, etc.) al empleado.
 */

export type TonoEstado = 'neutro' | 'alerta' | 'exito' | 'error' | 'info';

export interface EtiquetaEstado {
  texto: string;
  tono: TonoEstado;
}

const RECIBO: Record<string, EtiquetaEstado> = {
  PENDIENTE: { texto: 'Para firmar', tono: 'alerta' },
  VISTO: { texto: 'Falta firmar', tono: 'alerta' },
  FIRMADO: { texto: 'Firmado', tono: 'exito' },
  DISCONFORMIDAD: { texto: 'Con observaciones', tono: 'error' }
};

const CERTIFICADO: Record<string, EtiquetaEstado> = {
  CARGADO: { texto: 'Recibido', tono: 'info' },
  EN_PROCESAMIENTO: { texto: 'Procesando', tono: 'info' },
  EXTRAIDO: { texto: 'En revisión', tono: 'alerta' },
  EN_REVISION: { texto: 'En revisión', tono: 'alerta' },
  VALIDADO: { texto: 'Aprobado', tono: 'exito' },
  RECHAZADO: { texto: 'Rechazado', tono: 'error' },
  REQUIERE_ACLARACION: { texto: 'Necesitamos algo más', tono: 'alerta' }
};

const FICHAJE: Record<string, EtiquetaEstado> = {
  VALIDADA: { texto: 'Validada', tono: 'exito' },
  PENDIENTE_REVISION: { texto: 'En revisión', tono: 'alerta' },
  RECHAZADA: { texto: 'Rechazada', tono: 'error' }
};

const DOCUMENTO: Record<string, EtiquetaEstado> = {
  PENDIENTE: { texto: 'Pendiente', tono: 'alerta' },
  ASIGNADO: { texto: 'Para revisar', tono: 'alerta' },
  LEIDO: { texto: 'Leído', tono: 'neutro' },
  FIRMADO: { texto: 'Firmado', tono: 'exito' }
};

const SOLICITUD: Record<string, EtiquetaEstado> = {
  BORRADOR: { texto: 'Borrador', tono: 'neutro' },
  ENVIADA: { texto: 'Enviada', tono: 'info' },
  EN_APROBACION: { texto: 'En aprobación', tono: 'alerta' },
  EN_REVISION: { texto: 'En revisión', tono: 'alerta' },
  APROBADO: { texto: 'Aprobado', tono: 'exito' },
  RECHAZADO: { texto: 'Rechazado', tono: 'error' },
  CANCELADO: { texto: 'Cancelado', tono: 'neutro' },
  CAMBIOS_SOLICITADOS: { texto: 'Hay que corregir', tono: 'alerta' }
};

function humanizarCrudo(estado: string): string {
  return estado
    .toLowerCase()
    .split('_')
    .filter(Boolean)
    .map((palabra) => palabra.charAt(0).toUpperCase() + palabra.slice(1))
    .join(' ');
}

function resolver(
  mapa: Record<string, EtiquetaEstado>,
  estado: string | null | undefined
): EtiquetaEstado {
  if (!estado) {
    return { texto: '—', tono: 'neutro' };
  }
  return mapa[estado] ?? { texto: humanizarCrudo(estado), tono: 'neutro' };
}

export function etiquetaEstadoRecibo(estado: string | null | undefined): EtiquetaEstado {
  return resolver(RECIBO, estado);
}

export function etiquetaEstadoCertificado(estado: string | null | undefined): EtiquetaEstado {
  return resolver(CERTIFICADO, estado);
}

export function etiquetaEstadoFichaje(estado: string | null | undefined): EtiquetaEstado {
  return resolver(FICHAJE, estado);
}

export function etiquetaEstadoDocumento(estado: string | null | undefined): EtiquetaEstado {
  return resolver(DOCUMENTO, estado);
}

export function etiquetaEstadoSolicitud(estado: string | null | undefined): EtiquetaEstado {
  return resolver(SOLICITUD, estado);
}

/** Texto corto para badges legacy que aún reciben string. */
export function textoEstadoRecibo(estado: string | null | undefined): string {
  return etiquetaEstadoRecibo(estado).texto;
}
