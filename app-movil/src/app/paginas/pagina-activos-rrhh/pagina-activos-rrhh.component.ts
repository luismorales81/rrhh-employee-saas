import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  IonContent,
  IonHeader,
  IonTitle,
  IonToolbar,
  IonList,
  IonItem,
  IonLabel,
  IonBadge,
  IonButton,
  IonFab,
  IonFabButton,
  IonIcon,
  IonModal,
  IonInput,
  IonSelect,
  IonSelectOption,
  IonText,
  IonSpinner
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { addOutline } from 'ionicons/icons';
import {
  ActivoResumen,
  ServicioActivos,
  SolicitudAsignarActivo
} from '../../nucleo/servicios/servicio-activos';
import { EmpleadoResumen, ServicioEmpleados } from '../../nucleo/servicios/servicio-empleados';
import { EstadoCargaComponent } from '../../compartido/componentes/estado-carga/estado-carga.component';
import { EstadoVacioComponent } from '../../compartido/componentes/estado-vacio/estado-vacio.component';
import { EncabezadoPaginaComponent } from '../../compartido/componentes/encabezado-pagina/encabezado-pagina.component';
import {
  AccionTarjetaFila,
  TarjetaFilaComponent
} from '../../compartido/componentes/tarjeta-fila/tarjeta-fila.component';
import { ServicioMensajes } from '../../nucleo/servicios/servicio-mensajes';

addIcons({ addOutline });

@Component({
  selector: 'app-pagina-activos-rrhh',
  standalone: true,
  imports: [
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
    IonInput,
    IonSelect,
    IonSelectOption,
    IonText,
    IonSpinner,
    EstadoCargaComponent,
    EstadoVacioComponent,
    EncabezadoPaginaComponent,
    TarjetaFilaComponent
  ],
  template: `
    <ion-header>
      <ion-toolbar color="primary">
        <ion-title>Activos</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding pagina-fondo">
      <app-encabezado-pagina
        titulo="Inventario de activos"
        subtitulo="Crear, asignar y devolver equipos"
      />

      @if (cargando()) {
        <app-estado-carga />
      } @else if (mensajeError()) {
        <ion-text color="danger"><p>{{ mensajeError() }}</p></ion-text>
      } @else if (activos().length === 0) {
        <app-estado-vacio
          icono="laptop-outline"
          titulo="Sin activos"
          descripcion="Creá el primer activo con el botón +."
        />
      } @else {
        <div class="lista-tarjetas">
          @for (a of activos(); track a.id) {
            <app-tarjeta-fila
              [titulo]="a.nombre"
              [subtitulo]="a.tipo + ' · ' + (a.numeroSerie || 'Sin serie')"
              [etiqueta]="a.estado"
              [alerta]="a.estado === 'DISPONIBLE'"
              [acciones]="accionesPara(a)"
            />
          }
        </div>
      }

      <ion-fab slot="fixed" vertical="bottom" horizontal="end">
        <ion-fab-button type="button" (click)="modalCrearAbierto.set(true)">
          <ion-icon name="add-outline"></ion-icon>
        </ion-fab-button>
      </ion-fab>
    </ion-content>

    <ion-modal [isOpen]="modalCrearAbierto()" (didDismiss)="modalCrearAbierto.set(false)">
      <ng-template>
        <ion-header>
          <ion-toolbar>
            <ion-title>Nuevo activo</ion-title>
            <ion-button slot="end" fill="clear" type="button" (click)="modalCrearAbierto.set(false)">Cerrar</ion-button>
          </ion-toolbar>
        </ion-header>
        <ion-content class="ion-padding">
          <ion-input label="Nombre" labelPlacement="stacked" [(ngModel)]="nuevoNombre"></ion-input>
          <ion-input label="Tipo" labelPlacement="stacked" [(ngModel)]="nuevoTipo"></ion-input>
          <ion-input label="Nº serie" labelPlacement="stacked" [(ngModel)]="nuevoSerie"></ion-input>
          <ion-button expand="block" type="button" [disabled]="guardando()" (click)="crearActivo()">
            @if (guardando()) { <ion-spinner name="crescent"></ion-spinner> } @else { Crear }
          </ion-button>
        </ion-content>
      </ng-template>
    </ion-modal>

    <ion-modal [isOpen]="modalAsignarAbierto()" (didDismiss)="modalAsignarAbierto.set(false)">
      <ng-template>
        <ion-header>
          <ion-toolbar>
            <ion-title>Asignar activo</ion-title>
            <ion-button slot="end" fill="clear" type="button" (click)="modalAsignarAbierto.set(false)">Cerrar</ion-button>
          </ion-toolbar>
        </ion-header>
        <ion-content class="ion-padding">
          <ion-select label="Empleado" labelPlacement="stacked" [(ngModel)]="empleadoAsignarId">
            @for (e of empleados(); track e.id) {
              <ion-select-option [value]="e.id">{{ e.nombre }} {{ e.apellido }}</ion-select-option>
            }
          </ion-select>
          <ion-input type="date" label="Fecha entrega" labelPlacement="stacked" [(ngModel)]="fechaEntrega"></ion-input>
          <ion-button expand="block" type="button" [disabled]="guardando()" (click)="asignarActivo()">Asignar</ion-button>
        </ion-content>
      </ng-template>
    </ion-modal>
  `,
})
export class PaginaActivosRrhhComponent implements OnInit {
  private readonly servicioActivos = inject(ServicioActivos);
  private readonly servicioEmpleados = inject(ServicioEmpleados);
  private readonly servicioMensajes = inject(ServicioMensajes);

  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly mensajeError = signal<string | null>(null);
  readonly activos = signal<ActivoResumen[]>([]);
  readonly empleados = signal<EmpleadoResumen[]>([]);
  readonly modalCrearAbierto = signal(false);
  readonly modalAsignarAbierto = signal(false);
  readonly activoSeleccionado = signal<ActivoResumen | null>(null);

