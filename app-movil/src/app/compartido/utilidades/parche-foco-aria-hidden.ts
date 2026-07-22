import { liberarFocoActivo } from './liberar-foco-activo';

/**
 * Parche defensivo para Ionic 8 + Chrome:
 * cuando se aplica aria-hidden sobre un ancestro del elemento con foco,
 * Chrome registra "Blocked aria-hidden...". Liberamos el foco justo antes.
 *
 * @see https://github.com/ionic-team/ionic-framework/issues/30240
 */
export function instalarParcheFocoAriaHidden(): void {
  const win = window as WindowConParche;
  if (typeof Element === 'undefined' || win.__parcheFocoAriaHidden) {
    return;
  }
  win.__parcheFocoAriaHidden = true;

  const setAttributeOriginal = Element.prototype.setAttribute;
  Element.prototype.setAttribute = function setAttributeParcheado(
    this: Element,
    nombre: string,
    valor: string
  ): void {
    if (
      nombre === 'aria-hidden' &&
      (valor === 'true' || valor === '') &&
      document.activeElement instanceof Node &&
      this.contains(document.activeElement)
    ) {
      liberarFocoActivo();
    }
    setAttributeOriginal.call(this, nombre, valor);
  };
}

interface WindowConParche extends Window {
  __parcheFocoAriaHidden?: boolean;
}
