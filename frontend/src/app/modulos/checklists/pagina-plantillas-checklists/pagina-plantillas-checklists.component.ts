import { Component, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DirectivaTienePermiso } from '../../../nucleo/directivas/directiva-tiene-permiso';
import {
  PlantillaTareaOnboardingResumen,
  ServicioOnboarding
} from '../../../nucleo/servicios/servicio-onboarding';
import {
  PlantillaTareaOffboardingResumen,
  ServicioOffboarding
} from '../../../nucleo/servicios/servicio-offboarding';

type PestanaChecklist = 'onboarding' | 'offboarding';

const TIPOS_ONBOARDING = ['PERFIL', 'FIRMAR_CONTRATO', 'LEER_COMUNICACION', 'RECIBIR_NOTEBOOK'];
const TIPOS_OFFBOARDING = ['DEVOLVER_ACTIVO'];

@Component({
  selector: 'app-pagina-plantillas-checklists',
  standalone: true,
  imports: [ReactiveFormsModule, DirectivaTienePermiso],
  templateUrl: './pagina-plantillas-checklists.component.html'
})
export class PaginaPlantillasChecklistsComponent implements OnInit {
  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly error = signal<string | null>(null);
  readonly pestanaActiva = signal<PestanaChecklist>('onboarding');
  readonly mostrarFormulario = signal(false);
  readonly plantillasOnboarding = signal<PlantillaTareaOnboardingResumen[]>([]);
  readonly plantillasOffboarding = signal<PlantillaTareaOffboardingResumen[]>([]);
  readonly editandoOnboardingId = signal<number | null>(null);
  readonly editandoOffboardingId = signal<number | null>(null);
  readonly tiposOnboarding = TIPOS_ONBOARDING;
  readonly tiposOffboarding = TIPOS_OFFBOARDING;
  readonly formulario;

  constructor(
    fb: FormBuilder,
    private readonly servicioOnboarding: ServicioOnboarding,
    private readonly servicioOffboarding: ServicioOffboarding
  ) {
    this.formulario = fb.nonNullable.group({
      nombre: ['', Validators.required],
      orden: [1, [Validators.required, Validators.min(1)]],
      obligatoria: [true],
      tipo: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.recargar();
  }

  recargar(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.servicioOnboarding.listarPlantillas().subscribe({
      next: (lista) => this.plantillasOnboarding.set(lista),
      error: () => this.error.set('No se pudieron cargar las plantillas de onboarding')
    });
    this.servicioOffboarding.listarPlantillas().subscribe({
      next: (lista) => {
        this.plantillasOffboarding.set(lista);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar las plantillas de offboarding');
        this.cargando.set(false);
      }
    });
  }

  cambiarPestana(pestana: PestanaChecklist): void {
    this.pestanaActiva.set(pestana);
    this.cancelarFormulario();
  }

  alternarFormulario(): void {
    if (this.mostrarFormulario()) {
      this.cancelarFormulario();
    } else {
      this.editandoOnboardingId.set(null);
      this.editandoOffboardingId.set(null);
      const siguienteOrden = this.pestanaActiva() === 'onboarding'
        ? this.plantillasOnboarding().length + 1
        : this.plantillasOffboarding().length + 1;
      this.formulario.reset({
        nombre: '',
        orden: siguienteOrden,
        obligatoria: true,
        tipo: this.pestanaActiva() === 'onboarding' ? 'PERFIL' : 'DEVOLVER_ACTIVO'
      });
      this.mostrarFormulario.set(true);
    }
  }

  cancelarFormulario(): void {
    this.mostrarFormulario.set(false);
    this.editandoOnboardingId.set(null);
    this.editandoOffboardingId.set(null);
  }

  editarOnboarding(plantilla: PlantillaTareaOnboardingResumen): void {
    this.pestanaActiva.set('onboarding');
    this.editandoOnboardingId.set(plantilla.id);
    this.editandoOffboardingId.set(null);
    this.formulario.patchValue({
      nombre: plantilla.nombre,
      orden: plantilla.orden,
      obligatoria: plantilla.obligatoria,
      tipo: plantilla.tipo
    });
    this.mostrarFormulario.set(true);
  }

  editarOffboarding(plantilla: PlantillaTareaOffboardingResumen): void {
    this.pestanaActiva.set('offboarding');
    this.editandoOffboardingId.set(plantilla.id);
    this.editandoOnboardingId.set(null);
    this.formulario.patchValue({
      nombre: plantilla.nombre,
      orden: plantilla.orden,
      obligatoria: plantilla.obligatoria,
      tipo: plantilla.tipo
    });
    this.mostrarFormulario.set(true);
  }

  guardar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    const datos = this.formulario.getRawValue();
    this.guardando.set(true);
    this.error.set(null);

    if (this.pestanaActiva() === 'onboarding') {
      const id = this.editandoOnboardingId();
      const peticion = id
        ? this.servicioOnboarding.actualizarPlantilla(id, datos)
        : this.servicioOnboarding.crearPlantilla(datos);
      peticion.subscribe({
        next: () => {
          this.guardando.set(false);
          this.cancelarFormulario();
          this.recargar();
        },
        error: (err) => {
          this.guardando.set(false);
          this.error.set(err.error?.mensaje ?? 'No se pudo guardar la plantilla');
        }
      });
    } else {
      const id = this.editandoOffboardingId();
      const peticion = id
        ? this.servicioOffboarding.actualizarPlantilla(id, datos)
        : this.servicioOffboarding.crearPlantilla(datos);
      peticion.subscribe({
        next: () => {
          this.guardando.set(false);
          this.cancelarFormulario();
          this.recargar();
        },
        error: (err) => {
          this.guardando.set(false);
          this.error.set(err.error?.mensaje ?? 'No se pudo guardar la plantilla');
        }
      });
    }
  }

  eliminarOnboarding(id: number): void {
    if (!confirm('¿Eliminar esta plantilla de onboarding?')) return;
    this.servicioOnboarding.eliminarPlantilla(id).subscribe({
      next: () => this.recargar(),
      error: (err) => this.error.set(err.error?.mensaje ?? 'No se pudo eliminar')
    });
  }

  eliminarOffboarding(id: number): void {
    if (!confirm('¿Eliminar esta plantilla de offboarding?')) return;
    this.servicioOffboarding.eliminarPlantilla(id).subscribe({
      next: () => this.recargar(),
      error: (err) => this.error.set(err.error?.mensaje ?? 'No se pudo eliminar')
    });
  }
}
