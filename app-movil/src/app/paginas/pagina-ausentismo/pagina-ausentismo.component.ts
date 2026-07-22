import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  ActionSheetController,
  IonContent,
  IonHeader,
  IonTitle,
  IonToolbar,
  IonButton,
  IonText,
  IonInput,
  IonTextarea,
  IonSelect,
  IonSelectOption,
  IonToggle,
  IonItem,
  IonCard,
  IonCardHeader,
  IonCardTitle,
  IonCardContent,
  IonSegment,
  IonSegmentButton,
  IonLabel
} from '@ionic/angular/standalone';
import { switchMap } from 'rxjs/operators';
import {
  AusenciaResumen,
  ServicioAusentismo,
  TipoAusenciaResumen
} from '../../nucleo/servicios/servicio-ausentismo';
import { ServicioArchivos } from '../../nucleo/servicios/servicio-archivos';
import {
  CertificadoResumen,
  ETIQUETAS_TIPO_EXAMEN,
  ServicioCertificados
} from '../../nucleo/servicios/servicio-certificados';
import { ServicioEmpleados } from '../../nucleo/servicios/servicio-empleados';
import { ServicioPermisos } from '../../nucleo/servicios/servicio-permisos';
import { EstadoCargaComponent } from '../../compartido/componentes/estado-carga/estado-carga.component';
import { EstadoVacioComponent } from '../../compartido/componentes/estado-vacio/estado-vacio.component';
import { EncabezadoPaginaComponent } from '../../compartido/componentes/encabezado-pagina/encabezado-pagina.component';
import {
  AccionTarjetaFila,
  TarjetaFilaComponent
} from '../../compartido/componentes/tarjeta-fila/tarjeta-fila.component';
import { VisorDocumentoComponent } from '../../compartido/componentes/visor-documento/visor-documento.component';
import { ServicioMensajes } from '../../nucleo/servicios/servicio-mensajes';
import { ServicioAnalitica } from '../../nucleo/servicios/servicio-analitica';
import {
  etiquetaEstadoCertificado,
  TonoEstado
} from '../../nucleo/utilidades/etiquetas-estado';

type SegmentoAusentismo = 'avisar' | 'certificados';
type OrigenArchivo = 'camara' | 'galeria' | 'archivo';
type DestinoArchivo = 'formulario' | 'suelto' | { ausenciaId: number };

