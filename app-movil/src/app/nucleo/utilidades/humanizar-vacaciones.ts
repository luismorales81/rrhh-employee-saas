/**
 * Convierte mensajes legales/técnicos de vacaciones a copy entendible para el empleado.
 */
export function humanizarMensajeVacaciones(mensaje: string | null | undefined): string {
  if (!mensaje) {
    return 'Revisá las fechas e intentá de nuevo.';
  }

  const texto = mensaje.trim();
  const inferior = texto.toLowerCase();

  if (inferior.includes('art. 151') || inferior.includes('patrón de descanso') || inferior.includes('lct')) {
    return 'Esas fechas no encajan bien con tu patrón de descanso. Te sugerimos otra fecha de inicio para que te aprueben más fácil.';
  }
  if (inferior.includes('ley 27.802') || inferior.includes('fracción mínima') || inferior.includes('días corridos')) {
    return 'Las vacaciones se piden de a 7 días corridos como mínimo.';
  }
  if (inferior.includes('saldo insuficiente')) {
    return texto.replace('Saldo insuficiente:', 'No te alcanzan los días:').replace('disponible', 'te quedan').replace('solicitados', 'pedís');
  }
  if (inferior.includes('superpone')) {
    return 'Ese período se cruza con otra solicitud tuya que ya está en curso.';
  }

  return texto;
}

export function humanizarListaMensajesVacaciones(mensajes: string[]): string[] {
  return mensajes.map((m) => humanizarMensajeVacaciones(m));
}
