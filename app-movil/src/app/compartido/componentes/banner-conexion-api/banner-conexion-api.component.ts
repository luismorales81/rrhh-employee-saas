import { Component, inject, OnInit } from '@angular/core';
import { IonText } from '@ionic/angular/standalone';
import { ServicioEstadoApi } from '../../../nucleo/servicios/servicio-estado-api';

@Component({
  selector: 'app-banner-conexion-api',
  standalone: true,
  imports: [IonText],
  template: `
    @if (servicioEstadoApi.conectada() === false) {
      <div class="banner-error" role="alert">
        <ion-text color="light">
          <p>{{ servicioEstadoApi.mensaje() }}</p>
        </ion-text>
      </div>
    }
  `,
  styles: `
    .banner-error {
      background: #b91c1c;
      color: #fff;
      padding: 0.65rem 1rem;
      font-size: 0.85rem;
      line-height: 1.4;
    }
    p { margin: 0; }
  `
})
export class BannerConexionApiComponent implements OnInit {
  readonly servicioEstadoApi = inject(ServicioEstadoApi);

  ngOnInit(): void {
    this.servicioEstadoApi.verificar();
  }
}