@Component({
  selector: 'app-pagina-ausentismo',
  standalone: true,
  imports: [
    FormsModule,
    IonContent,
    IonHeader,
    IonTitle,
    IonToolbar,
    IonButton,
    IonText,
    IonInput,
    IonTextarea,
    IonSelect,
    IonSelectOption,
    IonToggle,
    IonItem,
    IonCard,
    IonCardHeader,
    IonCardTitle,
    IonCardContent,
    IonSegment,
    IonSegmentButton,
    IonLabel,
    EstadoCargaComponent,
    EstadoVacioComponent,
    EncabezadoPaginaComponent,
    TarjetaFilaComponent,
    VisorDocumentoComponent
  ],
  template: `
    <ion-header>
      <ion-toolbar>
        <ion-title>Ausencias</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding pagina-fondo">
      <app-encabezado-pagina
        titulo="Ausencias"
        subtitulo="Avisá una falta o subí un certificado"
      />

      @if (cargando()) {
        <app-estado-carga />
      } @else {
        <ion-segment [value]="segmentoActivo()" (ionChange)="alCambiarSegmento($event)">
          <ion-segment-button value="avisar">
            <ion-label>Avisar falta</ion-label>
          </ion-segment-button>
          @if (puedeVerCertificados()) {
            <ion-segment-button value="certificados">
              <ion-label>Certificados</ion-label>
            </ion-segment-button>
          }
        </ion-segment>

        @if (mensajeError()) {
          <ion-text color="danger"><p>{{ mensajeError() }}</p></ion-text>
        }

        @if (archivoEnVista(); as archivo) {
          <ion-card class="tarjeta-formulario">
            <ion-card-header>
              <ion-card-title>Archivo del certificado</ion-card-title>
            </ion-card-header>
            <ion-card-content>
              <app-visor-documento
                [urlFuente]="archivo"
                titulo="Archivo del certificado"
                etiquetaCarga="Cargando certificado"
                (errorCarga)="mensajeError.set($event)"
              />
              <ion-button expand="block" fill="clear" type="button" (click)="cerrarArchivoCertificado()">
                Cerrar
              </ion-button>
            </ion-card-content>
          </ion-card>
        }

        @if (segmentoActivo() === 'avisar') {
          <ion-card class="tarjeta-formulario">
            <ion-card-header>
              <ion-card-title>Avisar una falta</ion-card-title>
            </ion-card-header>
            <ion-card-content>
              <ion-select
                label="Tipo"
                labelPlacement="stacked"
                [(ngModel)]="tipoAusenciaId"
                interface="action-sheet"
              >
                @for (tipo of tipos(); track tipo.id) {
                  <ion-select-option [value]="tipo.id">{{ tipo.nombre }}</ion-select-option>
                }
              </ion-select>
              <ion-input label="Fecha" labelPlacement="stacked" type="date" [(ngModel)]="fecha"></ion-input>
              <ion-textarea
                label="Motivo"
                labelPlacement="stacked"
                [(ngModel)]="motivo"
                autoGrow="true"
              ></ion-textarea>
              <ion-item lines="none" class="toggle-item">
                <ion-toggle [(ngModel)]="justificada">¿Tenés certificado o constancia?</ion-toggle>
              </ion-item>
              @if (justificada && puedeCargarCertificado()) {
                <ion-button
                  expand="block"
                  fill="outline"
                  type="button"
                  (click)="abrirSelectorArchivo('formulario')"
                >
                  Adjuntar archivo
                </ion-button>
                @if (nombreArchivoCertificado()) {
                  <ion-text color="success">
                    <p>Archivo listo: {{ nombreArchivoCertificado() }}</p>
                  </ion-text>
                } @else if (tipoSeleccionado()?.requiereCertificado) {
                  <ion-text color="warning">
                    <p>Este tipo necesita un certificado o constancia.</p>
                  </ion-text>
                }
              }
              <ion-button expand="block" type="button" (click)="registrar()" [disabled]="guardando()">
                {{ guardando() ? 'Enviando...' : 'Avisar falta' }}
              </ion-button>
            </ion-card-content>
          </ion-card>

          <h3 class="grupo-lista-titulo">Mis ausencias</h3>
          @if (ausencias().length === 0) {
            <app-estado-vacio
              icono="medkit-outline"
              titulo="Sin faltas avisadas"
              descripcion="Cuando avises una ausencia, va a aparecer acá."
            />
          } @else {
            <div class="lista-tarjetas">
              @for (ausencia of ausencias(); track ausencia.id) {
                <app-tarjeta-fila
                  [titulo]="ausencia.tipoAusenciaNombre + ' — ' + formatearFecha(ausencia.fecha)"
                  [subtitulo]="ausencia.justificada ? 'Con certificado' : 'Sin certificado'"
                  [etiqueta]="ausencia.justificada ? 'Con certificado' : 'Sin certificado'"
                  [tonoEtiqueta]="ausencia.justificada ? 'exito' : 'alerta'"
                  [alerta]="!ausencia.justificada"
                  [acciones]="accionesPara(ausencia)"
                />
              }
            </div>
          }
        } @else if (puedeVerCertificados()) {
          <div class="cabecera-seccion">
            <h3 class="grupo-lista-titulo">Mis certificados</h3>
            @if (puedeCargarCertificado()) {
              <ion-button size="small" fill="outline" type="button" (click)="abrirSelectorArchivo('suelto')">
                Subir certificado
              </ion-button>
            }
          </div>
          @if (certificados().length === 0) {
            <app-estado-vacio
              icono="document-text-outline"
              titulo="Todavía no subiste certificados"
              descripcion="Cuando envíes uno, vas a ver acá cómo va la revisión."
            />
          } @else {
            <div class="lista-tarjetas">
              @for (certificado of certificados(); track certificado.id) {
                <app-tarjeta-fila
                  [titulo]="etiquetasTipo[certificado.tipoExamen] + ' — ' + formatearFecha(certificado.fechaEmision || certificado.createdAt)"
                  [subtitulo]="subtituloCertificado(certificado)"
                  [etiqueta]="chipCertificado(certificado).texto"
                  [tonoEtiqueta]="chipCertificado(certificado).tono"
                  [alerta]="certificado.estado === 'RECHAZADO' || certificado.estado === 'REQUIERE_ACLARACION'"
                  [acciones]="accionesCertificado(certificado)"
                />
              }
            </div>
          }
        }
      }
    </ion-content>
  `,
  styles: `
    ion-segment {
      margin-bottom: 1rem;
    }
    .tarjeta-formulario {
      margin: 0 0 1rem;
      border-radius: 14px;
    }
    ion-input, ion-textarea, ion-select { margin-bottom: 0.75rem; }
    .toggle-item { --padding-start: 0; margin-bottom: 0.75rem; }
    .cabecera-seccion {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 0.75rem;
      margin-top: 0.5rem;
    }
    .cabecera-seccion .grupo-lista-titulo { margin: 0; }
  `
})
export class PaginaAusentismoComponent implements OnInit {
  private readonly servicioAusentismo = inject(ServicioAusentismo);
  private readonly servicioArchivos = inject(ServicioArchivos);
  private readonly servicioCertificados = inject(ServicioCertificados);
  private readonly servicioEmpleados = inject(ServicioEmpleados);
  private readonly servicioMensajes = inject(ServicioMensajes);
  private readonly servicioAnalitica = inject(ServicioAnalitica);
  private readonly actionSheet = inject(ActionSheetController);
  readonly servicioPermisos = inject(ServicioPermisos);

