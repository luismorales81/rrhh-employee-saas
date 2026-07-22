import { Component, inject } from '@angular/core';
import {
  IonContent,
  IonHeader,
  IonTitle,
  IonToolbar,
  IonList,
  IonItem,
  IonLabel,
  IonButton,
  IonAvatar
} from '@ionic/angular/standalone';
import { ServicioAutenticacion } from '../../nucleo/servicios/servicio-autenticacion';

@Component({
  selector: 'app-pagina-perfil-rrhh',
  standalone: true,
  imports: [
    IonContent,
    IonHeader,
    IonTitle,
    IonToolbar,
    IonList,
    IonItem,
    IonLabel,
    IonButton,
    IonAvatar
  ],
  template: `
    <ion-header>
      <ion-toolbar color="primary">
        <ion-title>Perfil</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      <div class="perfil-encabezado">
        <ion-avatar>
          <div class="inicial">{{ inicial() }}</div>
        </ion-avatar>
        <h2>{{ email() }}</h2>
        <p>Ligo para equipos</p>
      </div>

      <ion-list>
        <ion-item>
          <ion-label>
            <h3>Rol</h3>
            <p>Equipo de personas</p>
          </ion-label>
        </ion-item>
      </ion-list>

      <ion-button expand="block" color="danger" (click)="cerrarSesion()">Cerrar sesión</ion-button>
    </ion-content>
  `,
  styles: `
    .perfil-encabezado {
      text-align: center;
      margin: 1.5rem 0 2rem;
    }

    ion-avatar {
      width: 72px;
      height: 72px;
      margin: 0 auto 0.75rem;
      background: var(--color-primario);
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .inicial {
      color: #fff;
      font-size: 1.75rem;
      font-weight: 700;
    }

    h2 { margin: 0; font-size: 1.1rem; }
    p { color: var(--ion-color-medium); margin-top: 0.25rem; }
    ion-button { margin-top: 2rem; }
  `
})
export class PaginaPerfilRrhhComponent {
  private readonly servicioAutenticacion = inject(ServicioAutenticacion);
  readonly email = () => this.servicioAutenticacion.sesionActual()?.email ?? '';

  inicial(): string {
    const mail = this.email();
    return mail ? mail.charAt(0).toUpperCase() : '?';
  }

  cerrarSesion(): void {
    this.servicioAutenticacion.cerrarSesion();
  }
}
