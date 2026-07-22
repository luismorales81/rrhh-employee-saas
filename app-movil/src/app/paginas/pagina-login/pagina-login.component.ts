import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import {
  IonContent,
  IonHeader,
  IonTitle,
  IonToolbar,
  IonItem,
  IonInput,
  IonButton,
  IonList,
  IonText,
  IonSpinner,
  IonIcon
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { eyeOutline, eyeOffOutline } from 'ionicons/icons';
import { ServicioAutenticacion } from '../../nucleo/servicios/servicio-autenticacion';
import { ServicioPermisos } from '../../nucleo/servicios/servicio-permisos';
import { ServicioEstadoApi } from '../../nucleo/servicios/servicio-estado-api';
import { ServicioPreferencias } from '../../nucleo/servicios/servicio-preferencias';
import { ServicioAnalitica } from '../../nucleo/servicios/servicio-analitica';
import { SolicitudLogin } from '../../nucleo/modelos/autenticacion';
import { environment } from '../../../environments/environment';

addIcons({ eyeOutline, eyeOffOutline });

@Component({
  selector: 'app-pagina-login',
  standalone: true,
  imports: [
    FormsModule,
    RouterLink,
    IonContent,
    IonHeader,
    IonTitle,
    IonToolbar,
    IonItem,
    IonInput,
    IonButton,
    IonList,
    IonText,
    IonSpinner,
    IonIcon
  ],
  template: `
    <ion-header>
      <ion-toolbar>
        <ion-title>Ligo</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      <div class="contenedor-login">
        <img class="logo" src="assets/marca/ligo-icono.svg" alt="Ligo" width="64" height="64" />
        <h1>Entrá a tu espacio Ligo</h1>
        <p>Tu recibo, tus vacaciones y tu jornada, en un solo lugar.</p>

        <ion-list>
          <ion-item>
            <ion-input
              label="Correo electrónico"
              labelPlacement="stacked"
              type="email"
              [(ngModel)]="credenciales.email"
              autocomplete="email"
            ></ion-input>
          </ion-item>
          <ion-item>
            <ion-input
              label="Contraseña"
              labelPlacement="stacked"
              [type]="mostrarPassword() ? 'text' : 'password'"
              [(ngModel)]="credenciales.password"
              autocomplete="current-password"
            ></ion-input>
            <ion-button
              slot="end"
              fill="clear"
              type="button"
              aria-label="Mostrar u ocultar contraseña"
              (click)="mostrarPassword.set(!mostrarPassword())"
            >
              <ion-icon
                slot="icon-only"
                [name]="mostrarPassword() ? 'eye-off-outline' : 'eye-outline'"
              ></ion-icon>
            </ion-button>
          </ion-item>
        </ion-list>

        <a class="enlace-recuperar" routerLink="/recuperar-acceso">¿Olvidaste tu contraseña?</a>

        @if (mensajeError()) {
          <ion-text color="danger">
            <p>{{ mensajeError() }}</p>
          </ion-text>
        }

        <ion-button expand="block" class="boton-entrar" (click)="iniciarSesion()" [disabled]="cargando()">
          @if (cargando()) {
            <ion-spinner name="crescent"></ion-spinner>
          } @else {
            Entrar
          }
        </ion-button>

        @if (!esProduccion) {
          <p class="ayuda-demo">
            Empleado: empleado&#64;demo.local · Equipo: rrhh&#64;demo.local · Demo123!
          </p>
        }
      </div>
    </ion-content>
  `,
  styles: `
    .contenedor-login {
      max-width: 420px;
      margin: 2rem auto;
    }

    .logo {
      display: block;
      width: 64px;
      height: 64px;
      margin: 0 auto var(--espacio-4);
    }

    h1 {
      margin: 0 0 var(--espacio-1);
      text-align: center;
      font-weight: 700;
      font-size: 1.5rem;
      color: var(--color-texto);
    }

    p {
      color: var(--color-texto-secundario);
      margin: 0 0 var(--espacio-6);
      text-align: center;
    }

    .enlace-recuperar {
      display: inline-block;
      margin: var(--espacio-3) 0 0;
      font-size: 0.875rem;
      font-weight: 600;
      color: var(--color-secundario);
      text-decoration: none;
    }

    .boton-entrar {
      margin-top: var(--espacio-5);
      min-height: 52px;
      font-weight: 700;
    }

    .ayuda-demo {
      margin-top: var(--espacio-4);
      font-size: 0.75rem;
      color: var(--color-texto-terciario);
    }

    ion-button[slot='end'] {
      margin: 0;
      min-height: 44px;
      min-width: 44px;
    }
  `
})
export class PaginaLoginComponent implements OnInit {
  private readonly servicioAutenticacion = inject(ServicioAutenticacion);
  private readonly servicioPermisos = inject(ServicioPermisos);
  private readonly servicioEstadoApi = inject(ServicioEstadoApi);
  private readonly servicioPreferencias = inject(ServicioPreferencias);
  private readonly servicioAnalitica = inject(ServicioAnalitica);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  credenciales: SolicitudLogin = { email: '', password: '' };
  readonly esProduccion = environment.produccion;
  readonly cargando = signal(false);
  readonly mensajeError = signal<string | null>(null);
  readonly mostrarPassword = signal(false);

  ngOnInit(): void {
    this.servicioPreferencias.aplicarTemaActual();
    if (!this.servicioPreferencias.onboardingProductoVisto()) {
      void this.router.navigate(['/bienvenida'], { replaceUrl: true });
      return;
    }

    this.servicioEstadoApi.verificar();
    if (this.route.snapshot.queryParamMap.get('sesionExpirada') === '1') {
      this.mensajeError.set('Tu sesión expiró. Volvé a iniciar sesión.');
    }
    if (this.route.snapshot.queryParamMap.get('portalNoSoportado') === '1') {
      this.mensajeError.set('Esta cuenta se administra desde la web de Ligo.');
    }
  }

  iniciarSesion(): void {
    const email = this.credenciales.email.trim();
    const password = this.credenciales.password;
    if (!email || !password) {
      this.mensajeError.set('Completá correo y contraseña.');
      return;
    }

    this.cargando.set(true);
    this.mensajeError.set(null);

    this.servicioAutenticacion.iniciarSesion({ email, password }).subscribe({
      next: () => {
        this.cargando.set(false);
        this.servicioEstadoApi.conectada.set(true);
        this.servicioEstadoApi.mensaje.set(null);
        if (this.servicioPermisos.esSuperAdmin()) {
          this.servicioAutenticacion.limpiarSesionLocal();
          this.mensajeError.set('Esta cuenta se administra desde la web de Ligo.');
          return;
        }
        if (this.servicioPermisos.esPortalRrhh()) {
          this.servicioAutenticacion.limpiarSesionLocal();
          this.mensajeError.set('El equipo de RRHH usa la web de Ligo. Esta app es para empleados.');
          return;
        }
        this.servicioAnalitica.loginOk('empleado');
        void this.router.navigate([this.servicioPermisos.rutaInicioPorPortal()]);
      },
      error: (err) => {
        this.cargando.set(false);
        this.mensajeError.set(
          err.status === 0
            ? 'No pudimos conectarnos. Revisá tu internet e intentá de nuevo.'
            : (err.error?.mensaje ?? 'No pudimos entrar. Revisá correo y contraseña.')
        );
      }
    });
  }
}
