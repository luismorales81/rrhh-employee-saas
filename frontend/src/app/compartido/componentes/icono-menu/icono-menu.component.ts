import { Component, Input } from '@angular/core';
import { LucideDynamicIcon } from '@lucide/angular';

@Component({
  selector: 'app-icono-menu',
  standalone: true,
  imports: [LucideDynamicIcon],
  template: `<svg [lucideIcon]="nombre" [size]="tamano" [strokeWidth]="2" class="icono-menu" aria-hidden="true" />`,
  styles: `
    :host {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }

    .icono-menu {
      color: currentColor;
    }
  `
})
export class IconoMenuComponent {
  @Input({ required: true }) nombre!: string;
  @Input() tamano = 18;
}
