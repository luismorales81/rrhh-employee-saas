/**
 * Mensajes de error orientados al empleado (nunca detalles de infra de desarrollo).
 */

export function mensajeSinConexion(): string {
  return 'No pudimos conectarnos. Revisá tu internet e intentá de nuevo.';
}

export function mensajeSesionExpirada(): string {
  return 'Tu sesión expiró. Volvé a entrar para continuar.';
}

export function mensajeErrorGenerico(contexto?: string): string {
  if (contexto) {
    return `No pudimos ${contexto}. Intentá de nuevo en unos segundos.`;
  }
  return 'Algo salió mal. Intentá de nuevo en unos segundos.';
}

/**
 * Interpreta un error HTTP (o similar) y devuelve copy de producto.
 */
export function mensajeDesdeErrorHttp(
  error: { status?: number; error?: { mensaje?: string } } | null | undefined,
  contexto?: string
): string {
  if (!error) {
    return mensajeErrorGenerico(contexto);
  }
  if (error.status === 0) {
    return mensajeSinConexion();
  }
  if (error.status === 401) {
    return mensajeSesionExpirada();
  }
  const mensajeApi = error.error?.mensaje;
  if (mensajeApi && typeof mensajeApi === 'string' && mensajeApi.trim()) {
    return mensajeApi;
  }
  return mensajeErrorGenerico(contexto);
}
