/** Diccionario de estados de dominio → etiqueta humana en castellano. */
const ETIQUETAS_ESTADO: Record<string, string> = {
  ACTIVO: 'Activo',
  INACTIVO: 'Inactivo',
  PENDIENTE: 'Pendiente',
  APROBADA: 'Aprobada',
  APROBADO: 'Aprobado',
  RECHAZADA: 'Rechazada',
  RECHAZADO: 'Rechazado',
  EN_REVISION: 'En revisión',
  EN_CURSO: 'En curso',
  CANCELADA: 'Cancelada',
  CANCELADO: 'Cancelado',
  FIRMADO: 'Firmado',
  SIN_FIRMAR: 'Sin firmar',
  CONFORME: 'Conforme',
  DISCONFORMIDAD: 'Con disconformidad',
  BORRADOR: 'Borrador',
  PUBLICADO: 'Publicado',
  PROGRAMADA: 'Programada',
  ENVIADA: 'Enviada',
  COMPLETADA: 'Completada',
  COMPLETADO: 'Completado',
  VENCIDO: 'Vencido',
  VENCIDA: 'Vencida'
};

export function etiquetaEstado(codigo: string | null | undefined): string {
  if (!codigo) {
    return '—';
  }
  const clave = codigo.trim().toUpperCase();
  return ETIQUETAS_ESTADO[clave] ?? codigo.replace(/_/g, ' ').toLowerCase().replace(/^\w/, (c) => c.toUpperCase());
}

export type VarianteEstadoVisual = 'neutro' | 'exito' | 'alerta' | 'error' | 'info' | 'primario';

export function varianteEstado(codigo: string | null | undefined): VarianteEstadoVisual {
  if (!codigo) {
    return 'neutro';
  }
  const c = codigo.toUpperCase();
  if (['ACTIVO', 'APROBADA', 'APROBADO', 'FIRMADO', 'CONFORME', 'COMPLETADA', 'COMPLETADO', 'PUBLICADO', 'ENVIADA'].includes(c)) {
    return 'exito';
  }
  if (['PENDIENTE', 'EN_REVISION', 'EN_CURSO', 'PROGRAMADA', 'BORRADOR', 'SIN_FIRMAR'].includes(c)) {
    return 'alerta';
  }
  if (['RECHAZADA', 'RECHAZADO', 'CANCELADA', 'CANCELADO', 'INACTIVO', 'VENCIDO', 'VENCIDA', 'DISCONFORMIDAD'].includes(c)) {
    return 'error';
  }
  return 'neutro';
}
