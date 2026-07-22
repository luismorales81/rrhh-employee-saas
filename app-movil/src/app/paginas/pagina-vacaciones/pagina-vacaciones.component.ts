import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { switchMap, throwError } from 'rxjs';
import {
  IonContent,
  IonHeader,
  IonTitle,
  IonToolbar,
  IonCard,
  IonCardHeader,
  IonCardTitle,
  IonCardSubtitle,
  IonCardContent,
  IonInput,
  IonTextarea,
  IonButton,
  IonText,
  IonGrid,
  IonRow,
  IonCol,
  IonNote
} from '@ionic/angular/standalone';
import {
  ErrorValidacionVacaciones,
  RespuestaSimularFechaVacaciones,
  ServicioVacaciones,
  SaldoVacacionesResumen,
  SolicitudVacacionesResumen,
  EventoCalendarioVacaciones
} from '../../nucleo/servicios/servicio-vacaciones';
import { ServicioMensajes } from '../../nucleo/servicios/servicio-mensajes';
import { ServicioAnalitica } from '../../nucleo/servicios/servicio-analitica';
import { EstadoCargaComponent } from '../../compartido/componentes/estado-carga/estado-carga.component';
import { EstadoVacioComponent } from '../../compartido/componentes/estado-vacio/estado-vacio.component';
import { EncabezadoPaginaComponent } from '../../compartido/componentes/encabezado-pagina/encabezado-pagina.component';
import { TarjetaFilaComponent } from '../../compartido/componentes/tarjeta-fila/tarjeta-fila.component';
import {
  calcularDiasSolicitados,
  extraerErroresVacaciones,
  validarPeriodoVacaciones
} from '../../nucleo/utilidades/validacion-vacaciones';
import { formatearEstadoSolicitud } from '../../nucleo/utilidades/formatear-estado-solicitud';
import {
  humanizarListaMensajesVacaciones,
  humanizarMensajeVacaciones
} from '../../nucleo/utilidades/humanizar-vacaciones';

interface DiaCalendarioMovil {
  fecha: Date | null;
  eventos: EventoCalendarioVacaciones[];
}

