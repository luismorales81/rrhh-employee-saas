/** Quita el foco del control activo para evitar avisos de aria-hidden en Ionic. */
export function liberarFocoActivo(): void {
  const activo = document.activeElement;
  if (activo instanceof HTMLElement && activo !== document.body && activo !== document.documentElement) {
    activo.blur();
  }
}
