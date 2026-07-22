import { Directive, HostListener } from '@angular/core';
import { liberarFocoActivo } from '../utilidades/liberar-foco-activo';

/**
 * Quita el foco del control antes de que Ionic oculte la página anterior con aria-hidden.
 * Evita el aviso del navegador cuando un descendiente enfocado queda dentro de ion-page-hidden.
 */
@Directive({
  selector: 'a[routerLink], ion-button[routerLink], button[routerLink]',
  standalone: true
})
export class DirectivaLiberarFocoNavegacion {
  /** pointerdown corre antes del click y de la transición de Ionic. */
  @HostListener('pointerdown')
  @HostListener('click')
  liberarFoco(): void {
    liberarFocoActivo();
  }
}
