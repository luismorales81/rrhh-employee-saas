import { Component, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {
  AlertController,
  IonButton,
  IonCheckbox,
  IonContent,
  IonHeader,
  IonItem,
  IonModal,
  IonTitle,
  IonToolbar
} from '@ionic/angular/standalone';
import {
  EstadoFichajeDia,
  MarcaFichajeResumen,
  ServicioFichajes,
  TipoMarca
} from '../../nucleo/servicios/servicio-fichajes';
import { ServicioGeolocalizacion } from '../../nucleo/servicios/servicio-geolocalizacion';
import { ServicioMensajes } from '../../nucleo/servicios/servicio-mensajes';
import { ServicioAnalitica } from '../../nucleo/servicios/servicio-analitica';
import { EncabezadoPaginaComponent } from '../../compartido/componentes/encabezado-pagina/encabezado-pagina.component';
import { EstadoCargaComponent } from '../../compartido/componentes/estado-carga/estado-carga.component';

@Component({
  selector: 'app-pagina-fichaje',
  standalone: true,
  imports: [
    FormsModule,
    RouterLink,
    IonContent,
    IonHeader,
    IonTitle,
    IonToolbar,
    IonButton,
    IonCheckbox,
    IonItem,
    IonModal,
    EncabezadoPaginaComponent,
    EstadoCargaComponent
  ],
  template: `
    <ion-header>
      <ion-toolbar>
        <ion-title>Fichar</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding pagina-fondo">
      <app-encabezado-pagina
        titulo="Mi jornada"
        subtitulo="Usamos tu ubicación solo al marcar. Sin seguimiento continuo."
      />

      @if (cargando()) {
        <app-estado-carga modo="spinner" mensaje="Preparando fichaje..." />
      } @else if (!estado()?.fichajeHabilitado) {
        <div class="card-aviso">
          <strong>Fichaje no disponible</strong>
          <p>Pedile a tu equipo que active el fichaje móvil para vos.</p>
        </div>
      } @else {
        <div class="hero-jornada" [class.en-jornada]="siguienteTipo() === 'SALIDA'">
          <p class="reloj">{{ horaActual() }}</p>
          <p class="estado-texto">{{ textoEstadoJornada() }}</p>
          @if (estado()?.ultimaMarcaHora) {
            <p class="meta">
              Última marca: {{ etiquetaTipo(estado()!.ultimaMarcaTipo!) }} a las {{ estado()!.ultimaMarcaHora }}
            </p>
          }
        </div>

        <ion-button
          expand="block"
          class="boton-fichar"
          [color]="siguienteTipo() === 'ENTRADA' ? 'success' : 'warning'"
          [disabled]="fichando() || mostrarConsentimiento()"
          (click)="alTocarFichar()"
        >
          {{ textoBotonFichar() }}
        </ion-button>

        @if (mensajeUbicacion()) {
          <p class="hint-ubicacion">{{ mensajeUbicacion() }}</p>
        }

        @if (ultimoResultado()) {
          <div
            class="resultado"
            [class.ok]="ultimoResultado()!.estado === 'VALIDADA'"
            [class.revision]="ultimoResultado()!.estado !== 'VALIDADA'"
          >
            {{ mensajeResultado() }}
          </div>
        }

        <div class="acciones-secundarias">
          <ion-button fill="clear" expand="block" type="button" (click)="mostrarInfo.set(true)">
            ¿Qué datos se registran?
          </ion-button>
          <ion-button fill="outline" expand="block" routerLink="/tabs/fichaje/historial">
            Ver mi historial
          </ion-button>
        </div>
      }
    </ion-content>

    <ion-modal [isOpen]="mostrarConsentimiento()" (didDismiss)="cerrarConsentimiento()">
      <ng-template>
        <ion-header>
          <ion-toolbar>
            <ion-title>Antes de fichar</ion-title>
          </ion-toolbar>
        </ion-header>
        <ion-content class="ion-padding">
          <p class="intro-politica">
            Tu empresa registra la marca con fecha, hora y ubicación puntual. Leé el resumen y aceptá para continuar.
          </p>
          <div class="texto-politica">{{ textoPolitica() }}</div>
          <ion-item lines="none">
            <ion-checkbox [(ngModel)]="aceptoPolitica" labelPlacement="end">
              Leí y acepto
            </ion-checkbox>
          </ion-item>
          <ion-button expand="block" [disabled]="!aceptoPolitica" (click)="aceptarPolitica()">
            Entendido, continuar
          </ion-button>
        </ion-content>
      </ng-template>
    </ion-modal>

    <ion-modal [isOpen]="mostrarInfo()" (didDismiss)="mostrarInfo.set(false)">
      <ng-template>
        <ion-header>
          <ion-toolbar>
            <ion-title>Datos que registramos</ion-title>
          </ion-toolbar>
        </ion-header>
        <ion-content class="ion-padding">
          <div class="texto-politica">{{ textoPolitica() }}</div>
          <ion-button expand="block" (click)="mostrarInfo.set(false)">Cerrar</ion-button>
        </ion-content>
      </ng-template>
    </ion-modal>
  `,
  styles: `
    .hero-jornada {
      background: var(--color-superficie);
      border: 1px solid var(--color-borde-suave);
      border-radius: var(--radio-xl);
      padding: var(--espacio-6) var(--espacio-5);
      text-align: center;
      margin-bottom: var(--espacio-5);
      box-shadow: var(--sombra-sm);
    }

    .hero-jornada.en-jornada {
      border-color: var(--color-alerta-borde);
      background: linear-gradient(to bottom, var(--color-alerta-fondo), var(--color-superficie) 60%);
    }

    .reloj {
      margin: 0;
      font-size: 2.5rem;
      font-weight: 700;
      letter-spacing: -0.03em;
      color: var(--color-texto);
      font-variant-numeric: tabular-nums;
    }

    .estado-texto {
      margin: 0.5rem 0 0;
      font-size: 1.05rem;
      font-weight: 600;
      color: var(--color-texto);
    }

    .meta {
      margin: 0.35rem 0 0;
      font-size: 0.875rem;
      color: var(--color-texto-secundario);
    }

    .boton-fichar {
      min-height: 56px;
      font-size: 1.05rem;
      font-weight: 700;
      --border-radius: 14px;
      margin: 0;
      transition: transform 120ms ease;
    }

    .boton-fichar:active {
      transform: scale(0.98);
    }

    .hint-ubicacion {
      margin: var(--espacio-3) 0 0;
      text-align: center;
      font-size: 0.875rem;
      color: var(--color-texto-secundario);
    }

    .resultado {
      margin-top: var(--espacio-4);
      padding: var(--espacio-4);
      border-radius: var(--radio-lg);
      font-weight: 600;
      font-size: 0.9375rem;
    }

    .resultado.ok {
      background: var(--color-exito-fondo);
      color: var(--color-exito-texto);
    }

    .resultado.revision {
      background: var(--color-alerta-fondo);
      color: var(--color-alerta-texto);
    }

    .acciones-secundarias {
      margin-top: var(--espacio-5);
      display: flex;
      flex-direction: column;
      gap: var(--espacio-2);
    }

    .card-aviso {
      background: var(--color-superficie);
      border-radius: var(--radio-xl);
      padding: var(--espacio-5);
      border: 1px solid var(--color-borde-suave);
    }

    .card-aviso p {
      margin: 0.35rem 0 0;
      color: var(--color-texto-secundario);
    }

    .intro-politica {
      color: var(--color-texto-secundario);
      line-height: 1.45;
    }

    .texto-politica {
      white-space: pre-wrap;
      font-family: inherit;
      font-size: 0.9rem;
      line-height: 1.5;
      max-height: 40vh;
      overflow: auto;
      padding: var(--espacio-3);
      background: var(--color-superficie-secundaria);
      border-radius: var(--radio-md);
      margin-bottom: var(--espacio-4);
    }
  `
})
export class PaginaFichajeComponent implements OnInit, OnDestroy {
  private readonly servicioFichajes = inject(ServicioFichajes);
  private readonly servicioGeo = inject(ServicioGeolocalizacion);
  private readonly servicioMensajes = inject(ServicioMensajes);
  private readonly servicioAnalitica = inject(ServicioAnalitica);
  private readonly alertController = inject(AlertController);

  readonly cargando = signal(true);
  readonly fichando = signal(false);
  readonly estado = signal<EstadoFichajeDia | null>(null);
  readonly textoPolitica = signal('');
  readonly mostrarConsentimiento = signal(false);
  readonly mostrarInfo = signal(false);
  readonly ultimoResultado = signal<MarcaFichajeResumen | null>(null);
  readonly mensajeUbicacion = signal<string | null>(null);
  readonly horaActual = signal(this.formatearHora(new Date()));
  aceptoPolitica = false;
  private intervaloReloj: ReturnType<typeof setInterval> | null = null;

  ngOnInit(): void {
    this.servicioFichajes.obtenerPolitica().subscribe({
      next: (p) => this.textoPolitica.set(p.textoTransparencia)
    });
    this.recargarEstado();
    this.intervaloReloj = setInterval(() => {
      this.horaActual.set(this.formatearHora(new Date()));
    }, 1000);
  }

  ngOnDestroy(): void {
    if (this.intervaloReloj) {
      clearInterval(this.intervaloReloj);
    }
  }

  siguienteTipo(): TipoMarca {
    return this.estado()?.siguienteTipo ?? 'ENTRADA';
  }

  etiquetaTipo(tipo: TipoMarca): string {
    return tipo === 'ENTRADA' ? 'Entrada' : 'Salida';
  }

  textoEstadoJornada(): string {
    if (this.siguienteTipo() === 'SALIDA') {
      return 'Estás en jornada';
    }
    return 'Todavía no marcaste entrada';
  }

  textoBotonFichar(): string {
    if (this.fichando()) {
      return 'Buscando tu ubicación…';
    }
    return this.siguienteTipo() === 'ENTRADA' ? 'Fichar entrada' : 'Fichar salida';
  }

  mensajeResultado(): string {
    const r = this.ultimoResultado();
    if (!r) {
      return '';
    }
    if (r.estado === 'VALIDADA') {
      return `${this.etiquetaTipo(r.tipo)} registrada${r.zonaFichajeNombre ? ' en ' + r.zonaFichajeNombre : ''}.`;
    }
    return `${this.etiquetaTipo(r.tipo)} enviada a revisión de tu equipo.`;
  }

  cerrarConsentimiento(): void {
    this.mostrarConsentimiento.set(false);
  }

  aceptarPolitica(): void {
    this.servicioFichajes.registrarConsentimiento(this.servicioGeo.obtenerIdDispositivo()).subscribe({
      next: () => {
        this.mostrarConsentimiento.set(false);
        this.recargarEstado();
        void this.servicioMensajes.exito('Listo, ya podés fichar.');
      },
      error: (err) => void this.servicioMensajes.error(err.error?.mensaje ?? 'No pudimos guardar tu aceptación')
    });
  }

  async alTocarFichar(): Promise<void> {
    if (this.estado()?.requiereConsentimiento) {
      this.mostrarConsentimiento.set(true);
      return;
    }

    if (this.siguienteTipo() === 'SALIDA') {
      const alerta = await this.alertController.create({
        header: '¿Fichar salida?',
        message: 'Vas a cerrar tu jornada de hoy.',
        buttons: [
          { text: 'Cancelar', role: 'cancel' },
          {
            text: 'Sí, salir',
            handler: () => {
              void this.fichar();
            }
          }
        ]
      });
      await alerta.present();
      return;
    }

    await this.fichar();
  }

  async fichar(): Promise<void> {
    this.fichando.set(true);
    this.mensajeUbicacion.set('Estamos buscando tu ubicación…');
    try {
      const ubicacion = await this.servicioGeo.obtenerUbicacionActual();
      this.mensajeUbicacion.set(
        ubicacion.precisionMetros > 80
          ? 'La señal es un poco imprecisa; igual vamos a intentar registrar.'
          : null
      );
      const tipo = this.siguienteTipo();
      this.servicioFichajes.marcar({
        tipo,
        latitud: ubicacion.latitud,
        longitud: ubicacion.longitud,
        precisionMetros: ubicacion.precisionMetros,
        fechaHoraDispositivo: new Date().toISOString(),
        idDispositivo: this.servicioGeo.obtenerIdDispositivo(),
        modeloDispositivo: this.servicioGeo.obtenerModeloDispositivo(),
        sistemaOperativo: this.servicioGeo.obtenerSistemaOperativo(),
        versionApp: '0.1.0'
      }).subscribe({
        next: (marca) => {
          this.ultimoResultado.set(marca);
          this.fichando.set(false);
          this.mensajeUbicacion.set(null);
          this.recargarEstado();
          this.servicioAnalitica.fichajeOk(marca.tipo);
          void this.servicioMensajes.exito(
            marca.estado === 'VALIDADA' ? 'Fichaje registrado.' : 'Fichaje enviado a revisión.'
          );
        },
        error: (err) => {
          this.fichando.set(false);
          this.mensajeUbicacion.set(null);
          void this.servicioMensajes.error(err.error?.mensaje ?? 'No se pudo registrar el fichaje');
        }
      });
    } catch (e) {
      this.fichando.set(false);
      const mensaje = e instanceof Error ? e.message : 'No pudimos obtener la ubicación';
      this.mensajeUbicacion.set(mensaje);
      void this.servicioMensajes.error(mensaje);
    }
  }

  private formatearHora(fecha: Date): string {
    return fecha.toLocaleTimeString('es-AR', { hour: '2-digit', minute: '2-digit' });
  }

  private recargarEstado(): void {
    this.cargando.set(true);
    this.servicioFichajes.obtenerEstadoDia().subscribe({
      next: (e) => {
        this.estado.set(e);
        if (e.requiereConsentimiento) {
          this.mostrarConsentimiento.set(true);
        }
        this.cargando.set(false);
      },
      error: () => {
        this.cargando.set(false);
        void this.servicioMensajes.error('No se pudo cargar el estado de fichaje');
      }
    });
  }
}
