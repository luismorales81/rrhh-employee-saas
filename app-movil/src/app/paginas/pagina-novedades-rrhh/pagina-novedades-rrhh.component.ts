import { Component, inject, OnInit, signal, computed } from '@angular/core';
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
  NovedadLiquidacionResumen,
  ServicioNovedades,
  TipoNovedadLiquidacion
} from '../../nucleo/servicios/servicio-novedades';
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

const TIPOS: TipoNovedadLiquidacion[] = [
  'HORAS_EXTRAS', 'BONIFICACION', 'COMISION', 'AUSENCIA', 'LICENCIA', 'OTRO'
];

@Component({
  selector: 'app-pagina-novedades-rrhh',
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
    EstadoCargaComponent,
    EstadoVacioComponent,
    EncabezadoPaginaComponent,
    TarjetaFilaComponent
  ],
  template: `
    <ion-header>
      <ion-toolbar color="primary">
        <ion-title>Novedades</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding pagina-fondo">
      <app-encabezado-pagina
        titulo="Novedades de liquidación"
        subtitulo="Borradores, confirmación y exportación"
      />

      <ion-input label="Período (AAAA-MM)" labelPlacement="stacked" [(ngModel)]="periodoFiltro"></ion-input>
      <div class="acciones-lote">
        <ion-button size="small" (click)="confirmarLote()" [disabled]="procesando()">Confirmar lote</ion-button>
        <ion-button size="small" color="secondary" (click)="exportarCsv()" [disabled]="procesando()">Exportar CSV</ion-button>
      </div>

      @if (cargando()) {
        <app-estado-carga />
      } @else if (mensajeError()) {
        <ion-text color="danger"><p>{{ mensajeError() }}</p></ion-text>
      } @else if (mensajeExito()) {
        <ion-text color="success"><p>{{ mensajeExito() }}</p></ion-text>
      }

      @if (!cargando() && novedadesFiltradas().length === 0) {
        <app-estado-vacio
          icono="document-text-outline"
          titulo="Sin novedades"
          descripcion="No hay novedades para el período seleccionado."
        />
      } @else if (!cargando()) {
        <div class="lista-tarjetas">
          @for (n of novedadesFiltradas(); track n.id) {
            <app-tarjeta-fila
              [titulo]="n.nombreEmpleado"
              [subtitulo]="n.tipo + ' · ' + (n.descripcion || 'Sin descripción')"
              [etiqueta]="n.estado"
              [alerta]="n.estado === 'BORRADOR'"
              [acciones]="accionesPara(n)"
            />
          }
        </div>
      }

      <ion-fab slot="fixed" vertical="bottom" horizontal="end">
        <ion-fab-button type="button" (click)="modalAbierto.set(true)">
          <ion-icon name="add-outline"></ion-icon>
        </ion-fab-button>
      </ion-fab>
    </ion-content>

    <ion-modal [isOpen]="modalAbierto()" (didDismiss)="modalAbierto.set(false)">
      <ng-template>
        <ion-header>
          <ion-toolbar>
            <ion-title>Nueva novedad</ion-title>
            <ion-button slot="end" fill="clear" type="button" (click)="modalAbierto.set(false)">Cerrar</ion-button>
          </ion-toolbar>
        </ion-header>
        <ion-content class="ion-padding">
          <ion-select label="Empleado" labelPlacement="stacked" [(ngModel)]="empleadoId">
            @for (e of empleados(); track e.id) {
              <ion-select-option [value]="e.id">{{ e.nombre }} {{ e.apellido }}</ion-select-option>
            }
          </ion-select>
          <ion-select label="Tipo" labelPlacement="stacked" [(ngModel)]="tipo">
            @for (t of tipos; track t) {
              <ion-select-option [value]="t">{{ t }}</ion-select-option>
            }
          </ion-select>
          <ion-input label="Descripción" labelPlacement="stacked" [(ngModel)]="descripcion"></ion-input>
          <ion-input type="date" label="Fecha" labelPlacement="stacked" [(ngModel)]="fecha"></ion-input>
          <ion-input type="number" label="Monto" labelPlacement="stacked" [(ngModel)]="monto"></ion-input>
          <ion-button expand="block" type="button" [disabled]="procesando()" (click)="crear()">Guardar borrador</ion-button>
        </ion-content>
      </ng-template>
    </ion-modal>
  `,
  styles: `.acciones-lote { display: flex; gap: 0.5rem; margin: 0.75rem 0; }`
})
export class PaginaNovedadesRrhhComponent implements OnInit {
  private readonly servicioNovedades = inject(ServicioNovedades);
  private readonly servicioEmpleados = inject(ServicioEmpleados);
  private readonly servicioMensajes = inject(ServicioMensajes);

