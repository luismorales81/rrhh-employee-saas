import { Component, inject, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { ViewWillEnter } from '@ionic/angular';
import {
  IonContent,
  IonHeader,
  IonToolbar,
  IonButtons,
  IonButton,
  IonIcon,
  IonBadge,
  IonRefresher,
  IonRefresherContent,
  IonText
} from '@ionic/angular/standalone';
import { forkJoin, of } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { addIcons } from 'ionicons';
import {
  notificationsOutline,
  chevronForwardOutline,
  timeOutline,
  calendarOutline,
  medkitOutline,
  documentAttachOutline
} from 'ionicons/icons';
import { ServicioSolicitudes } from '../../nucleo/servicios/servicio-solicitudes';
import { ServicioDocumentos } from '../../nucleo/servicios/servicio-documentos';
import { ServicioComunicaciones } from '../../nucleo/servicios/servicio-comunicaciones';
import { ServicioNotificaciones } from '../../nucleo/servicios/servicio-notificaciones';
import { ServicioEmpleados } from '../../nucleo/servicios/servicio-empleados';
import { ServicioOnboarding } from '../../nucleo/servicios/servicio-onboarding';
import { ServicioMensajes } from '../../nucleo/servicios/servicio-mensajes';
import { ServicioPermisos } from '../../nucleo/servicios/servicio-permisos';
import { ServicioRecibos } from '../../nucleo/servicios/servicio-recibos';
import { ServicioFichajes } from '../../nucleo/servicios/servicio-fichajes';
import { ServicioContadoresTabs } from '../../nucleo/servicios/servicio-contadores-tabs';
import { ServicioPreferencias } from '../../nucleo/servicios/servicio-preferencias';
import { ServicioAnalitica } from '../../nucleo/servicios/servicio-analitica';
import { EstadoCargaComponent } from '../../compartido/componentes/estado-carga/estado-carga.component';
import { BloqueOnboardingComponent } from '../../compartido/componentes/bloque-onboarding/bloque-onboarding.component';
import { mensajeDesdeErrorHttp } from '../../nucleo/utilidades/mensajes-error-producto';
import { environment } from '../../../environments/environment';

addIcons({
  notificationsOutline,
  chevronForwardOutline,
  timeOutline,
  calendarOutline,
  medkitOutline,
  documentAttachOutline
});

interface PendienteInicio {
  id: string;
  titulo: string;
  detalle: string;
  ruta: string;
  prioridad: number;
}

@Component({
  selector: 'app-pagina-inicio',
  standalone: true,
  imports: [
    RouterLink,
    IonContent,
    IonHeader,
    IonToolbar,
    IonButtons,
    IonButton,
    IonIcon,
    IonBadge,
    IonRefresher,
    IonRefresherContent,
    IonText,
    EstadoCargaComponent,
    BloqueOnboardingComponent
  ],
  template: `
    <ion-header class="header-inicio">
      <ion-toolbar>
        <div class="saludo-toolbar" slot="start">
          <p class="fecha">{{ fechaHoy }}</p>
          <h1>Hola{{ nombreUsuario() ? ', ' + nombreUsuario() : '' }}</h1>
        </div>
        <ion-buttons slot="end">
          <ion-button routerLink="/tabs/notificaciones" aria-label="Notificaciones">
            <ion-icon slot="icon-only" name="notifications-outline"></ion-icon>
            @if (notificacionesNoLeidas() > 0) {
              <ion-badge color="danger">{{ notificacionesNoLeidas() > 9 ? '9+' : notificacionesNoLeidas() }}</ion-badge>
            }
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding contenido-inicio">
      <ion-refresher slot="fixed" (ionRefresh)="recargar($event)">
        <ion-refresher-content></ion-refresher-content>
      </ion-refresher>

      @if (mensajeError()) {
        <ion-text color="danger"><p>{{ mensajeError() }}</p></ion-text>
      } @else if (cargando()) {
        <app-estado-carga modo="lista" mensaje="Cargando tu espacio..." />
      } @else {
        @if (mostrarTipNavegacion()) {
          <div class="tip-navegacion">
            <div>
              <strong>Nueva organización</strong>
              <p>Ahora tenés Mi tiempo y Mis papeles. Todo importante en menos toques.</p>
            </div>
            <button type="button" (click)="cerrarTipNavegacion()">Entendido</button>
          </div>
        }

        @if (tareasOnboarding().length > 0) {
          <app-bloque-onboarding
            [tareas]="tareasOnboarding()"
            [procesando]="procesandoOnboarding()"
            (completar)="marcarTareaOnboarding($event)"
          />
        }

        <section class="seccion">
          <h2 class="titulo-seccion">Para hoy</h2>
          @if (pendientes().length === 0) {
            <div class="card-tranquila">
              <strong>Nada urgente</strong>
              <p>Cuando tengas algo pendiente, va a aparecer acá.</p>
            </div>
          } @else {
            <div class="lista-pendientes">
              @for (p of pendientes(); track p.id) {
                <a class="card-pendiente" href="#" (click)="abrirPendiente($event, p)">
                  <div>
                    <strong>{{ p.titulo }}</strong>
                    <span>{{ p.detalle }}</span>
                  </div>
                  <ion-icon name="chevron-forward-outline" aria-hidden="true"></ion-icon>
                </a>
              }
            </div>
          }
        </section>

        <section class="seccion">
          <h2 class="titulo-seccion">Acciones rápidas</h2>
          <div class="chips-acciones">
            @if (servicioPermisos.tienePermiso('fichaje.registrar')) {
              <a class="chip-accion primario" routerLink="/tabs/fichaje">
                <ion-icon name="time-outline" aria-hidden="true"></ion-icon>
                {{ etiquetaFichar() }}
              </a>
            }
            <a class="chip-accion" routerLink="/tabs/vacaciones">
              <ion-icon name="calendar-outline" aria-hidden="true"></ion-icon>
              Vacaciones
            </a>
            <a class="chip-accion" routerLink="/tabs/ausentismo">
              <ion-icon name="medkit-outline" aria-hidden="true"></ion-icon>
              Avisar falta
            </a>
            <a class="chip-accion" routerLink="/tabs/ausentismo">
              <ion-icon name="document-attach-outline" aria-hidden="true"></ion-icon>
              Certificado
            </a>
          </div>
        </section>

        @if (avisosRecientes().length > 0) {
          <section class="seccion">
            <div class="cabecera-seccion">
              <h2 class="titulo-seccion">Novedades</h2>
              <a routerLink="/tabs/comunicaciones">Ver todas</a>
            </div>
            <div class="lista-pendientes">
              @for (a of avisosRecientes(); track a.id) {
                <a class="card-pendiente suave" routerLink="/tabs/comunicaciones">
                  <div>
                    <strong>{{ a.titulo }}</strong>
                    <span>{{ a.detalle }}</span>
                  </div>
                  <ion-icon name="chevron-forward-outline" aria-hidden="true"></ion-icon>
                </a>
              }
            </div>
          </section>
        }
      }
    </ion-content>
  `,
  styles: `
    .header-inicio ion-toolbar {
      --padding-start: 1rem;
      --padding-end: 0.5rem;
    }

    .saludo-toolbar {
      padding: 0.5rem 0;
    }

    .saludo-toolbar .fecha {
      margin: 0;
      font-size: 0.75rem;
      font-weight: 600;
      text-transform: capitalize;
      color: var(--color-texto-secundario);
    }

    .saludo-toolbar h1 {
      margin: 0.1rem 0 0;
      font-size: 1.375rem;
      font-weight: 700;
      letter-spacing: -0.02em;
      color: var(--color-texto);
    }

    ion-button {
      position: relative;
    }

    ion-badge {
      position: absolute;
      top: 0;
      right: 0;
      font-size: 0.625rem;
      min-width: 1.1rem;
      height: 1.1rem;
      padding: 0 0.25rem;
    }

    .seccion {
      margin-bottom: var(--espacio-6);
    }

    .titulo-seccion {
      margin: 0 0 var(--espacio-3);
      font-size: 0.8125rem;
      font-weight: 700;
      letter-spacing: 0.04em;
      text-transform: uppercase;
      color: var(--color-texto-terciario);
    }

    .cabecera-seccion {
      display: flex;
      justify-content: space-between;
      align-items: baseline;
      margin-bottom: var(--espacio-3);
    }

    .cabecera-seccion .titulo-seccion {
      margin: 0;
    }

    .cabecera-seccion a {
      font-size: 0.8125rem;
      font-weight: 600;
      color: var(--color-secundario);
      text-decoration: none;
    }

    .card-tranquila {
      background: var(--color-superficie);
      border: 1px solid var(--color-borde-suave);
      border-radius: var(--radio-xl);
      padding: var(--espacio-5);
      box-shadow: var(--sombra-sm);
    }

    .card-tranquila strong {
      display: block;
      font-size: 1rem;
      color: var(--color-texto);
    }

    .card-tranquila p {
      margin: 0.35rem 0 0;
      color: var(--color-texto-secundario);
      font-size: 0.875rem;
    }

    .lista-pendientes {
      display: flex;
      flex-direction: column;
      gap: var(--espacio-3);
    }

    .card-pendiente {
      display: flex;
      align-items: center;
      gap: var(--espacio-3);
      padding: var(--espacio-4);
      min-height: 64px;
      background: var(--color-superficie);
      border: 1px solid var(--color-alerta-borde);
      border-radius: var(--radio-xl);
      text-decoration: none;
      color: inherit;
      box-shadow: var(--sombra-sm);
    }

    .card-pendiente.suave {
      border-color: var(--color-borde-suave);
    }

    .card-pendiente div {
      flex: 1;
      min-width: 0;
      display: flex;
      flex-direction: column;
      gap: 0.2rem;
    }

    .card-pendiente strong {
      font-size: 0.9375rem;
      color: var(--color-texto);
    }

    .card-pendiente span {
      font-size: 0.8125rem;
      color: var(--color-texto-secundario);
    }

    .card-pendiente ion-icon {
      color: var(--color-texto-terciario);
      flex-shrink: 0;
    }

    .chips-acciones {
      display: flex;
      flex-wrap: wrap;
      gap: var(--espacio-2);
    }

    .chip-accion {
      display: inline-flex;
      align-items: center;
      gap: 0.4rem;
      min-height: 44px;
      padding: 0.6rem 0.9rem;
      border-radius: var(--radio-completo);
      background: var(--color-superficie);
      border: 1px solid var(--color-borde);
      color: var(--color-texto);
      text-decoration: none;
      font-size: 0.875rem;
      font-weight: 600;
    }

    .chip-accion.primario {
      background: var(--color-primario);
      border-color: var(--color-primario);
      color: #fff;
    }

    .chip-accion ion-icon {
      font-size: 1.1rem;
    }

    .tip-navegacion {
      display: flex;
      align-items: flex-start;
      gap: var(--espacio-3);
      margin-bottom: var(--espacio-5);
      padding: var(--espacio-4);
      border-radius: var(--radio-xl);
      background: var(--color-secundario-suave);
      border: 1px solid var(--color-primario-borde);
      animation: tip-entrada 320ms ease-out;
    }

    .tip-navegacion strong {
      display: block;
      color: var(--color-texto);
      font-size: 0.95rem;
    }

    .tip-navegacion p {
      margin: 0.25rem 0 0;
      color: var(--color-texto-secundario);
      font-size: 0.85rem;
      line-height: 1.4;
    }

    .tip-navegacion button {
      flex-shrink: 0;
      border: 0;
      background: var(--color-primario);
      color: #fff;
      font-weight: 600;
      font-size: 0.8rem;
      border-radius: var(--radio-completo);
      padding: 0.55rem 0.85rem;
      min-height: 40px;
    }

    @keyframes tip-entrada {
      from { opacity: 0; transform: translateY(-6px); }
      to { opacity: 1; transform: translateY(0); }
    }
  `
})
export class PaginaInicioComponent implements OnInit, ViewWillEnter {
  readonly servicioPermisos = inject(ServicioPermisos);
  private readonly servicioSolicitudes = inject(ServicioSolicitudes);
  private readonly servicioDocumentos = inject(ServicioDocumentos);
  private readonly servicioComunicaciones = inject(ServicioComunicaciones);
  private readonly servicioNotificaciones = inject(ServicioNotificaciones);
  private readonly servicioEmpleados = inject(ServicioEmpleados);
  private readonly servicioOnboarding = inject(ServicioOnboarding);
  private readonly servicioRecibos = inject(ServicioRecibos);
  private readonly servicioFichajes = inject(ServicioFichajes);
  private readonly servicioMensajes = inject(ServicioMensajes);
  private readonly servicioContadores = inject(ServicioContadoresTabs);
  private readonly servicioPreferencias = inject(ServicioPreferencias);
  private readonly servicioAnalitica = inject(ServicioAnalitica);
  private readonly router = inject(Router);

  readonly fechaHoy = new Date().toLocaleDateString('es-AR', {
    weekday: 'long',
    day: 'numeric',
    month: 'short'
  });
  readonly nombreUsuario = signal('');
  readonly cargando = signal(true);
  readonly mensajeError = signal<string | null>(null);
  readonly pendientes = signal<PendienteInicio[]>([]);
  readonly avisosRecientes = signal<PendienteInicio[]>([]);
  readonly notificacionesNoLeidas = signal(0);
  readonly etiquetaFichar = signal('Fichar');
  readonly tareasOnboarding = signal<{ id: number; nombre: string; completada: boolean }[]>([]);
  readonly procesandoOnboarding = signal(false);
  readonly mostrarTipNavegacion = signal(
    environment.navegacionV2 && !this.servicioPreferencias.tipNavegacionVisto()
  );
  private empleadoId = 0;

  ngOnInit(): void {
    this.cargarResumen();
  }

  ionViewWillEnter(): void {
    if (!this.cargando()) {
      this.cargarResumen();
    }
  }

  abrirPendiente(evento: Event, pendiente: PendienteInicio): void {
    evento.preventDefault();
    this.servicioAnalitica.homePendienteTap(pendiente.ruta);
    void this.router.navigateByUrl(pendiente.ruta);
  }

  recargar(evento: CustomEvent): void {
    this.cargarResumen(() => (evento.target as HTMLIonRefresherElement).complete());
  }

  marcarTareaOnboarding(tareaId: number): void {
    if (!this.empleadoId) return;
    this.procesandoOnboarding.set(true);
    this.servicioOnboarding.completarTarea(this.empleadoId, tareaId).subscribe({
      next: () => {
        this.procesandoOnboarding.set(false);
        void this.servicioMensajes.exito('Tarea completada.');
        this.cargarTareasOnboarding();
      },
      error: () => {
        this.procesandoOnboarding.set(false);
        this.mensajeError.set('No se pudo completar la tarea');
      }
    });
  }

  cerrarTipNavegacion(): void {
    this.servicioPreferencias.marcarTipNavegacionVisto();
    this.mostrarTipNavegacion.set(false);
  }

  private cargarResumen(alFinalizar?: () => void): void {
    this.cargando.set(true);
    this.mensajeError.set(null);

    this.servicioEmpleados.obtenerPropio().pipe(
      switchMap((empleado) => {
        this.empleadoId = empleado.id;
        this.nombreUsuario.set(empleado.nombre);
        return forkJoin({
          solicitudes: this.servicioSolicitudes.listarPropias().pipe(catchError(() => of([]))),
          documentos: this.servicioDocumentos.listarMisAsignaciones().pipe(catchError(() => of([]))),
          comunicaciones: this.servicioComunicaciones.listarMisComunicaciones().pipe(catchError(() => of([]))),
          notificaciones: this.servicioNotificaciones.contarNoLeidas().pipe(
            catchError(() => of({ cantidad: 0 }))
          ),
          legajo: this.servicioEmpleados.obtenerLegajo(empleado.id).pipe(catchError(() => of(null))),
          recibos: this.servicioRecibos.listarPropios().pipe(catchError(() => of([]))),
          fichaje: this.servicioPermisos.tienePermiso('fichaje.registrar')
            ? this.servicioFichajes.obtenerEstadoDia().pipe(catchError(() => of(null)))
            : of(null)
        });
      })
    ).subscribe({
      next: ({ solicitudes, documentos, comunicaciones, notificaciones, legajo, recibos, fichaje }) => {
        const lista: PendienteInicio[] = [];

        for (const r of recibos.filter((x) => x.estado === 'PENDIENTE' || x.estado === 'VISTO')) {
          lista.push({
            id: `recibo-${r.id}`,
            titulo: 'Firmar recibo',
            detalle: r.periodo || r.titulo,
            ruta: `/tabs/recibos/${r.id}`,
            prioridad: 1
          });
        }

        for (const d of documentos.filter((x) => x.estado !== 'LEIDO' && (!x.requiereFirma || !x.firmado))) {
          lista.push({
            id: `doc-${d.id}`,
            titulo: d.requiereFirma && !d.firmado ? 'Documento para firmar' : 'Documento para revisar',
            detalle: d.tituloDocumento || 'Documento asignado',
            ruta: '/tabs/documentos',
            prioridad: 2
          });
        }

        for (const s of solicitudes
          .filter((x) => ['BORRADOR', 'CAMBIOS_SOLICITADOS'].includes(x.estado))
          .slice(0, 2)) {
          lista.push({
            id: `sol-${s.id}`,
            titulo: 'Solicitud incompleta',
            detalle: s.tipoSolicitudNombre || 'Revisá tu solicitud',
            ruta: '/tabs/solicitudes',
            prioridad: 3
          });
        }

        for (const c of comunicaciones
          .filter((x) => !x.leida || (x.requiereConfirmacion && !x.confirmada))
          .slice(0, 2)) {
          lista.push({
            id: `aviso-${c.id}`,
            titulo: c.requiereConfirmacion && !c.confirmada ? 'Confirmar aviso' : 'Nuevo aviso',
            detalle: c.tituloComunicacion,
            ruta: '/tabs/comunicaciones',
            prioridad: 4
          });
        }

        if (legajo?.estadoOnboarding === 'EN_PROGRESO') {
          lista.push({
            id: 'onboarding',
            titulo: 'Primeros pasos',
            detalle: 'Completá tu ingreso',
            ruta: '/tabs/onboarding',
            prioridad: 5
          });
          this.cargarTareasOnboarding();
        } else {
          this.tareasOnboarding.set([]);
        }

        lista.sort((a, b) => a.prioridad - b.prioridad);
        this.pendientes.set(lista.slice(0, 5));

        this.avisosRecientes.set(
          comunicaciones
            .filter((c) => !c.leida)
            .slice(0, 3)
            .map((c) => ({
              id: `nov-${c.id}`,
              titulo: c.tituloComunicacion,
              detalle: 'Tocá para leer',
              ruta: '/tabs/comunicaciones',
              prioridad: 0
            }))
        );

        this.notificacionesNoLeidas.set(notificaciones.cantidad);

        if (!fichaje?.fichajeHabilitado) {
          this.etiquetaFichar.set('Fichar');
        } else if (fichaje.ultimaMarcaTipo === 'ENTRADA') {
          this.etiquetaFichar.set('Fichar salida');
        } else {
          this.etiquetaFichar.set('Fichar entrada');
        }

        this.servicioContadores.refrescar();
        this.cargando.set(false);
        alFinalizar?.();
      },
      error: (err) => {
        this.mensajeError.set(mensajeDesdeErrorHttp(err, 'cargar tu inicio'));
        this.cargando.set(false);
        alFinalizar?.();
      }
    });
  }

  private cargarTareasOnboarding(): void {
    if (!this.empleadoId) return;
    this.servicioOnboarding.listarTareasEmpleado(this.empleadoId).pipe(
      catchError(() => of([]))
    ).subscribe((tareas) => this.tareasOnboarding.set(tareas));
  }
}
