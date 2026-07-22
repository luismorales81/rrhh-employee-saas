import { Component, inject, OnInit, signal } from '@angular/core';
import {
  IonContent,
  IonHeader,
  IonTitle,
  IonToolbar,
  IonList,
  IonItem,
  IonLabel,
  IonButton,
  IonSpinner,
  IonText,
  IonAvatar,
  AlertController
} from '@ionic/angular/standalone';
import {
  EmpleadoResumen,
  ServicioEmpleados
} from '../../nucleo/servicios/servicio-empleados';
import { ServicioAutenticacion } from '../../nucleo/servicios/servicio-autenticacion';
import { ServicioPermisos } from '../../nucleo/servicios/servicio-permisos';
import { ServicioContadoresTabs } from '../../nucleo/servicios/servicio-contadores-tabs';
import { FilaAccesoComponent } from '../../compartido/componentes/fila-acceso/fila-acceso.component';

@Component({
  selector: 'app-pagina-perfil',
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
    IonSpinner,
    IonText,
    IonAvatar,
    FilaAccesoComponent
  ],
  template: `
    <ion-header>
      <ion-toolbar>
        <ion-title>Perfil</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding pagina-fondo">
      @if (cargando()) {
        <div class="centrado">
          <ion-spinner name="crescent"></ion-spinner>
        </div>
      } @else if (mensajeError()) {
        <ion-text color="danger"><p>{{ mensajeError() }}</p></ion-text>
      } @else if (empleado()) {
        <div class="perfil-encabezado">
          <ion-avatar>
            <div class="inicial">{{ inicial() }}</div>
          </ion-avatar>
          <h2>{{ empleado()!.nombre }} {{ empleado()!.apellido }}</h2>
          <p>{{ emailSesion() }}</p>
        </div>

        <ion-list class="lista-perfil">
          <ion-item>
            <ion-label>
              <h3>N.º empleado</h3>
              <p>{{ empleado()!.numeroEmpleado || '—' }}</p>
            </ion-label>
          </ion-item>
          <ion-item>
            <ion-label>
              <h3>DNI</h3>
              <p>{{ empleado()!.dni || '—' }}</p>
            </ion-label>
          </ion-item>
          <ion-item>
            <ion-label>
              <h3>CUIL</h3>
              <p>{{ empleado()!.cuil || '—' }}</p>
            </ion-label>
          </ion-item>
          <ion-item>
            <ion-label>
              <h3>Fecha de ingreso</h3>
              <p>{{ fechaIngreso() }}</p>
            </ion-label>
          </ion-item>
        </ion-list>

        <h3 class="grupo-lista-titulo">Tu espacio</h3>
        <div class="lista-accesos">
          <app-fila-acceso
            titulo="Notificaciones"
            subtitulo="Alertas y novedades"
            icono="notifications-outline"
            ruta="/tabs/notificaciones"
            [badge]="contadores().notificaciones"
          />
          <app-fila-acceso
            titulo="Avisos"
            subtitulo="Comunicados de la empresa"
            icono="megaphone-outline"
            ruta="/tabs/comunicaciones"
          />
          @if (servicioPermisos.tienePermiso('activo.ver')) {
            <app-fila-acceso
              titulo="Mis equipos"
              subtitulo="Lo que te asignaron"
              icono="laptop-outline"
              ruta="/tabs/mis-activos"
            />
          }
          <app-fila-acceso
            titulo="Solicitudes"
            subtitulo="Licencias y trámites"
            icono="document-text-outline"
            ruta="/tabs/solicitudes"
          />
          @if (onboardingActivo()) {
            <app-fila-acceso
              titulo="Primeros pasos"
              subtitulo="Completá tu ingreso"
              icono="checkmark-done-outline"
              ruta="/tabs/onboarding"
            />
          }
          <app-fila-acceso
            titulo="Ayuda"
            subtitulo="Preguntas frecuentes"
            icono="help-circle-outline"
            ruta="/tabs/ayuda"
          />
          <app-fila-acceso
            titulo="Configuración"
            subtitulo="Apariencia y versión"
            icono="settings-outline"
            ruta="/tabs/configuracion"
          />
        </div>
      }

      <ion-button expand="block" type="button" color="danger" fill="outline" (click)="confirmarCerrarSesion()">
        Cerrar sesión
      </ion-button>
    </ion-content>
  `,
  styles: `
    .centrado {
      display: flex;
      justify-content: center;
      padding: 2rem;
    }

    .perfil-encabezado {
      text-align: center;
      margin: 1rem 0 1.5rem;
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

    h2 { margin: 0; color: var(--color-texto); }
    .perfil-encabezado p { margin: 0.25rem 0 0; color: var(--color-texto-secundario); }
    .lista-perfil {
      border-radius: 12px;
      overflow: hidden;
      margin-bottom: 1rem;
      border: 1px solid var(--color-borde-suave);
    }
    .lista-accesos {
      display: flex;
      flex-direction: column;
      gap: var(--espacio-3);
      margin-bottom: var(--espacio-6);
    }
    ion-button { margin-top: 0.5rem; margin-bottom: 2rem; min-height: 48px; }
  `
})
export class PaginaPerfilComponent implements OnInit {
  readonly servicioPermisos = inject(ServicioPermisos);
  private readonly servicioEmpleados = inject(ServicioEmpleados);
  private readonly servicioAutenticacion = inject(ServicioAutenticacion);
  private readonly servicioContadores = inject(ServicioContadoresTabs);
  private readonly alertController = inject(AlertController);

  readonly empleado = signal<EmpleadoResumen | null>(null);
  readonly emailSesion = signal(this.servicioAutenticacion.sesionActual()?.email ?? '');
  readonly cargando = signal(true);
  readonly mensajeError = signal<string | null>(null);
  readonly onboardingActivo = signal(false);
  readonly contadores = this.servicioContadores.contadores;

  ngOnInit(): void {
    this.cargarPerfil();
    this.servicioContadores.refrescar();
  }

  inicial(): string {
    const e = this.empleado();
    return e ? e.nombre.charAt(0).toUpperCase() : '?';
  }

  fechaIngreso(): string {
    const raw = this.empleado()?.fechaIngreso;
    if (!raw) return '—';
    const fecha = new Date(raw);
    if (Number.isNaN(fecha.getTime())) return raw;
    return fecha.toLocaleDateString('es-AR');
  }

  async confirmarCerrarSesion(): Promise<void> {
    const alerta = await this.alertController.create({
      header: 'Cerrar sesión',
      message: '¿Seguro que querés salir de Ligo?',
      buttons: [
        { text: 'Cancelar', role: 'cancel' },
        {
          text: 'Salir',
          role: 'destructive',
          handler: () => this.servicioAutenticacion.cerrarSesion()
        }
      ]
    });
    await alerta.present();
  }

  private cargarPerfil(): void {
    this.servicioEmpleados.obtenerPropio().subscribe({
      next: (empleado) => {
        this.empleado.set(empleado);
        this.servicioEmpleados.obtenerLegajo(empleado.id).subscribe({
          next: (legajo) => this.onboardingActivo.set(legajo?.estadoOnboarding === 'EN_PROGRESO'),
          error: () => this.onboardingActivo.set(false)
        });
        this.cargando.set(false);
      },
      error: () => {
        this.mensajeError.set('No se pudo cargar tu perfil.');
        this.cargando.set(false);
      }
    });
  }
}
