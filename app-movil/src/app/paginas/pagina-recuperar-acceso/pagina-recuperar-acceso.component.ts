import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import {
  IonBackButton,
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonInput,
  IonItem,
  IonTitle,
  IonToolbar
} from '@ionic/angular/standalone';
import { PanelExitoComponent } from '../../compartido/componentes/panel-exito/panel-exito.component';

@Component({
  selector: 'app-pagina-recuperar-acceso',
  standalone: true,
  imports: [
    FormsModule,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButtons,
    IonBackButton,
    IonContent,
    IonItem,
    IonInput,
    IonButton,
    PanelExitoComponent
  ],
  template: `
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button defaultHref="/iniciar-sesion"></ion-back-button>
        </ion-buttons>
        <ion-title>Recuperar acceso</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      @if (enviado()) {
        <app-panel-exito
          titulo="Pedile ayuda a tu equipo"
          [mensaje]="mensajeExito()"
          textoAccion="Volver a entrar"
          rutaAccion="/iniciar-sesion"
        />
      } @else {
        <div class="contenedor">
          <h1>¿Olvidaste tu contraseña?</h1>
          <p>
            Todavía no enviamos mails automáticos desde la app.
            Dejanos tu correo y pedile a RRHH que restablezca tu acceso.
          </p>

          <ion-item>
            <ion-input
              label="Correo electrónico"
              labelPlacement="stacked"
              type="email"
              [(ngModel)]="email"
              autocomplete="email"
            ></ion-input>
          </ion-item>

          @if (error()) {
            <p class="error">{{ error() }}</p>
          }

          <ion-button expand="block" class="cta" type="button" (click)="continuar()">
            Continuar
          </ion-button>

          <ion-button expand="block" fill="clear" type="button" (click)="volverLogin()">
            Volver al inicio de sesión
          </ion-button>
        </div>
      }
    </ion-content>
  `,
  styles: `
    .contenedor {
      max-width: 420px;
      margin: 1.5rem auto;
    }

    h1 {
      margin: 0 0 0.5rem;
      font-size: 1.35rem;
      font-weight: 700;
      color: var(--color-texto);
    }

    p {
      color: var(--color-texto-secundario);
      line-height: 1.45;
      margin: 0 0 1.25rem;
    }

    .cta {
      margin-top: 1.25rem;
      min-height: 52px;
      font-weight: 700;
    }

    .error {
      color: var(--color-error-texto);
      font-size: 0.875rem;
    }
  `
})
export class PaginaRecuperarAccesoComponent {
  private readonly router = inject(Router);

  email = '';
  readonly enviado = signal(false);
  readonly error = signal<string | null>(null);
  readonly mensajeExito = signal('');

  continuar(): void {
    const correo = this.email.trim();
    if (!correo || !correo.includes('@')) {
      this.error.set('Ingresá un correo válido.');
      return;
    }
    this.error.set(null);
    this.mensajeExito.set(
      `Escribile a tu equipo de RRHH indicando el correo ${correo} para que te den una nueva contraseña.`
    );
    this.enviado.set(true);
  }

  volverLogin(): void {
    void this.router.navigateByUrl('/iniciar-sesion');
  }
}