  readonly tipos = TIPOS;
  readonly cargando = signal(true);
  readonly procesando = signal(false);
  readonly mensajeError = signal<string | null>(null);
  readonly mensajeExito = signal<string | null>(null);
  readonly novedades = signal<NovedadLiquidacionResumen[]>([]);
  readonly empleados = signal<EmpleadoResumen[]>([]);
  readonly modalAbierto = signal(false);

  periodoFiltro = new Date().toISOString().slice(0, 7);
  empleadoId: number | null = null;
  tipo: TipoNovedadLiquidacion = 'HORAS_EXTRAS';
  descripcion = '';
  fecha = new Date().toISOString().slice(0, 10);
  monto: number | null = null;

  readonly novedadesFiltradas = computed(() =>
    this.novedades().filter((n) => n.periodo === this.periodoFiltro)
  );

  ngOnInit(): void {
    this.recargar();
    this.servicioEmpleados.listar().subscribe({
      next: (lista) => this.empleados.set(lista)
    });
  }

  accionesPara(novedad: NovedadLiquidacionResumen): AccionTarjetaFila[] {
    if (novedad.estado !== 'BORRADOR') return [];
    return [{
      id: `eliminar-${novedad.id}`,
      etiqueta: 'Eliminar',
      color: 'danger',
      ejecutar: () => this.eliminar(novedad.id)
    }];
  }

  recargar(): void {
    this.cargando.set(true);
    this.servicioNovedades.listar().subscribe({
      next: (lista) => {
        this.novedades.set(lista);
        this.cargando.set(false);
      },
      error: () => {
        this.mensajeError.set('No se pudieron cargar las novedades.');
        this.cargando.set(false);
      }
    });
  }

  crear(): void {
    if (!this.empleadoId) return;
    this.procesando.set(true);
    this.servicioNovedades.crear({
      empleadoId: this.empleadoId,
      periodo: this.periodoFiltro,
      tipo: this.tipo,
      descripcion: this.descripcion || undefined,
      monto: this.monto ?? undefined,
      fecha: this.fecha
    }).subscribe({
      next: () => {
        this.procesando.set(false);
        this.modalAbierto.set(false);
        this.recargar();
      },
      error: () => {
        this.procesando.set(false);
        this.mensajeError.set('No se pudo crear la novedad.');
      }
    });
  }

  eliminar(id: number): void {
    this.servicioNovedades.eliminar(id).subscribe({
      next: () => this.recargar(),
      error: () => this.mensajeError.set('No se pudo eliminar.')
    });
  }

  confirmarLote(): void {
    this.procesando.set(true);
    this.servicioNovedades.confirmarLote(this.periodoFiltro).subscribe({
      next: (r) => {
        this.procesando.set(false);
        this.mensajeExito.set(`Se confirmaron ${r.cantidadConfirmadas} novedades.`);
        this.recargar();
      },
      error: (err) => {
        this.procesando.set(false);
        this.mensajeError.set(err.error?.mensaje ?? 'No se pudo confirmar el lote.');
      }
    });
  }

  exportarCsv(): void {
    this.procesando.set(true);
    this.servicioNovedades.exportarCsv(this.periodoFiltro).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `novedades-${this.periodoFiltro}.csv`;
        a.click();
        URL.revokeObjectURL(url);
        this.procesando.set(false);
        this.mensajeExito.set('CSV exportado.');
        this.recargar();
      },
      error: (err) => {
        this.procesando.set(false);
        this.mensajeError.set(err.error?.mensaje ?? 'No hay novedades confirmadas para exportar.');
      }
    });
  }
}
