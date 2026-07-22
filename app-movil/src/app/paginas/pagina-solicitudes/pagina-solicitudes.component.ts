import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { switchMap, throwError, forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import {
  IonContent,
  IonHeader,
  IonTitle,
  IonToolbar,
  IonButton,
  IonFab,
  IonFabButton,
  IonIcon,
  IonModal,
  IonTextarea,
  IonText,
  IonItem,
  IonLabel,
  IonList,
  IonSpinner,
  IonInput,
  IonNote
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { addOutline } from 'ionicons/icons';
import { ServicioMensajes } from '../../nucleo/servicios/servicio-mensajes';
import {
  ServicioSolicitudes,
  SolicitudResumen,
  TipoSolicitudResumen
} from '../../nucleo/servicios/servicio-solicitudes';
import {
  ErrorValidacionVacaciones,
  RespuestaSimularFechaVacaciones,
  ServicioVacaciones,
  SaldoVacacionesResumen,
  SolicitudVacacionesResumen
} from '../../nucleo/servicios/servicio-vacaciones';
import { EstadoCargaComponent } from '../../compartido/componentes/estado-carga/estado-carga.component';
import { EstadoVacioComponent } from '../../compartido/componentes/estado-vacio/estado-vacio.component';
import { EncabezadoPaginaComponent } from '../../compartido/componentes/encabezado-pagina/encabezado-pagina.component';
import {
  AccionTarjetaFila,
  TarjetaFilaComponent
} from '../../compartido/componentes/tarjeta-fila/tarjeta-fila.component';
import {
  calcularDiasSolicitados,
  extraerErroresVacaciones,
  validarPeriodoVacaciones
} from '../../nucleo/utilidades/validacion-vacaciones';
import {
  esEstadoSolicitudPendiente,
  formatearEstadoSolicitud
} from '../../nucleo/utilidades/formatear-estado-solicitud';
import {
  enriquecerSolicitudesConVacaciones,
  esTipoVacaciones
} from '../../nucleo/utilidades/enriquecer-solicitudes';

addIcons({ addOutline });

@Component({
  selector: 'app-pagina-solicitudes',
  standalone: true,
  imports: [
    DatePipe,
    FormsModule,
    IonContent,
    IonHeader,
    IonTitle,
    IonToolbar,
    IonButton,
    IonFab,
    IonFabButton,
    IonIcon,
    IonModal,
    IonTextarea,
    IonText,
    IonItem,
    IonLabel,
    IonList,
    IonSpinner,
    IonInput,
    IonNote,
    EstadoCargaComponent,
    EstadoVacioComponent,
    EncabezadoPaginaComponent,
    TarjetaFilaComponent
  ],
  template: `
    <ion-header>
      <ion-toolbar>
        <ion-title>Solicitudes</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding pagina-fondo">
      <app-encabezado-pagina
        titulo="Solicitudes"
        subtitulo="Licencias, trámites y seguimiento de pedidos"
      />

      @if (cargando()) {
        <app-estado-carga />
      } @else if (mensajeError()) {
        <ion-text color="danger"><p>{{ mensajeError() }}</p></ion-text>
      } @else if (solicitudesVisibles().length === 0) {
        <app-estado-vacio
          icono="document-text-outline"
          titulo="Sin solicitudes"
          descripcion="Tocá el botón + para crear una nueva solicitud."
        />
      } @else {
        <div class="lista-tarjetas">
          @for (solicitud of solicitudesVisibles(); track solicitud.id) {
            <app-tarjeta-fila
              [titulo]="solicitud.tipoSolicitudNombre"
              [subtitulo]="subtituloSolicitud(solicitud)"
              [meta]="(solicitud.createdAt | date: 'dd/MM/yyyy HH:mm') ?? ''"
              [etiqueta]="etiquetaEstado(solicitud.estado)"
              [alerta]="esEstadoSolicitudPendiente(solicitud.estado)"
              [acciones]="accionesPara(solicitud)"
            />
          }
        </div>
      }

      <ion-fab slot="fixed" vertical="bottom" horizontal="end">
        <ion-fab-button type="button" (click)="abrirModal()">
          <ion-icon name="add-outline"></ion-icon>
        </ion-fab-button>
      </ion-fab>
    </ion-content>

    <ion-modal [isOpen]="modalAbierto()" (didDismiss)="cerrarModal()">
      <ng-template>
        <ion-header>
          <ion-toolbar>
            <ion-title>Nueva solicitud</ion-title>
            <ion-button slot="end" fill="clear" type="button" (click)="cerrarModal()">Cerrar</ion-button>
          </ion-toolbar>
        </ion-header>
        <ion-content class="ion-padding">
          <p class="etiqueta-campo">Tipo de solicitud *</p>

          @if (cargandoTipos()) {
            <app-estado-carga mensaje="Cargando tipos..." />
          } @else if (errorTipos()) {
            <ion-text color="danger"><p>{{ errorTipos() }}</p></ion-text>
            <ion-button expand="block" fill="outline" type="button" (click)="cargarTipos()">
              Reintentar
            </ion-button>
          } @else if (tipos().length === 0) {
            <ion-text color="medium">
              <p>No hay tipos de solicitud configurados. Pedile a tu equipo que los configure.</p>
            </ion-text>
          } @else {
            <ion-list class="lista-tipos">
              @for (tipo of tipos(); track tipo.id) {
                <ion-item
                  button
                  type="button"
                  [class.tipo-seleccionado]="tipoSeleccionadoId() === tipo.id"
                  (click)="seleccionarTipo(tipo.id)"
                >
                  <ion-label>
                    <h3>{{ tipo.nombre }}</h3>
                    @if (tipo.descripcion) {
                      <p>{{ tipo.descripcion }}</p>
                    }
                  </ion-label>
                </ion-item>
              }
            </ion-list>
          }

          @if (tipoSeleccionado()) {
            @if (esVacaciones()) {
              <ion-note class="nota-ayuda">
                Las vacaciones se gestionan con validación LCT (mínimo 7 días y patrón de descanso).
              </ion-note>
              @if (cargandoSaldo()) {
                <ion-note class="nota-saldo">Cargando saldo disponible...</ion-note>
              } @else if (errorSaldo()) {
                <ion-text color="danger"><p class="nota-saldo">{{ errorSaldo() }}</p></ion-text>
              } @else {
                @if (saldoVacaciones(); as saldo) {
                  <p class="resumen-saldo">
                    {{ saldo.diasDisponibles }} día(s) disponibles · {{ saldo.politicaNombre }}
                  </p>
                }
              }
            }
            @if (esLicencia()) {
              <ion-note class="nota-ayuda">
                Indicá el período de la licencia. Tu equipo revisará tu pedido.
              </ion-note>
            }

            @if (requiereFechas()) {
              <ion-input
                label="Fecha inicio *"
                labelPlacement="stacked"
                type="date"
                [(ngModel)]="fechaInicio"
                (ionInput)="alCambiarFechas()"
              />
              <ion-input
                label="Fecha fin *"
                labelPlacement="stacked"
                type="date"
                [(ngModel)]="fechaFin"
                (ionInput)="alCambiarFechas()"
              />

              @if (esVacaciones() && diasSolicitados() > 0) {
                <p class="resumen-dias" [class.resumen-dias--alerta]="excedeSaldo()">
                  {{ diasSolicitados() }} día(s) solicitado(s)
                </p>
              }

              @if (esVacaciones() && simulacionFecha()) {
                @if (!simulacionFecha()!.esValida) {
                  <ion-text color="warning">
                    <p>{{ simulacionFecha()!.motivo }}</p>
                  </ion-text>
                  @if (simulacionFecha()!.fechaSugerida) {
                    <ion-button
                      expand="block"
                      fill="outline"
                      size="small"
                      type="button"
                      (click)="aplicarFechaSugerida()"
                    >
                      Usar fecha sugerida: {{ simulacionFecha()!.fechaSugerida }}
                    </ion-button>
                  }
                }
              }
            }
          }

          <ion-textarea label="Motivo" labelPlacement="stacked" [(ngModel)]="motivo" autoGrow="true" />

          @if (erroresValidacion().length) {
            <div class="bloque-errores">
              @for (error of erroresValidacion(); track error) {
                <ion-text color="danger"><p>{{ error }}</p></ion-text>
              }
            </div>
          }

          @if (advertencias().length) {
            <div class="bloque-advertencias">
              @for (aviso of advertencias(); track aviso) {
                <ion-text color="warning"><p>{{ aviso }}</p></ion-text>
              }
            </div>
          }

          <ion-button
            expand="block"
            type="button"
            (click)="crearSolicitud()"
            [disabled]="botonEnvioDeshabilitado()"
          >
            @if (guardando()) {
              <ion-spinner name="crescent"></ion-spinner>
            } @else {
              {{ esVacaciones() ? 'Solicitar vacaciones' : 'Crear y enviar' }}
            }
          </ion-button>
        </ion-content>
      </ng-template>
    </ion-modal>
  `,
  styles: `
    ion-textarea, ion-input { margin: 0.75rem 0; display: block; }
    .etiqueta-campo {
      margin: 0 0 0.5rem;
      font-size: 0.85rem;
      font-weight: 600;
      color: var(--ion-color-medium);
    }
    .lista-tipos {
      border-radius: 12px;
      overflow: hidden;
      margin-bottom: 0.5rem;
    }
    .tipo-seleccionado {
      --background: rgba(0, 84, 233, 0.08);
      --color: var(--ion-color-primary);
    }
    .nota-ayuda {
      display: block;
      margin: 0.5rem 0 0.25rem;
      font-size: 0.8rem;
      line-height: 1.4;
    }
    .resumen-dias {
      margin: 0.25rem 0 0.5rem;
      font-size: 0.85rem;
      font-weight: 600;
      color: var(--ion-color-primary);
    }
    .resumen-dias--alerta {
      color: var(--ion-color-danger);
    }
    .resumen-saldo {
      margin: 0.25rem 0 0.5rem;
      font-size: 0.85rem;
      font-weight: 600;
      color: var(--ion-color-medium);
    }
    .nota-saldo {
      display: block;
      margin: 0.25rem 0;
      font-size: 0.8rem;
    }
    .bloque-errores p,
    .bloque-advertencias p {
      margin: 0.25rem 0;
      font-size: 0.85rem;
    }
  `
})
export class PaginaSolicitudesComponent implements OnInit {
  private readonly servicioSolicitudes = inject(ServicioSolicitudes);
  private readonly servicioVacaciones = inject(ServicioVacaciones);
  private readonly servicioMensajes = inject(ServicioMensajes);
  private readonly router = inject(Router);

  private readonly diasMinimosVacaciones = 7;

  readonly solicitudes = signal<SolicitudResumen[]>([]);
  readonly tipos = signal<TipoSolicitudResumen[]>([]);
  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly mensajeError = signal<string | null>(null);
  readonly modalAbierto = signal(false);
  readonly cargandoTipos = signal(false);
  readonly errorTipos = signal<string | null>(null);
  readonly erroresValidacion = signal<string[]>([]);
  readonly advertencias = signal<string[]>([]);
  readonly simulacionFecha = signal<RespuestaSimularFechaVacaciones | null>(null);
  readonly saldoVacaciones = signal<SaldoVacacionesResumen | null>(null);
  readonly cargandoSaldo = signal(false);
  readonly simulandoFecha = signal(false);
  readonly errorSaldo = signal<string | null>(null);
  readonly solicitudesVacaciones = signal<SolicitudVacacionesResumen[]>([]);

  private anioSaldoCargado: number | null = null;

  readonly tipoSeleccionadoId = signal<number | null>(null);
  motivo = '';
  fechaInicio = '';
  fechaFin = '';

  readonly tipoSeleccionado = computed(() => {
    const id = this.tipoSeleccionadoId();
    if (!id) {
      return null;
    }
    return this.tipos().find((tipo) => tipo.id === id) ?? null;
  });

  readonly solicitudesVisibles = computed(() =>
    this.solicitudes().filter((solicitud) => solicitud.estado !== 'CANCELADO')
  );

  ngOnInit(): void {
    this.cargarDatos();
  }

  esVacaciones(): boolean {
    return this.coincideNombreTipo('vacaciones');
  }

  esLicencia(): boolean {
    return this.coincideNombreTipo('licencia');
  }

  requiereFechas(): boolean {
    return this.esVacaciones() || this.esLicencia();
  }

  diasSolicitados(): number {
    return calcularDiasSolicitados(this.fechaInicio, this.fechaFin);
  }

  excedeSaldo(): boolean {
    const saldo = this.saldoVacaciones();
    if (!saldo) {
      return false;
    }
    const dias = this.diasSolicitados();
    return dias > 0 && dias > Number(saldo.diasDisponibles);
  }

  botonEnvioDeshabilitado(): boolean {
    if (this.guardando() || !this.tipoSeleccionadoId() || this.cargandoTipos()) {
      return true;
    }
    if (this.esVacaciones()) {
      return this.cargandoSaldo()
        || this.simulandoFecha()
        || !this.saldoVacaciones()
        || this.erroresValidacion().length > 0;
    }
    return this.erroresValidacion().length > 0;
  }

  subtituloSolicitud(solicitud: SolicitudResumen): string {
    if (solicitud.fechaInicioVacaciones && solicitud.fechaFinVacaciones) {
      return `${this.formatearFechaLegible(solicitud.fechaInicioVacaciones)} → ${this.formatearFechaLegible(solicitud.fechaFinVacaciones)}`;
    }
    if (esTipoVacaciones(solicitud.tipoSolicitudNombre)) {
      return 'Pedido incompleto — podés darlo de baja';
    }
    return solicitud.motivo || 'Sin detalle';
  }

  etiquetaEstado(estado: string): string {
    return formatearEstadoSolicitud(estado);
  }

  esEstadoSolicitudPendiente(estado: string): boolean {
    return esEstadoSolicitudPendiente(estado);
  }

  esSolicitudVacacionesIncompleta(solicitud: SolicitudResumen): boolean {
    return esTipoVacaciones(solicitud.tipoSolicitudNombre)
      && solicitud.solicitudVacacionesId == null;
  }

  accionesPara(solicitud: SolicitudResumen): AccionTarjetaFila[] {
    if (!this.puedeCancelar(solicitud.estado)) {
      return [];
    }

    return [{
      id: `cancelar-${solicitud.id}`,
      etiqueta: this.esSolicitudVacacionesIncompleta(solicitud) ? 'Dar de baja' : 'Cancelar',
      color: 'danger',
      relleno: 'outline',
      ejecutar: () => this.cancelarSolicitud(solicitud.id)
    }];
  }

  puedeCancelar(estado: string): boolean {
    return estado === 'BORRADOR'
      || estado === 'EN_REVISION'
      || estado === 'CAMBIOS_SOLICITADOS';
  }

  cancelarSolicitud(id: number): void {
    this.servicioSolicitudes.cancelar(id).subscribe({
      next: () => {
        void this.servicioMensajes.exito('Solicitud cancelada.');
        this.cargarDatos();
      },
      error: (err) => {
        void this.servicioMensajes.error(err.error?.mensaje ?? 'No se pudo cancelar la solicitud.');
      }
    });
  }

  abrirModal(): void {
    this.modalAbierto.set(true);
    this.cargarTipos();
  }

  cerrarModal(): void {
    this.modalAbierto.set(false);
    this.limpiarFormulario();
    this.errorTipos.set(null);
  }

  seleccionarTipo(id: number): void {
    this.tipoSeleccionadoId.set(id);
    this.erroresValidacion.set([]);
    this.advertencias.set([]);
    this.simulacionFecha.set(null);
    this.fechaInicio = '';
    this.fechaFin = '';
    this.saldoVacaciones.set(null);
    this.anioSaldoCargado = null;
    this.errorSaldo.set(null);
    this.solicitudesVacaciones.set([]);

    const tipo = this.tipos().find((item) => item.id === id);
    if (tipo?.nombre?.toLowerCase().includes('vacaciones')) {
      this.cerrarModal();
      void this.router.navigate(['/tabs/vacaciones']);
      return;
    }

    if (this.esVacaciones()) {
      this.cargarSaldoVacaciones();
      this.cargarSolicitudesVacaciones();
    }
  }

  alCambiarFechas(): void {
    if (this.esVacaciones()) {
      this.cargarSaldoVacaciones(this.anioDesdeFechaInicio());
      this.simularFechaInicio();
      return;
    }
    this.erroresValidacion.set([]);
  }

  aplicarFechaSugerida(): void {
    const simulacion = this.simulacionFecha();
    if (!simulacion?.fechaSugerida) {
      return;
    }
    this.fechaInicio = simulacion.fechaSugerida;
    const dias = this.diasSolicitados();
    if (dias > 0) {
      const fin = new Date(`${simulacion.fechaSugerida}T00:00:00`);
      fin.setDate(fin.getDate() + dias - 1);
      this.fechaFin = this.formatearFecha(fin);
    }
    this.simularFechaInicio();
  }

  cargarTipos(): void {
    this.cargandoTipos.set(true);
    this.errorTipos.set(null);

    this.servicioSolicitudes.listarTipos().subscribe({
      next: (tipos) => {
        this.tipos.set(tipos);
        this.cargandoTipos.set(false);
        if (tipos.length === 0) {
          this.errorTipos.set('No hay tipos de solicitud disponibles para tu empresa.');
        }
      },
      error: (err) => {
        this.cargandoTipos.set(false);
        this.errorTipos.set(
          err.status === 0
            ? 'No pudimos conectarnos. Revisá tu internet e intentá de nuevo.'
            : (err.error?.mensaje ?? 'No se pudieron cargar los tipos de solicitud.')
        );
      }
    });
  }

  crearSolicitud(): void {
    const tipoId = this.tipoSeleccionadoId();
    if (!tipoId) {
      void this.servicioMensajes.error('Seleccioná un tipo de solicitud.');
      return;
    }

    this.erroresValidacion.set([]);
    this.advertencias.set([]);

    if (this.esVacaciones()) {
      this.crearSolicitudVacaciones();
      return;
    }

    if (this.esLicencia()) {
      const errorFechas = this.validarFechasLicencia();
      if (errorFechas) {
        this.erroresValidacion.set([errorFechas]);
        return;
      }
    }

    this.guardando.set(true);
    const motivoFinal = this.esLicencia()
      ? this.armarMotivoLicencia()
      : (this.motivo || undefined);

    this.servicioSolicitudes
      .crear({ tipoSolicitudId: tipoId, motivo: motivoFinal })
      .subscribe({
        next: (solicitud) => {
          if (solicitud.estado === 'BORRADOR' || solicitud.estado === 'CAMBIOS_SOLICITADOS') {
            this.servicioSolicitudes.enviar(solicitud.id).subscribe({
              next: () => this.finalizarCreacionExitosa(),
              error: () => {
                this.guardando.set(false);
                void this.servicioMensajes.error('La solicitud se creó pero no pudo enviarse.');
                this.cargarDatos();
              }
            });
            return;
          }
          this.finalizarCreacionExitosa();
        },
        error: (err) => {
          this.guardando.set(false);
          void this.servicioMensajes.error(err.error?.mensaje ?? 'No se pudo crear la solicitud.');
        }
      });
  }

  private crearSolicitudVacaciones(): void {
    const errorFechas = this.validarFechasVacaciones();
    if (errorFechas.length) {
      this.erroresValidacion.set(errorFechas);
      void this.servicioMensajes.error(errorFechas[0]);
      return;
    }

    if (!this.saldoVacaciones()) {
      const mensaje = this.errorSaldo() ?? 'No se pudo verificar tu saldo de vacaciones.';
      this.erroresValidacion.set([mensaje]);
      void this.servicioMensajes.error(mensaje);
      return;
    }

    this.guardando.set(true);
    this.servicioVacaciones
      .simularFecha({
        fechaInicioDeseada: this.fechaInicio,
        diasDeseados: this.diasSolicitados()
      })
      .pipe(
        switchMap((simulacion) => {
          this.simulacionFecha.set(simulacion);
          if (!simulacion.esValida) {
            const motivo = simulacion.motivo
              ?? 'La fecha de inicio no es válida según tu patrón de descanso (Art. 151 LCT).';
            return throwError(() => ({
              error: { errores: [motivo] }
            }));
          }

          return this.servicioVacaciones.solicitar({
            fechaInicio: this.fechaInicio,
            fechaFin: this.fechaFin,
            motivo: this.motivo || undefined
          });
        })
      )
      .subscribe({
        next: (respuesta) => {
          this.guardando.set(false);
          this.cerrarModal();
          if (respuesta.advertencias?.length) {
            void this.servicioMensajes.exito(`Vacaciones solicitadas. ${respuesta.advertencias.join(' ')}`);
          } else {
            void this.servicioMensajes.exito('Solicitud de vacaciones enviada.');
          }
          void this.router.navigate(['/tabs/vacaciones']);
        },
        error: (err) => {
          this.guardando.set(false);
          this.mostrarErroresVacaciones(err);
        }
      });
  }

  private mostrarErroresVacaciones(error: unknown): void {
    const errores = extraerErroresVacaciones(error);
    this.erroresValidacion.set(errores);

    const advertencias = (error as { error?: ErrorValidacionVacaciones })?.error?.advertencias;
    if (advertencias?.length) {
      this.advertencias.set(advertencias);
    }

    void this.servicioMensajes.error(errores[0]);
  }

  private validarFechasLicencia(): string | null {
    if (!this.fechaInicio || !this.fechaFin) {
      return 'Indicá fecha de inicio y fin de la licencia.';
    }
    if (this.fechaFin < this.fechaInicio) {
      return 'La fecha fin no puede ser anterior a la fecha de inicio.';
    }
    return null;
  }

  private validarFechasVacaciones(): string[] {
    const diasDisponibles = this.saldoVacaciones()?.diasDisponibles;
    const errores = validarPeriodoVacaciones(
      this.fechaInicio,
      this.fechaFin,
      this.diasMinimosVacaciones,
      diasDisponibles != null ? Number(diasDisponibles) : null,
      this.solicitudesVacaciones()
    );

    const simulacion = this.simulacionFecha();
    if (simulacion && !simulacion.esValida) {
      errores.push(
        simulacion.motivo ?? 'La fecha de inicio no es válida según tu patrón de descanso (Art. 151 LCT).'
      );
    }

    return errores;
  }

  private actualizarErroresVacaciones(): void {
    if (!this.esVacaciones()) {
      return;
    }
    this.erroresValidacion.set(this.validarFechasVacaciones());
  }

  private cargarSaldoVacaciones(anio?: number): void {
    const anioConsulta = anio ?? this.anioDesdeFechaInicio();

    if (this.anioSaldoCargado === anioConsulta && this.saldoVacaciones() !== null) {
      this.actualizarErroresVacaciones();
      return;
    }

    this.cargandoSaldo.set(true);
    this.errorSaldo.set(null);
    this.servicioVacaciones.saldoPropio(anioConsulta).subscribe({
      next: (saldo) => {
        this.saldoVacaciones.set(saldo);
        this.anioSaldoCargado = anioConsulta;
        this.cargandoSaldo.set(false);
        this.actualizarErroresVacaciones();
      },
      error: (err) => {
        this.cargandoSaldo.set(false);
        this.saldoVacaciones.set(null);
        this.anioSaldoCargado = null;
        const mensaje = (err.error as { mensaje?: string })?.mensaje
          ?? 'No se pudo cargar tu saldo de vacaciones.';
        this.errorSaldo.set(mensaje);
        this.erroresValidacion.set([mensaje]);
      }
    });
  }

  private cargarSolicitudesVacaciones(): void {
    this.servicioVacaciones.listarPropias().subscribe({
      next: (lista) => {
        this.solicitudesVacaciones.set(lista);
        this.actualizarErroresVacaciones();
      },
      error: () => {}
    });
  }

  private anioDesdeFechaInicio(): number {
    if (this.fechaInicio) {
      return new Date(`${this.fechaInicio}T00:00:00`).getFullYear();
    }
    return new Date().getFullYear();
  }

  private armarMotivoLicencia(): string {
    const periodo = `Período: ${this.formatearFechaLegible(this.fechaInicio)} al ${this.formatearFechaLegible(this.fechaFin)}.`;
    const detalle = this.motivo?.trim();
    return detalle ? `${periodo} ${detalle}` : periodo;
  }

  private simularFechaInicio(): void {
    const dias = this.diasSolicitados();
    if (!this.fechaInicio || dias < 1) {
      this.simulacionFecha.set(null);
      this.actualizarErroresVacaciones();
      return;
    }

    this.simulandoFecha.set(true);
    this.servicioVacaciones.simularFecha({
      fechaInicioDeseada: this.fechaInicio,
      diasDeseados: dias
    }).subscribe({
      next: (resultado) => {
        this.simulacionFecha.set(resultado);
        this.simulandoFecha.set(false);
        this.actualizarErroresVacaciones();
      },
      error: () => {
        this.simulacionFecha.set(null);
        this.simulandoFecha.set(false);
        this.erroresValidacion.set([
          'No se pudo validar la fecha de inicio. Verificá tu conexión e intentá de nuevo.'
        ]);
      }
    });
  }

  private coincideNombreTipo(palabraClave: string): boolean {
    const nombre = this.tipoSeleccionado()?.nombre?.toLowerCase() ?? '';
    return nombre.includes(palabraClave);
  }

  private finalizarCreacionExitosa(): void {
    this.guardando.set(false);
    this.cerrarModal();
    void this.servicioMensajes.exito('Solicitud creada y enviada.');
    this.cargarDatos();
  }

  private limpiarFormulario(): void {
    this.tipoSeleccionadoId.set(null);
    this.motivo = '';
    this.fechaInicio = '';
    this.fechaFin = '';
    this.saldoVacaciones.set(null);
    this.anioSaldoCargado = null;
    this.cargandoSaldo.set(false);
    this.simulandoFecha.set(false);
    this.errorSaldo.set(null);
    this.solicitudesVacaciones.set([]);
    this.erroresValidacion.set([]);
    this.advertencias.set([]);
    this.simulacionFecha.set(null);
  }

  private cargarDatos(): void {
    this.cargando.set(true);
    this.mensajeError.set(null);

    forkJoin({
      solicitudes: this.servicioSolicitudes.listarPropias(),
      vacaciones: this.servicioVacaciones.listarPropias().pipe(catchError(() => of([])))
    }).subscribe({
      next: ({ solicitudes, vacaciones }) => {
        this.solicitudes.set(enriquecerSolicitudesConVacaciones(solicitudes, vacaciones));
        this.cargando.set(false);
      },
      error: (err) => {
        this.mensajeError.set(
          err.status === 0
            ? 'No pudimos conectarnos. Revisá tu internet e intentá de nuevo.'
            : 'No se pudieron cargar las solicitudes.'
        );
        this.cargando.set(false);
      }
    });
  }

  private formatearFecha(fecha: Date): string {
    const anio = fecha.getFullYear();
    const mes = String(fecha.getMonth() + 1).padStart(2, '0');
    const dia = String(fecha.getDate()).padStart(2, '0');
    return `${anio}-${mes}-${dia}`;
  }

  private formatearFechaLegible(valor: string): string {
    const [anio, mes, dia] = valor.split('-');
    return `${dia}/${mes}/${anio}`;
  }
}