@Component({
  selector: 'app-pagina-vacaciones',
  standalone: true,
  imports: [
    FormsModule,
    IonContent,
    IonHeader,
    IonTitle,
    IonToolbar,
    IonCard,
    IonCardHeader,
    IonCardTitle,
    IonCardSubtitle,
    IonCardContent,
    IonInput,
    IonTextarea,
    IonButton,
    IonText,
    IonGrid,
    IonRow,
    IonCol,
    IonNote,
    EstadoCargaComponent,
    EstadoVacioComponent,
    EncabezadoPaginaComponent,
    TarjetaFilaComponent
  ],
  template: `
    <ion-header>
      <ion-toolbar>
        <ion-title>Vacaciones</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding pagina-fondo">
      @if (pasoPedido() === 0) {
        <app-encabezado-pagina
          titulo="Vacaciones"
          subtitulo="Consultá tu saldo y pedí tus días"
        />
      } @else if (pasoPedido() === 1) {
        <app-encabezado-pagina
          titulo="Pedir vacaciones"
          subtitulo="Elegí las fechas del pedido"
        />
      } @else {
        <app-encabezado-pagina
          titulo="Revisá tu pedido"
          subtitulo="Confirmá antes de enviar"
        />
      }

      @if (cargando()) {
        <app-estado-carga />
      } @else if (mensajeError()) {
        <ion-text color="danger"><p>{{ mensajeError() }}</p></ion-text>
      } @else if (pasoPedido() === 0) {
        @if (saldo()) {
          <div class="hero-saldo">
            <p class="hero-etiqueta">{{ saldo()!.politicaNombre }}</p>
            <p class="hero-titulo">Te quedan {{ saldo()!.diasDisponibles }} días</p>
            <p class="hero-meta">
              Pendientes: {{ saldo()!.diasPendientes }} · Usados: {{ saldo()!.diasUsados }}
            </p>
          </div>
        }

        <ion-button expand="block" type="button" (click)="iniciarPedido()">
          Pedir vacaciones
        </ion-button>

        <h3 class="grupo-lista-titulo">Mis solicitudes</h3>
        @if (solicitudes().length === 0) {
          <app-estado-vacio
            icono="calendar-outline"
            titulo="Todavía no pediste vacaciones"
            descripcion="Cuando envíes un pedido, lo vas a ver acá."
          />
        } @else {
          <div class="lista-tarjetas">
            @for (solicitud of solicitudes(); track solicitud.solicitudVacacionesId) {
              <app-tarjeta-fila
                [titulo]="tituloPeriodo(solicitud.fechaInicio, solicitud.fechaFin)"
                [subtitulo]="solicitud.diasSolicitados + ' días'"
                [etiqueta]="etiquetaEstado(solicitud.estado)"
                [alerta]="solicitud.estado !== 'APROBADO'"
              />
            }
          </div>
        }

        <div class="cabecera-calendario">
          <ion-button fill="clear" size="small" type="button" (click)="mesAnterior()">‹</ion-button>
          <h3 class="titulo-mes">{{ tituloMes() }}</h3>
          <ion-button fill="clear" size="small" type="button" (click)="mesSiguiente()">›</ion-button>
        </div>

        <ion-card class="tarjeta-calendario">
          <ion-card-content>
            <ion-grid class="calendario-movil">
              <ion-row>
                @for (nombre of nombresDias; track nombre) {
                  <ion-col class="dia-cabecera">{{ nombre }}</ion-col>
                }
              </ion-row>
              @for (fila of filasCalendario(); track $index) {
                <ion-row>
                  @for (dia of fila; track $index) {
                    <ion-col class="dia-celda" [class.dia-vacio]="!dia.fecha">
                      @if (dia.fecha) {
                        <span class="dia-numero">{{ dia.fecha.getDate() }}</span>
                        @for (evento of dia.eventos; track evento.solicitudId) {
                          <span class="punto-evento" [class]="claseEstado(evento.estado)"></span>
                        }
                      }
                    </ion-col>
                  }
                </ion-row>
              }
            </ion-grid>
          </ion-card-content>
        </ion-card>

        <h3 class="grupo-lista-titulo">Este mes</h3>
        @if (eventosCalendario().length === 0) {
          <app-estado-vacio
            icono="calendar-outline"
            titulo="Sin vacaciones este mes"
            descripcion="No hay pedidos en el período visible."
          />
        } @else {
          <div class="lista-tarjetas">
            @for (evento of eventosCalendario(); track evento.solicitudId) {
              <app-tarjeta-fila
                [titulo]="tituloPeriodo(evento.fechaInicio, evento.fechaFin)"
                [subtitulo]="evento.diasSolicitados + ' días'"
                [etiqueta]="etiquetaEstado(evento.estado)"
                [alerta]="evento.estado !== 'APROBADO'"
              />
            }
          </div>
        }
      } @else if (pasoPedido() === 1) {
        <ion-card class="tarjeta-formulario">
          <ion-card-content>
            <ion-input
              label="Fecha inicio"
              labelPlacement="stacked"
              type="date"
              [(ngModel)]="fechaInicio"
              (ionInput)="alCambiarFechas()"
            ></ion-input>
            <ion-input
              label="Fecha fin"
              labelPlacement="stacked"
              type="date"
              [(ngModel)]="fechaFin"
              (ionInput)="alCambiarFechas()"
            ></ion-input>
            @if (diasSolicitados() > 0) {
              <p class="resumen-dias" [class.resumen-dias--alerta]="excedeSaldo()">
                {{ diasSolicitados() }} día(s) solicitado(s)
              </p>
            }
            <ion-textarea
              label="Motivo (opcional)"
              labelPlacement="stacked"
              [(ngModel)]="motivo"
              autoGrow="true"
            ></ion-textarea>
            @for (error of erroresValidacion(); track error) {
              <ion-text color="danger"><p class="mensaje-validacion">{{ error }}</p></ion-text>
            }
            <ion-button
              expand="block"
              type="button"
              (click)="continuarARevision()"
              [disabled]="!puedeContinuar()"
            >
              Continuar
            </ion-button>
            <ion-button expand="block" fill="clear" type="button" (click)="cancelarPedido()">
              Cancelar
            </ion-button>
          </ion-card-content>
        </ion-card>
      } @else {
        <ion-card class="tarjeta-formulario">
          <ion-card-header>
            <ion-card-subtitle>Resumen del pedido</ion-card-subtitle>
            <ion-card-title>{{ tituloPeriodo(fechaInicio, fechaFin) }}</ion-card-title>
          </ion-card-header>
          <ion-card-content>
            <p class="resumen-dias" [class.resumen-dias--alerta]="excedeSaldo()">
              {{ diasSolicitados() }} día(s) · {{ motivo || 'Sin motivo' }}
            </p>

            @if (simulacionFecha() && !simulacionFecha()!.esValida) {
              <ion-note color="warning">
                {{ humanizarMotivo(simulacionFecha()!.motivo) }}
              </ion-note>
              @if (simulacionFecha()!.fechaSugerida) {
                <ion-button
                  expand="block"
                  fill="outline"
                  size="small"
                  type="button"
                  (click)="aplicarFechaSugerida()"
                >
                  Usar fecha sugerida: {{ formatearFechaLista(simulacionFecha()!.fechaSugerida!) }}
                </ion-button>
              }
            }

            @for (error of erroresValidacion(); track error) {
              <ion-text color="danger"><p class="mensaje-validacion">{{ error }}</p></ion-text>
            }
            @for (aviso of advertencias(); track aviso) {
              <ion-text color="warning"><p class="mensaje-validacion">{{ aviso }}</p></ion-text>
            }

            <ion-button
              expand="block"
              type="button"
              (click)="solicitar()"
              [disabled]="botonEnvioDeshabilitado()"
            >
              {{ guardando() ? 'Enviando...' : 'Enviar' }}
            </ion-button>
            <ion-button expand="block" fill="clear" type="button" (click)="volverAFechas()">
              Volver
            </ion-button>
          </ion-card-content>
        </ion-card>
      }
    </ion-content>
  `,
  styles: `
    .hero-saldo {
      background: var(--color-superficie);
      border: 1px solid var(--color-borde-suave);
      border-radius: var(--radio-xl, 16px);
      padding: var(--espacio-6, 1.5rem) var(--espacio-5, 1.25rem);
      text-align: center;
      margin-bottom: var(--espacio-4, 1rem);
      box-shadow: var(--sombra-sm, none);
    }
    .hero-etiqueta {
      margin: 0;
      font-size: 0.8rem;
      color: var(--color-texto-secundario, var(--ion-color-medium));
    }
    .hero-titulo {
      margin: 0.35rem 0 0;
      font-size: 1.6rem;
      font-weight: 700;
      color: var(--color-texto, inherit);
    }
    .hero-meta {
      margin: 0.4rem 0 0;
      font-size: 0.85rem;
      color: var(--color-texto-secundario, var(--ion-color-medium));
    }
    .tarjeta-formulario, .tarjeta-calendario {
      margin: 0 0 1rem;
      border-radius: 14px;
    }
    ion-input, ion-textarea { margin-bottom: 0.75rem; }
    .resumen-dias {
      margin: 0 0 0.75rem;
      font-size: 0.85rem;
      font-weight: 600;
      color: var(--ion-color-primary);
    }
    .resumen-dias--alerta {
      color: var(--ion-color-danger);
    }
    .mensaje-validacion {
      margin: 0.25rem 0;
      font-size: 0.85rem;
    }
    .cabecera-calendario {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin: 1.25rem 0 0.5rem;
    }
    .titulo-mes { margin: 0; font-size: 1rem; font-weight: 600; }
    .calendario-movil { padding: 0; }
    .dia-cabecera {
      text-align: center;
      font-size: 0.7rem;
      font-weight: 600;
      color: var(--ion-color-medium);
      padding: 0.25rem 0;
    }
    .dia-celda {
      min-height: 2.5rem;
      border: 1px solid var(--ion-color-light);
      text-align: center;
      padding: 0.15rem;
      position: relative;
    }
    .dia-vacio { background: var(--ion-color-light); opacity: 0.4; }
    .dia-numero { font-size: 0.75rem; display: block; }
    .punto-evento {
      display: inline-block;
      width: 6px;
      height: 6px;
      border-radius: 50%;
      margin: 1px;
    }
    .estado-aprobado { background: var(--ion-color-success); }
    .estado-pendiente { background: var(--ion-color-warning); }
    .estado-rechazado { background: var(--ion-color-medium); }
  `
})
export class PaginaVacacionesComponent implements OnInit {
  private readonly servicioVacaciones = inject(ServicioVacaciones);
  private readonly servicioMensajes = inject(ServicioMensajes);
  private readonly servicioAnalitica = inject(ServicioAnalitica);