  readonly tipos = signal<TipoAusenciaResumen[]>([]);
  readonly ausencias = signal<AusenciaResumen[]>([]);
  readonly certificados = signal<CertificadoResumen[]>([]);
  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly mensajeError = signal<string | null>(null);
  readonly archivoEnVista = signal<string | null>(null);
  readonly segmentoActivo = signal<SegmentoAusentismo>('avisar');

  readonly etiquetasTipo = ETIQUETAS_TIPO_EXAMEN;

  tipoAusenciaId: number | null = null;
  fecha = '';
  motivo = '';
  justificada = false;
  archivoCertificado: File | null = null;

  readonly tipoSeleccionado = computed(() =>
    this.tipos().find((tipo) => tipo.id === this.tipoAusenciaId) ?? null
  );

  readonly nombreArchivoCertificado = signal('');

  accionesPara(ausencia: AusenciaResumen): AccionTarjetaFila[] {
    if (ausencia.justificada || !this.puedeCargarCertificado()) {
      return [];
    }
    return [{
      id: `justificar-${ausencia.id}`,
      etiqueta: 'Adjuntar cert.',
      relleno: 'outline',
      ejecutar: () => void this.abrirSelectorArchivo({ ausenciaId: ausencia.id })
    }];
  }

  accionesCertificado(certificado: CertificadoResumen): AccionTarjetaFila[] {
    return [{
      id: `ver-${certificado.id}`,
      etiqueta: 'Ver archivo',
      relleno: 'outline',
      ejecutar: () => this.abrirArchivoCertificado(certificado.archivoUrl)
    }];
  }

  chipCertificado(certificado: CertificadoResumen): { texto: string; tono: TonoEstado } {
    return etiquetaEstadoCertificado(certificado.estado);
  }

  ngOnInit(): void {
    this.cargarDatos();
  }

  alCambiarSegmento(evento: CustomEvent): void {
    const valor = (evento.detail as { value?: string })?.value;
    if (valor === 'avisar' || valor === 'certificados') {
      this.segmentoActivo.set(valor);
    }
  }

