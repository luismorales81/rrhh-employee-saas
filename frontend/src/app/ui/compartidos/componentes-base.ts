import { Component, Input } from '@angular/core';
import { NgClass } from '@angular/common';
import { RouterLink } from '@angular/router';

export type VarianteInsignia = 'neutro' | 'exito' | 'alerta' | 'error' | 'info' | 'primario';

@Component({
  selector: 'ui-insignia',
  standalone: true,
  imports: [NgClass],
  template: `
    <span class="ui-insignia" [ngClass]="'ui-insignia--' + variante">
      <ng-content />
    </span>
  `,
  styles: `
    .ui-insignia {
      display: inline-flex;
      align-items: center;
      padding: 0.125rem 0.5rem;
      border-radius: var(--radio-completo);
      font-size: var(--texto-caption);
      font-weight: 500;
      line-height: 1.4;
      white-space: nowrap;
    }
    .ui-insignia--neutro {
      background: var(--color-superficie-secundaria);
      color: var(--color-texto-secundario);
    }
    .ui-insignia--exito {
      background: var(--color-exito-fondo);
      color: var(--color-exito-texto);
    }
    .ui-insignia--alerta {
      background: var(--color-alerta-fondo);
      color: var(--color-alerta-texto);
    }
    .ui-insignia--error {
      background: var(--color-error-fondo);
      color: var(--color-error-texto);
    }
    .ui-insignia--info {
      background: var(--color-info-fondo);
      color: var(--color-info-texto);
    }
    .ui-insignia--primario {
      background: var(--color-primario-suave);
      color: var(--color-secundario);
    }
  `
})
export class InsigniaComponent {
  @Input() variante: VarianteInsignia = 'neutro';
}

@Component({
  selector: 'ui-avatar',
  standalone: true,
  template: `
    <span class="ui-avatar" [style.width.px]="tamano" [style.height.px]="tamano" [style.font-size.px]="tamano * 0.36">
      @if (url) {
        <img [src]="url" [alt]="nombre || ''" />
      } @else {
        {{ iniciales }}
      }
    </span>
  `,
  styles: `
    .ui-avatar {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      border-radius: var(--radio-completo);
      background: var(--color-primario-suave);
      color: var(--color-secundario);
      font-weight: 600;
      overflow: hidden;
      flex-shrink: 0;
    }
    .ui-avatar img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  `
})
export class AvatarComponent {
  @Input() nombre = '';
  @Input() url = '';
  @Input() tamano = 36;

  get iniciales(): string {
    const partes = this.nombre.trim().split(/\s+/).filter(Boolean);
    if (partes.length === 0) return '?';
    if (partes.length === 1) return partes[0].slice(0, 2).toUpperCase();
    return (partes[0][0] + partes[1][0]).toUpperCase();
  }
}

export interface MigaNavegacion {
  etiqueta: string;
  ruta?: string | string[];
}

@Component({
  selector: 'ui-migas',
  standalone: true,
  imports: [RouterLink],
  template: `
    <nav class="ui-migas" aria-label="Migas de pan">
      @for (miga of items; track $index; let ultimo = $last) {
        @if (!ultimo && miga.ruta) {
          <a class="ui-migas__enlace" [routerLink]="miga.ruta">{{ miga.etiqueta }}</a>
          <span class="ui-migas__sep" aria-hidden="true">/</span>
        } @else if (!ultimo) {
          <span class="ui-migas__texto">{{ miga.etiqueta }}</span>
          <span class="ui-migas__sep" aria-hidden="true">/</span>
        } @else {
          <span class="ui-migas__actual" aria-current="page">{{ miga.etiqueta }}</span>
        }
      }
    </nav>
  `,
  styles: `
    .ui-migas {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      gap: var(--espacio-2);
      margin-bottom: var(--espacio-4);
      font-size: var(--texto-caption);
      color: var(--color-texto-terciario);
    }
    .ui-migas__enlace {
      color: var(--color-texto-secundario);
      text-decoration: none;
    }
    .ui-migas__enlace:hover {
      color: var(--color-primario);
    }
    .ui-migas__sep {
      color: var(--color-borde-fuerte);
    }
    .ui-migas__actual {
      color: var(--color-texto);
      font-weight: 500;
    }
  `
})
export class MigasComponent {
  @Input({ required: true }) items: MigaNavegacion[] = [];
}