  private readonly diasMinimos = 7;

  readonly saldo = signal<SaldoVacacionesResumen | null>(null);
  readonly solicitudes = signal<SolicitudVacacionesResumen[]>([]);
  readonly eventosCalendario = signal<EventoCalendarioVacaciones[]>([]);
  readonly mesActual = signal(new Date());
  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly mensajeError = signal<string | null>(null);
  readonly erroresValidacion = signal<string[]>([]);
  readonly advertencias = signal<string[]>([]);
  readonly simulacionFecha = signal<RespuestaSimularFechaVacaciones | null>(null);
  readonly simulandoFecha = signal(false);
  readonly pasoPedido = signal(0);

  private anioSaldoCargado: number | null = null;

  readonly nombresDias = ['L', 'M', 'X', 'J', 'V', 'S', 'D'];
  readonly nombresMeses = [
    'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
    'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
  ];

  readonly filasCalendario = computed(() => {
    const dias = this.construirDiasMes();
    const filas: DiaCalendarioMovil[][] = [];
    for (let i = 0; i < dias.length; i += 7) {
      filas.push(dias.slice(i, i + 7));
    }
    return filas;
  });

  fechaInicio = '';
  fechaFin = '';
  motivo = '';

  ngOnInit(): void {
    this.cargarDatos();
  }