  puedeCargarCertificado(): boolean {
    return this.servicioPermisos.tienePermiso('certificado.cargar');
  }

  puedeVerCertificados(): boolean {
    return this.servicioPermisos.tieneAlgunPermiso([
      'certificado.ver_propio',
      'certificado.cargar'
    ]);
  }

  async abrirSelectorArchivo(destino: DestinoArchivo): Promise<void> {
    const hoja = await this.actionSheet.create({
      header: 'Adjuntar archivo',
      buttons: [
        {
          text: 'Cámara',
          handler: () => {
            this.abrirInputArchivo('camara', destino);
          }
        },
        {
          text: 'Galería',
          handler: () => {
            this.abrirInputArchivo('galeria', destino);
          }
        },
        {
          text: 'Archivo',
          handler: () => {
            this.abrirInputArchivo('archivo', destino);
          }
        },
        { text: 'Cancelar', role: 'cancel' }
      ]
    });
    await hoja.present();
  }

  alSeleccionarCertificado(evento: Event): void {
    const input = evento.target as HTMLInputElement;
    const archivo = input.files?.[0] ?? null;
    this.archivoCertificado = archivo;
    this.nombreArchivoCertificado.set(archivo?.name ?? '');
  }

  seleccionarCertificadoParaAusencia(ausenciaId: number): void {
    void this.abrirSelectorArchivo({ ausenciaId });
  }

  seleccionarCertificadoSuelto(): void {
    void this.abrirSelectorArchivo('suelto');
  }

  justificarConCertificado(ausenciaId: number, archivo: File): void {
    this.guardando.set(true);
    this.mensajeError.set(null);
    this.enviarCertificadoMedico(archivo).pipe(
      switchMap((certificado) =>
        this.servicioAusentismo.justificarPropia(ausenciaId, certificado.archivoUrl)
      )
    ).subscribe({
      next: () => {
        this.guardando.set(false);
        void this.servicioMensajes.exito('Certificado enviado y ausencia justificada.');
        this.cargarDatos();
      },
      error: () => {
        this.guardando.set(false);
        this.mensajeError.set('No se pudo enviar el certificado médico.');
      }
    });
  }

  cargarCertificadoSuelto(archivo: File): void {
    this.guardando.set(true);
    this.mensajeError.set(null);
    this.enviarCertificadoMedico(archivo).subscribe({
      next: () => {
        this.guardando.set(false);
        this.servicioAnalitica.certificadoSubido();
        void this.servicioMensajes.exito('Certificado enviado a revisión médica.');
        this.cargarCertificados();
      },
      error: () => {
        this.guardando.set(false);
        this.mensajeError.set('No se pudo cargar el certificado médico.');
      }
    });
  }

  registrar(): void {
    if (!this.tipoAusenciaId || !this.fecha) {
      this.mensajeError.set('Completá tipo y fecha.');
      return;
    }

    const tipo = this.tipoSeleccionado();
    if (this.justificada && tipo?.requiereCertificado && !this.archivoCertificado) {
      this.mensajeError.set('Este tipo de ausencia requiere adjuntar un certificado médico.');
      return;
    }

    const enviarAusencia = (urlCertificado?: string) => {
      this.guardando.set(true);
      this.mensajeError.set(null);
      this.servicioAusentismo.registrarPropia({
        tipoAusenciaId: this.tipoAusenciaId!,
        fecha: this.fecha,
        motivo: this.motivo || undefined,
        justificada: this.justificada,
        urlCertificado
      }).subscribe({
        next: () => {
          this.guardando.set(false);
          this.limpiarFormulario();
          void this.servicioMensajes.exito('Ausencia registrada.');
          this.cargarDatos();
        },
        error: () => {
          this.guardando.set(false);
          this.mensajeError.set('No se pudo registrar la ausencia.');
        }
      });
    };

    if (this.justificada && this.archivoCertificado && this.puedeCargarCertificado()) {
      this.guardando.set(true);
      this.enviarCertificadoMedico(this.archivoCertificado).subscribe({
        next: (certificado) => enviarAusencia(certificado.archivoUrl),
        error: () => {
          this.guardando.set(false);
          this.mensajeError.set('No se pudo enviar el certificado médico.');
        }
      });
      return;
    }

    enviarAusencia();
  }

