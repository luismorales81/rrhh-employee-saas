import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import {
  IonBackButton,
  IonButton,
  IonButtons,
  IonCheckbox,
  IonContent,
  IonHeader,
  IonText,
  IonTextarea,
  IonTitle,
  IonToolbar
} from '@ionic/angular/standalone';
import {
  ReciboResumen,
  ServicioRecibos,
  TEXTO_LEGAL_FIRMA_RECIBO,
  tieneIntegridadVerificable,
  TipoConfirmacionRecibo
} from '../../nucleo/servicios/servicio-recibos';
import { ServicioMensajes } from '../../nucleo/servicios/servicio-mensajes';
import { ServicioAnalitica } from '../../nucleo/servicios/servicio-analitica';
import { EstadoCargaComponent } from '../../compartido/componentes/estado-carga/estado-carga.component';
import { VisorDocumentoComponent } from '../../compartido/componentes/visor-documento/visor-documento.component';
import { PanelExitoComponent } from '../../compartido/componentes/panel-exito/panel-exito.component';
import { ChipEstadoComponent } from '../../compartido/componentes/chip-estado/chip-estado.component';
import { etiquetaEstadoRecibo } from '../../nucleo/utilidades/etiquetas-estado';

@Component({
  selector: 'app-pagina-detalle-recibo',
  standalone: true,
  imports: [
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButtons,
    IonBackButton,
    IonContent,
    IonText,
    IonButton,
    IonCheckbox,
    IonTextarea,
    EstadoCargaComponent,
    VisorDocumentoComponent,
    PanelExitoComponent,
    ChipEstadoComponent
  ],
  template: `
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button defaultHref="/tabs/recibos"></ion-back-button>
        </ion-buttons>
        <ion-title>Recibo</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding pagina-fondo">
      @if (cargando()) {
        <app-estado-carga mensaje="Cargando recibo..." />
      } @else if (exito()) {
        <app-panel-exito
          [titulo]="tituloExito()"
          [mensaje]="mensajeExito()"
          textoAccion="Volver a recibos"
          rutaAccion="/tabs/recibos"
        />
      } @else if (mensajeError() && !recibo()) {
        <ion-text color="danger"><p>{{ mensajeError() }}</p></ion-text>
      } @else if (recibo()) {
        <h2>{{ recibo()!.titulo }}</h2>
        <div class="meta">
          <span>Período: {{ recibo()!.periodo }}</span>
          <app-chip-estado
            [texto]="chipEstado().texto"
            [tono]="chipEstado().tono"
          />
          <span class="version">v{{ recibo()!.version }}</span>
        </div>

        @if (mensajeError()) {
          <ion-text color="danger"><p>{{ mensajeError() }}</p></ion-text>
        }

        @if (!integridadOk()) {
          <div class="aviso-integridad" role="alert">
            <p>Este recibo no se puede firmar todavía. Tu equipo tiene que volver a publicarlo con el PDF.</p>
          </div>
        } @else {
          <app-visor-documento
            [blobFuente]="blobPdf()"
            [cargandoExterno]="cargandoPdf()"
            titulo="Recibo PDF"
            etiquetaCarga="Cargando PDF del recibo"
            (errorCarga)="mensajeError.set($event)"
          />

          @if (versiones().length > 1) {
            <div class="historial-versiones">
              <h3>Historial de versiones</h3>
              <ul>
                @for (v of versiones(); track v.id) {
                  <li>
                    v{{ v.version }} — {{ etiquetaVersion(v.estado) }}
                    @if (!v.vigente) { (reemplazada) }
                  </li>
                }
              </ul>
            </div>
          }

          @if (puedeConfirmar(recibo()!.estado)) {
            <div class="bloque-firma">
              <ion-button
                expand="block"
                fill="clear"
                size="small"
                type="button"
                (click)="mostrarLegal.set(!mostrarLegal())"
              >
                {{ mostrarLegal() ? 'Ocultar texto legal' : 'Ver texto legal' }}
              </ion-button>

              @if (mostrarLegal()) {
                <p class="texto-legal">{{ textoLegal }}</p>
              }

              <ion-checkbox
                labelPlacement="end"
                [checked]="confirmacionLegal()"
                (ionChange)="confirmacionLegal.set($event.detail.checked)"
              >
                Confirmé haber leído el recibo
              </ion-checkbox>

              @if (!mostrarDisconformidad()) {
                <ion-button
                  expand="block"
                  [disabled]="procesando()"
                  type="button"
                  (click)="confirmar('CONFIRMADO')"
                >
                  Todo bien, confirmar
                </ion-button>
                <ion-button
                  expand="block"
                  fill="outline"
                  type="button"
                  (click)="mostrarDisconformidad.set(true)"
                >
                  No estoy de acuerdo
                </ion-button>
              } @else {
                <ion-textarea
                  label="Motivo"
                  labelPlacement="stacked"
                  placeholder="Contanos qué no coincide (mínimo 10 caracteres)"
                  [value]="comentarioDisconformidad()"
                  (ionInput)="comentarioDisconformidad.set($any($event.target).value)"
                  rows="4"
                ></ion-textarea>
                <ion-button
                  expand="block"
                  color="warning"
                  [disabled]="procesando()"
                  type="button"
                  (click)="confirmar('DISCONFORMIDAD')"
                >
                  Registrar observaciones
                </ion-button>
                <ion-button
                  expand="block"
                  fill="clear"
                  type="button"
                  (click)="mostrarDisconformidad.set(false)"
                >
                  Cancelar
                </ion-button>
              }
            </div>
          } @else if (puedeDescargarComprobante(recibo()!)) {
            <ion-button expand="block" type="button" (click)="descargarComprobante()">
              Descargar evidencia
            </ion-button>
          }
        }
      }
    </ion-content>
  `,
  styles: `
    .meta {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      gap: 0.5rem 0.75rem;
      color: var(--ion-color-medium);
      margin-bottom: 1rem;
      font-size: 0.9rem;
    }

    .version {
      opacity: 0.85;
    }

    .aviso-integridad {
      padding: 1rem;
      border-radius: 10px;
      background: var(--color-alerta-fondo, #fef3c7);
      color: var(--color-alerta-texto, #92400e);
      margin: 0.5rem 0 1rem;
    }

    .aviso-integridad p {
      margin: 0;
      line-height: 1.45;
    }

    .texto-legal {
      font-size: 0.85rem;
      color: #475569;
      line-height: 1.45;
      margin: 0;
      padding: 0.75rem;
      background: var(--color-superficie-secundaria, #f8fafc);
      border-radius: 8px;
    }

    .historial-versiones {
      margin: 1rem 0;
      font-size: 0.9rem;
    }

    .historial-versiones ul {
      padding-left: 1.2rem;
      margin: 0.5rem 0;
    }

    .bloque-firma {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
      margin-top: 1rem;
    }

    h2 {
      margin: 0 0 0.35rem;
      font-size: 1.15rem;
    }

    h3 {
      margin: 0 0 0.5rem;
      font-size: 0.95rem;
    }
  `
})
export class PaginaDetalleReciboComponent implements OnInit {
  private readonly ruta = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly servicioRecibos = inject(ServicioRecibos);
  private readonly servicioMensajes = inject(ServicioMensajes);
  private readonly servicioAnalitica = inject(ServicioAnalitica);
  private readonly destruirRef = inject(DestroyRef);