  tituloMes(): string {
    const fecha = this.mesActual();
    return `${this.nombresMeses[fecha.getMonth()]} ${fecha.getFullYear()}`;
  }

  mesAnterior(): void {
    const actual = this.mesActual();
    this.mesActual.set(new Date(actual.getFullYear(), actual.getMonth() - 1, 1));
    this.cargarCalendarioMes();
  }

  mesSiguiente(): void {
    const actual = this.mesActual();
    this.mesActual.set(new Date(actual.getFullYear(), actual.getMonth() + 1, 1));
    this.cargarCalendarioMes();
  }

  claseEstado(estado: string): string {
    if (estado === 'APROBADO') return 'estado-aprobado';
    if (estado === 'RECHAZADO' || estado === 'CANCELADO') return 'estado-rechazado';
    return 'estado-pendiente';
  }

  etiquetaEstado(estado: string): string {
    return formatearEstadoSolicitud(estado);
  }

  formatearFechaLista(valor: string): string {
    const [anio, mes, dia] = valor.split('-');
    if (!anio || !mes || !dia) {
      return valor;
    }
    return `${dia}/${mes}/${anio}`;
  }

  tituloPeriodo(inicio: string, fin: string): string {
    return `${this.formatearFechaLista(inicio)} → ${this.formatearFechaLista(fin)}`;
  }

  humanizarMotivo(motivo: string | null | undefined): string {
    return humanizarMensajeVacaciones(motivo);
  }