@Component({
  selector: 'ui-estado-vacio',
  standalone: true,
  template: `
    <div class="ui-estado-vacio">
      @if (icono) {
        <div class="ui-estado-vacio__icono" aria-hidden="true">{{ icono }}</div>
      }
      <h3 class="ui-estado-vacio__titulo">{{ titulo }}</h3>
      @if (descripcion) {
        <p class="ui-estado-vacio__desc">{{ descripcion }}</p>
      }
      <div class="ui-estado-vacio__accion">
        <ng-content />
      </div>
    </div>
  `,
  styles: `
    .ui-estado-vacio {
      text-align: center;
      padding: var(--espacio-12) var(--espacio-6);
      background: var(--color-superficie);
      border: 1px dashed var(--color-borde);
      border-radius: var(--radio-lg);
    }
    .ui-estado-vacio__icono {
      font-size: 1.75rem;
      margin-bottom: var(--espacio-3);
    }
    .ui-estado-vacio__titulo {
      margin: 0 0 var(--espacio-2);
      font-size: var(--texto-h3);
      font-weight: 600;
      color: var(--color-texto);
    }
    .ui-estado-vacio__desc {
      margin: 0 auto var(--espacio-4);
      max-width: 28rem;
      color: var(--color-texto-secundario);
      font-size: var(--texto-body);
    }
    .ui-estado-vacio__accion:empty {
      display: none;
    }
  `
})
export class EstadoVacioComponent {
  @Input() titulo = 'No hay datos';
  @Input() descripcion = '';
  @Input() icono = '';
}

@Component({
  selector: 'ui-estado-error',
  standalone: true,
  template: `
    <div class="ui-estado-error" role="alert">
      <strong>{{ titulo }}</strong>
      @if (mensaje) {
        <p>{{ mensaje }}</p>
      }
      <ng-content />
    </div>
  `,
  styles: `
    .ui-estado-error {
      padding: var(--espacio-4);
      border-radius: var(--radio-md);
      background: var(--color-error-fondo);
      border: 1px solid var(--color-error-borde);
      color: var(--color-error-texto);
      font-size: var(--texto-body);
    }
    .ui-estado-error p {
      margin: var(--espacio-2) 0 0;
    }
    .ui-estado-error strong {
      font-weight: 600;
    }
  `
})
export class EstadoErrorComponent {
  @Input() titulo = 'Algo salió mal';
  @Input() mensaje = '';
}

@Component({
  selector: 'ui-alerta',
  standalone: true,
  imports: [NgClass],
  template: `
    <div class="ui-alerta" [ngClass]="'ui-alerta--' + variante" role="status">
      <ng-content />
    </div>
  `,
  styles: `
    .ui-alerta {
      padding: var(--espacio-3) var(--espacio-4);
      border-radius: var(--radio-md);
      border: 1px solid;
      font-size: var(--texto-body);
    }
    .ui-alerta--info {
      background: var(--color-info-fondo);
      border-color: var(--color-info-borde);
      color: var(--color-info-texto);
    }
    .ui-alerta--exito {
      background: var(--color-exito-fondo);
      border-color: transparent;
      color: var(--color-exito-texto);
    }
    .ui-alerta--alerta {
      background: var(--color-alerta-fondo);
      border-color: var(--color-alerta-borde);
      color: var(--color-alerta-texto);
    }
    .ui-alerta--error {
      background: var(--color-error-fondo);
      border-color: var(--color-error-borde);
      color: var(--color-error-texto);
    }
  `
})
export class AlertaComponent {
  @Input() variante: 'info' | 'exito' | 'alerta' | 'error' = 'info';
}