  subtituloCertificado(certificado: CertificadoResumen): string {
    const partes: string[] = [`Cargado el ${this.formatearFecha(certificado.createdAt)}`];
    if (certificado.reposoDias != null) {
      partes.push(`${certificado.reposoDias} día(s) de reposo`);
    }
    if (certificado.estado === 'EN_REVISION' || certificado.estado === 'EN_PROCESAMIENTO') {
      partes.push('En revisión por el equipo médico');
    }
    return partes.join(' · ');
  }

  abrirArchivoCertificado(ruta: string): void {
    this.mensajeError.set(null);
    this.archivoEnVista.set(ruta);
  }

  cerrarArchivoCertificado(): void {
    this.archivoEnVista.set(null);
  }

  private abrirInputArchivo(origen: OrigenArchivo, destino: DestinoArchivo): void {
    const input = document.createElement('input');
    input.type = 'file';
    if (origen === 'camara') {
      input.accept = 'image/*';
      input.setAttribute('capture', 'environment');
    } else if (origen === 'galeria') {
      input.accept = 'image/*';
    } else {
      input.accept = '.pdf,image/*';
    }
    input.onchange = (evento) => {
      const archivo = (evento.target as HTMLInputElement).files?.[0];
      if (archivo) {
        this.procesarArchivoSeleccionado(archivo, destino);
      }
      input.remove();
    };
    input.click();
  }

  private procesarArchivoSeleccionado(archivo: File, destino: DestinoArchivo): void {
    if (destino === 'formulario') {
      this.archivoCertificado = archivo;
      this.nombreArchivoCertificado.set(archivo.name);
      return;
    }
    if (destino === 'suelto') {
      this.cargarCertificadoSuelto(archivo);
      return;
    }
    this.justificarConCertificado(destino.ausenciaId, archivo);
  }

  private enviarCertificadoMedico(archivo: File) {
    return this.servicioArchivos.subir(archivo).pipe(
      switchMap((archivoSubido) => this.servicioCertificados.cargar({
        archivoId: archivoSubido.id,
        tipoExamen: 'AUSENTISMO',
        origenCarga: 'EMPLEADO'
      }))
    );
  }

  private limpiarFormulario(): void {
    this.fecha = '';
    this.motivo = '';
    this.justificada = false;
    this.archivoCertificado = null;
    this.nombreArchivoCertificado.set('');
  }

  private cargarDatos(): void {
    this.cargando.set(true);
    this.mensajeError.set(null);

    this.servicioAusentismo.listarTipos().subscribe({
      next: (lista) => this.tipos.set(lista),
      error: () => this.mensajeError.set('No se pudieron cargar los tipos de ausencia.')
    });

    this.servicioAusentismo.listarMisAusencias().subscribe({
      next: (lista) => {
        this.ausencias.set(lista);
        this.cargando.set(false);
      },
      error: () => {
        this.mensajeError.set('No se pudieron cargar las ausencias.');
        this.cargando.set(false);
      }
    });

    if (this.puedeVerCertificados()) {
      this.cargarCertificados();
    }
  }

  private cargarCertificados(): void {
    this.servicioEmpleados.obtenerPropio().subscribe({
      next: (empleado) => {
        this.servicioCertificados.listarPorEmpleado(empleado.id).subscribe({
          next: (lista) => this.certificados.set(lista),
          error: () => this.mensajeError.set('No se pudieron cargar tus certificados médicos.')
        });
      },
      error: () => this.mensajeError.set('No se pudo identificar tu legajo de empleado.')
    });
  }

  formatearFecha(valor: string): string {
    const fecha = new Date(valor);
    if (Number.isNaN(fecha.getTime())) {
      return valor;
    }
    return fecha.toLocaleDateString('es-AR');
  }
}
