import { Component, input } from '@angular/core';

@Component({
  selector: 'app-encabezado-pagina',
  standalone: true,
  template: `
    <header class="encabezado-pagina">
      <h1>{{ titulo() }}</h1>
      @if (subtitulo()) {
        <p>{{ subtitulo() }}</p>
      }
    </header>
  `,
  styles: `
    .encabezado-pagina {
      margin-bottom: var(--espacio-4);

      h1 {
        margin: 0 0 var(--espacio-1);
        font-size: 1.5rem;
        font-weight: 700;
        letter-spacing: -0.02em;
        color: var(--color-texto);
      }

      p {
        margin: 0;
        color: var(--color-texto-secundario);
        font-size: 0.9375rem;
        line-height: 1.5;
      }
    }
  `
})
export class EncabezadoPaginaComponent {
  readonly titulo = input.required<string>();
  readonly subtitulo = input<string | null>(null);
}