  readonly textoLegal = TEXTO_LEGAL_FIRMA_RECIBO;
  readonly recibo = signal<ReciboResumen | null>(null);
  readonly versiones = signal<ReciboResumen[]>([]);
  readonly cargando = signal(true);
  readonly mensajeError = signal<string | null>(null);
  readonly procesando = signal(false);
  readonly mostrarDisconformidad = signal(false);
  readonly comentarioDisconformidad = signal('');
  readonly confirmacionLegal = signal(false);
  readonly mostrarLegal = signal(false);
  readonly blobPdf = signal<Blob | null>(null);
  readonly cargandoPdf = signal(false);
  readonly exito = signal(false);
  readonly tituloExito = signal('Listo');
  readonly mensajeExito = signal('');
  readonly integridadOk = signal(true);

  chipEstado() {
    return etiquetaEstadoRecibo(this.recibo()?.estado);
  }

  etiquetaVersion(estado: string): string {
    return etiquetaEstadoRecibo(estado).texto;
  }

  ngOnInit(): void {
    this.ruta.paramMap.pipe(takeUntilDestroyed(this.destruirRef)).subscribe((params) => {
      const id = Number(params.get('id'));
      if (!id) {
        this.mensajeError.set('Recibo no válido');
        this.cargando.set(false);
        return;
      }
      this.reiniciarEstado();
      this.cargarRecibo(id);
    });
  }

  puedeConfirmar(estado: string): boolean {
    return estado === 'PENDIENTE' || estado === 'VISTO';
  }

  puedeDescargarComprobante(recibo: ReciboResumen): boolean {
    return recibo.estado === 'FIRMADO' || recibo.estado === 'DISCONFORMIDAD';
  }