  nuevoNombre = '';
  nuevoTipo = '';
  nuevoSerie = '';
  empleadoAsignarId: number | null = null;
  fechaEntrega = new Date().toISOString().slice(0, 10);

  ngOnInit(): void {
    this.recargar();
    this.servicioEmpleados.listar().subscribe({
      next: (lista) => this.empleados.set(lista),
      error: () => {}
    });
  }

  accionesPara(activo: ActivoResumen): AccionTarjetaFila[] {
    if (activo.estado === 'DISPONIBLE') {
      return [{
        id: `asignar-${activo.id}`,
        etiqueta: 'Asignar',
        ejecutar: () => this.abrirAsignar(activo)
      }];
    }
    if (activo.estado === 'ASIGNADO') {
      return [{
        id: `devolver-${activo.id}`,
        etiqueta: 'Devolver',
        color: 'secondary',
        ejecutar: () => this.devolver(activo.id)
      }];
    }
    return [];
  }

  recargar(): void {
    this.cargando.set(true);
    this.servicioActivos.listar().subscribe({
      next: (lista) => {
        this.activos.set(lista);
        this.cargando.set(false);
      },
      error: () => {
        this.mensajeError.set('No se pudieron cargar los activos.');
        this.cargando.set(false);
      }
    });
  }

  crearActivo(): void {
    if (!this.nuevoNombre.trim() || !this.nuevoTipo.trim()) return;
    this.guardando.set(true);
    this.servicioActivos.crear({
      nombre: this.nuevoNombre.trim(),
      tipo: this.nuevoTipo.trim(),
      numeroSerie: this.nuevoSerie.trim() || undefined
    }).subscribe({
      next: () => {
        this.guardando.set(false);
        this.modalCrearAbierto.set(false);
        this.nuevoNombre = '';
        this.nuevoTipo = '';
        this.nuevoSerie = '';
        this.recargar();
      },
      error: () => {
        this.guardando.set(false);
        this.mensajeError.set('No se pudo crear el activo.');
      }
    });
  }

  abrirAsignar(activo: ActivoResumen): void {
    this.activoSeleccionado.set(activo);
    this.modalAsignarAbierto.set(true);
  }

  asignarActivo(): void {
    const activo = this.activoSeleccionado();
    if (!activo || !this.empleadoAsignarId) return;
    const solicitud: SolicitudAsignarActivo = {
      empleadoId: this.empleadoAsignarId,
      fechaEntrega: this.fechaEntrega
    };
    this.guardando.set(true);
    this.servicioActivos.asignar(activo.id, solicitud).subscribe({
      next: () => {
        this.guardando.set(false);
        this.modalAsignarAbierto.set(false);
        this.recargar();
      },
      error: () => {
        this.guardando.set(false);
        this.mensajeError.set('No se pudo asignar el activo.');
      }
    });
  }

  devolver(activoId: number): void {
    this.servicioActivos.devolver(activoId, { fechaDevolucion: new Date().toISOString().slice(0, 10) }).subscribe({
      next: () => this.recargar(),
      error: () => this.mensajeError.set('No se pudo registrar la devolución.')
    });
  }
}
