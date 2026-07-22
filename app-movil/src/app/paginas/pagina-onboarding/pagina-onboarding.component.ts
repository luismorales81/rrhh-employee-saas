import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import {
  IonContent,
  IonHeader,
  IonTitle,
  IonToolbar,
  IonText,
  IonButton
} from '@ionic/angular/standalone';
import { ServicioEmpleados } from '../../nucleo/servicios/servicio-empleados';
import { ServicioOnboarding } from '../../nucleo/servicios/servicio-onboarding';
import { BloqueOnboardingComponent } from '../../compartido/componentes/bloque-onboarding/bloque-onboarding.component';
import { EstadoCargaComponent } from '../../compartido/componentes/estado-carga/estado-carga.component';
import { EstadoVacioComponent } from '../../compartido/componentes/estado-vacio/estado-vacio.component';
import { EncabezadoPaginaComponent } from '../../compartido/componentes/encabezado-pagina/encabezado-pagina.component';
import { DirectivaLiberarFocoNavegacion } from '../../compartido/directivas/directiva-liberar-foco-navegacion';

@Component({
  selector: 'app-pagina-onboarding',
  standalone: true,
  imports: [
    RouterLink,
    IonContent,
    IonHeader,
    IonTitle,
    IonToolbar,
    IonText,
    IonButton,
    BloqueOnboardingComponent,
    EstadoCargaComponent,
    EstadoVacioComponent,
    EncabezadoPaginaComponent,
    DirectivaLiberarFocoNavegacion
  ],
  template: `
    <ion-header>
      <ion-toolbar>
        <ion-title>Primeros pasos</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding pagina-fondo">
      <app-encabezado-pagina
        titulo="Bienvenida"
        subtitulo="Completá el checklist para terminar tu ingreso"
      />

      @if (cargando()) {
        <app-estado-carga mensaje="Cargando tareas..." />
      } @else if (mensajeError()) {
        <ion-text color="danger"><p>{{ mensajeError() }}</p></ion-text>
      } @else if (estadoOnboarding() !== 'EN_PROGRESO') {
        <app-estado-vacio
          icono="checkmark-done-outline"
          titulo="Sin ingreso activo"
          descripcion="No tenés primeros pasos pendientes en este momento."
          textoAccion="Volver al inicio"
          rutaAccion="/tabs/inicio"
        />
      } @else if (tareas().length === 0) {
        <app-estado-vacio
          icono="checkmark-done-outline"
          titulo="Sin tareas pendientes"
          descripcion="Las tareas aparecen cuando tu equipo te da la bienvenida."
        />
      } @else {
        <app-bloque-onboarding
          [tareas]="tareas()"
          [procesando]="procesando()"
          (completar)="marcarCompletada($event)"
        />
        <ion-button expand="block" fill="outline" routerLink="/tabs/documentos">
          Ir a documentos para firmar
        </ion-button>
        <ion-button expand="block" fill="clear" routerLink="/tabs/perfil">
          Revisar mis datos
        </ion-button>
      }
    </ion-content>
  `
})
export class PaginaOnboardingComponent implements OnInit {
  private readonly servicioEmpleados = inject(ServicioEmpleados);
  private readonly servicioOnboarding = inject(ServicioOnboarding);

  readonly cargando = signal(true);
  readonly procesando = signal(false);
  readonly mensajeError = signal<string | null>(null);
  readonly estadoOnboarding = signal<string | null>(null);
  readonly tareas = signal<{ id: number; nombre: string; completada: boolean }[]>([]);
  private empleadoId = 0;

  ngOnInit(): void {
    this.servicioEmpleados.obtenerPropio().subscribe({
      next: (empleado) => {
        this.empleadoId = empleado.id;
        this.servicioEmpleados.obtenerLegajo(empleado.id).subscribe({
          next: (legajo) => {
            this.estadoOnboarding.set(legajo.estadoOnboarding);
            if (legajo.estadoOnboarding === 'EN_PROGRESO') {
              this.cargarTareas();
            } else {
              this.cargando.set(false);
            }
          },
          error: () => {
            this.mensajeError.set('No se pudieron cargar tus primeros pasos.');
            this.cargando.set(false);
          }
        });
      },
      error: () => {
        this.mensajeError.set('No se pudo identificar tu empleado.');
        this.cargando.set(false);
      }
    });
  }

  marcarCompletada(tareaId: number): void {
    this.procesando.set(true);
    this.servicioOnboarding.completarTarea(this.empleadoId, tareaId).subscribe({
      next: () => {
        this.procesando.set(false);
        this.cargarTareas();
      },
      error: () => {
        this.procesando.set(false);
        this.mensajeError.set('No se pudo completar la tarea.');
      }
    });
  }

  private cargarTareas(): void {
    this.servicioOnboarding.listarTareasEmpleado(this.empleadoId).subscribe({
      next: (lista) => {
        this.tareas.set(lista);
        this.cargando.set(false);
      },
      error: () => {
        this.mensajeError.set('No se pudieron cargar las tareas.');
        this.cargando.set(false);
      }
    });
  }
}