  iniciarPedido(): void {
    this.erroresValidacion.set([]);
    this.advertencias.set([]);
    this.simulacionFecha.set(null);
    this.pasoPedido.set(1);
  }

  continuarARevision(): void {
    const errores = humanizarListaMensajesVacaciones(this.validarFechasVacaciones());
    if (errores.length) {
      this.erroresValidacion.set(errores);
      void this.servicioMensajes.error(errores[0]);
      return;
    }
    this.erroresValidacion.set([]);
    this.pasoPedido.set(2);
  }

  volverAFechas(): void {
    this.pasoPedido.set(1);
  }

  cancelarPedido(): void {
    this.fechaInicio = '';
    this.fechaFin = '';
    this.motivo = '';
    this.erroresValidacion.set([]);
    this.advertencias.set([]);
    this.simulacionFecha.set(null);
    this.pasoPedido.set(0);
  }

  puedeContinuar(): boolean {
    return !!this.fechaInicio
      && !!this.fechaFin
      && !this.simulandoFecha()
      && this.erroresValidacion().length === 0;
  }

  solicitar(): void {
    this.erroresValidacion.set([]);
    this.advertencias.set([]);
    this.mensajeError.set(null);

    const errores = humanizarListaMensajesVacaciones(this.validarFechasVacaciones());
    if (errores.length) {
      this.erroresValidacion.set(errores);
      void this.servicioMensajes.error(errores[0]);
      return;
    }

    if (!this.saldo()) {
      const mensaje = 'No se pudo verificar tu saldo de vacaciones.';
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
            const motivo = humanizarMensajeVacaciones(
              simulacion.motivo
                ?? 'La fecha de inicio no es válida según tu patrón de descanso (Art. 151 LCT).'
            );
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
          this.fechaInicio = '';
          this.fechaFin = '';
          this.motivo = '';
          this.simulacionFecha.set(null);
          this.pasoPedido.set(0);
          this.servicioAnalitica.vacacionesEnviadas();
          if (respuesta.advertencias?.length) {
            this.advertencias.set(humanizarListaMensajesVacaciones(respuesta.advertencias));
            void this.servicioMensajes.exito('Solicitud enviada con advertencias.');
          } else {
            void this.servicioMensajes.exito('Solicitud de vacaciones enviada.');
          }
          this.cargarDatos();
        },
        error: (err) => {
          this.guardando.set(false);
          const erroresRespuesta = humanizarListaMensajesVacaciones(extraerErroresVacaciones(err));
          this.erroresValidacion.set(erroresRespuesta);
          const advertencias = (err as { error?: ErrorValidacionVacaciones })?.error?.advertencias;
          if (advertencias?.length) {
            this.advertencias.set(humanizarListaMensajesVacaciones(advertencias));
          }
          void this.servicioMensajes.error(erroresRespuesta[0]);
        }
      });
  }

  diasSolicitados(): number {
    return calcularDiasSolicitados(this.fechaInicio, this.fechaFin);
  }

  excedeSaldo(): boolean {
    const saldoActual = this.saldo();
    if (!saldoActual) {
      return false;
    }
    const dias = this.diasSolicitados();
    return dias > 0 && dias > Number(saldoActual.diasDisponibles);
  }

  botonEnvioDeshabilitado(): boolean {
    return this.guardando()
      || this.simulandoFecha()
      || !this.saldo()
      || this.erroresValidacion().length > 0;
  }

  alCambiarFechas(): void {
    this.cargarSaldoParaAnio(this.anioDesdeFechaInicio());
    this.simularFechaInicio();
  }

  aplicarFechaSugerida(): void {
    const simulacion = this.simulacionFecha();
    if (!simulacion?.fechaSugerida) {
      return;
    }
    const dias = this.diasSolicitados();
    this.fechaInicio = simulacion.fechaSugerida;
    if (dias > 0) {
      const fin = new Date(`${simulacion.fechaSugerida}T00:00:00`);
      fin.setDate(fin.getDate() + dias - 1);
      this.fechaFin = this.formatearFecha(fin);
    }
    this.simularFechaInicio();
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

  private validarFechasVacaciones(): string[] {
    const diasDisponibles = this.saldo()?.diasDisponibles;
    const errores = validarPeriodoVacaciones(
      this.fechaInicio,
      this.fechaFin,
      this.diasMinimos,
      diasDisponibles != null ? Number(diasDisponibles) : null,
      this.solicitudes()
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
    this.erroresValidacion.set(humanizarListaMensajesVacaciones(this.validarFechasVacaciones()));
  }

  private cargarSaldoParaAnio(anio: number): void {
    if (this.anioSaldoCargado === anio && this.saldo() !== null) {
      this.actualizarErroresVacaciones();
      return;
    }

    this.servicioVacaciones.saldoPropio(anio).subscribe({
      next: (saldo) => {
        this.saldo.set(saldo);
        this.anioSaldoCargado = anio;
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

  private cargarDatos(): void {
    this.cargando.set(true);
    this.mensajeError.set(null);

    this.servicioVacaciones.saldoPropio().subscribe({
      next: (saldo) => {
        this.saldo.set(saldo);
        this.anioSaldoCargado = saldo.anio ?? new Date().getFullYear();
        this.cargando.set(false);
      },
      error: () => {
        this.mensajeError.set('No se pudo cargar el saldo de vacaciones.');
        this.cargando.set(false);
      }
    });

    this.servicioVacaciones.listarPropias().subscribe({
      next: (lista) => {
        this.solicitudes.set(lista);
        this.actualizarErroresVacaciones();
      },
      error: () => {}
    });

    this.cargarCalendarioMes();
  }

  private cargarCalendarioMes(): void {
    const fecha = this.mesActual();
    const desde = this.formatearFecha(new Date(fecha.getFullYear(), fecha.getMonth(), 1));
    const hasta = this.formatearFecha(new Date(fecha.getFullYear(), fecha.getMonth() + 1, 0));
    this.servicioVacaciones.obtenerCalendario(desde, hasta).subscribe({
      next: (eventos) => this.eventosCalendario.set(eventos),
      error: () => {}
    });
  }

  private construirDiasMes(): DiaCalendarioMovil[] {
    const fecha = this.mesActual();
    const anio = fecha.getFullYear();
    const mes = fecha.getMonth();
    const primerDia = new Date(anio, mes, 1);
    const ultimoDia = new Date(anio, mes + 1, 0).getDate();
    const offset = (primerDia.getDay() + 6) % 7;
    const eventos = this.eventosCalendario();

    const dias: DiaCalendarioMovil[] = [];
    for (let i = 0; i < offset; i++) {
      dias.push({ fecha: null, eventos: [] });
    }
    for (let dia = 1; dia <= ultimoDia; dia++) {
      const fechaDia = new Date(anio, mes, dia);
      const eventosDia = eventos.filter((e) => this.eventoEnDia(e, fechaDia));
      dias.push({ fecha: fechaDia, eventos: eventosDia });
    }
    while (dias.length % 7 !== 0) {
      dias.push({ fecha: null, eventos: [] });
    }
    return dias;
  }

  private eventoEnDia(evento: EventoCalendarioVacaciones, fecha: Date): boolean {
    const inicio = new Date(evento.fechaInicio + 'T00:00:00');
    const fin = new Date(evento.fechaFin + 'T00:00:00');
    const actual = new Date(fecha.getFullYear(), fecha.getMonth(), fecha.getDate());
    return actual >= inicio && actual <= fin;
  }

  private formatearFecha(fecha: Date): string {
    const anio = fecha.getFullYear();
    const mes = String(fecha.getMonth() + 1).padStart(2, '0');
    const dia = String(fecha.getDate()).padStart(2, '0');
    return `${anio}-${mes}-${dia}`;
  }
}