  confirmar(tipo: TipoConfirmacionRecibo): void {
    const recibo = this.recibo();
    if (!recibo) return;

    if (tipo === 'CONFIRMADO' && !this.confirmacionLegal()) {
      void this.servicioMensajes.error('Confirmá que leíste el recibo para continuar.');
      return;
    }

    if (tipo === 'DISCONFORMIDAD' && this.comentarioDisconformidad().trim().length < 10) {
      void this.servicioMensajes.error('Indicá el motivo (mínimo 10 caracteres).');
      return;
    }

    this.procesando.set(true);
    this.servicioRecibos.confirmarRecepcion(recibo.id, {
      tipo,
      comentario: tipo === 'DISCONFORMIDAD' ? this.comentarioDisconformidad().trim() : null
    }).subscribe({
      next: (actualizado) => {
        this.procesando.set(false);
        this.recibo.set(actualizado);
        if (tipo === 'CONFIRMADO') {
          this.servicioAnalitica.reciboFirmado(actualizado.id);
        }
        this.tituloExito.set(
          tipo === 'CONFIRMADO' ? 'Recibo confirmado' : 'Observaciones registradas'
        );
        this.mensajeExito.set(
          tipo === 'CONFIRMADO'
            ? 'Tu confirmación quedó registrada. Podés volver a la lista cuando quieras.'
            : 'Registramos tu disconformidad. Tu equipo va a poder ver el motivo.'
        );
        this.exito.set(true);
        this.servicioRecibos.listarVersiones(actualizado.id).subscribe({
          next: (lista) => this.versiones.set(lista)
        });
      },
      error: (err) => {
        this.procesando.set(false);
        void this.servicioMensajes.error(err.error?.mensaje ?? 'No se pudo confirmar el recibo.');
      }
    });
  }

  descargarComprobante(): void {
    const recibo = this.recibo();
    if (!recibo) return;
    this.servicioRecibos.descargarComprobante(recibo.id).subscribe({
      next: (blob) => {
        const esPdf = blob.type.includes('pdf');
        const extension = esPdf ? 'pdf' : 'json';
        const url = URL.createObjectURL(blob);
        const enlace = document.createElement('a');
        enlace.href = url;
        enlace.download = `evidencia-${recibo.periodo}.${extension}`;
        enlace.click();
        URL.revokeObjectURL(url);
        if (!esPdf) {
          void this.servicioMensajes.info(
            'Descargaste la evidencia digital del recibo. Tu equipo puede pedirte este archivo.'
          );
        }
      },
      error: () => void this.servicioMensajes.error('No se pudo descargar la evidencia.')
    });
  }

  private cargarRecibo(id: number): void {
    this.servicioRecibos.listarPropios().subscribe({
      next: (lista) => {
        const recibo = lista.find((r) => r.id === id);
        if (!recibo) {
          const vigente = lista.find((r) => r.vigente);
          if (vigente) {
            void this.router.navigate(['/tabs/recibos', vigente.id], { replaceUrl: true });
            return;
          }
          void this.servicioMensajes.error('Este recibo ya no está disponible.');
          void this.router.navigate(['/tabs/recibos'], { replaceUrl: true });
          return;
        }
        this.procesarRecibo(recibo);
      },
      error: () => {
        this.mensajeError.set('No se pudo cargar el recibo');
        this.cargando.set(false);
      }
    });
  }

  private procesarRecibo(recibo: ReciboResumen): void {
    if (!tieneIntegridadVerificable(recibo)) {
      this.recibo.set(recibo);
      this.integridadOk.set(false);
      this.cargando.set(false);
      return;
    }

    this.integridadOk.set(true);

    if (this.puedeConfirmar(recibo.estado)) {
      this.servicioRecibos.visualizar(recibo.id).subscribe({
        next: (actualizado) => {
          this.recibo.set(actualizado);
          this.cargando.set(false);
          this.cargarPdf(recibo.id);
        },
        error: (err) => {
          this.mensajeError.set(err.error?.mensaje ?? 'No se pudo cargar el recibo');
          this.cargando.set(false);
        }
      });
    } else {
      this.recibo.set(recibo);
      this.cargando.set(false);
      this.cargarPdf(recibo.id);
      this.servicioRecibos.listarVersiones(recibo.id).subscribe({
        next: (lista) => this.versiones.set(lista)
      });
    }
  }

  private reiniciarEstado(): void {
    this.recibo.set(null);
    this.versiones.set([]);
    this.blobPdf.set(null);
    this.cargandoPdf.set(false);
    this.cargando.set(true);
    this.mensajeError.set(null);
    this.procesando.set(false);
    this.mostrarDisconformidad.set(false);
    this.comentarioDisconformidad.set('');
    this.confirmacionLegal.set(false);
    this.mostrarLegal.set(false);
    this.exito.set(false);
    this.integridadOk.set(true);
  }

  private cargarPdf(id: number): void {
    this.cargandoPdf.set(true);
    this.blobPdf.set(null);
    this.servicioRecibos.descargarPdf(id).subscribe({
      next: (blob) => {
        this.blobPdf.set(blob);
        this.cargandoPdf.set(false);
      },
      error: () => {
        this.cargandoPdf.set(false);
        this.mensajeError.set('No se pudo cargar el PDF');
      }
    });
  }
}
