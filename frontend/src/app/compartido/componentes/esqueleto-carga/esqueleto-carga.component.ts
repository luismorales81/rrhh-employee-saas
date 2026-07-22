import { Component, input } from '@angular/core';

export type VarianteEsqueleto = 'lista' | 'indicadores' | 'perfil' | 'notificaciones' | 'vacaciones';

@Component({
  selector: 'app-esqueleto-carga',
  standalone: true,
  templateUrl: './esqueleto-carga.component.html'
})
export class EsqueletoCargaComponent {
  readonly variante = input<VarianteEsqueleto>('lista');
}
