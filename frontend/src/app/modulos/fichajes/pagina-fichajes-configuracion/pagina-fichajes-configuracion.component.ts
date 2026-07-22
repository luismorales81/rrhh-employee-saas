import { Component, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DirectivaTienePermiso } from '../../../nucleo/directivas/directiva-tiene-permiso';
import {
  ConfiguracionFichajeEmpleadoResumen,
  ServicioFichajes
} from '../../../nucleo/servicios/servicio-fichajes';

@Component({
  selector: 'app-pagina-fichajes-configuracion',
  standalone: true,
  imports: [ReactiveFormsModule, DirectivaTienePermiso, RouterLink],
  templateUrl: './pagina-fichajes-configuracion.component.html'
})
export class PaginaFichajesConfiguracionComponent implements OnInit {
  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly error = signal<string | null>(null);
  readonly configuraciones = signal<ConfiguracionFichajeEmpleadoResumen[]>([]);
  readonly mostrarPolitica = signal(false);
  readonly textoPolitica = signal('');

  readonly formularioPolitica;

  constructor(
    fb: FormBuilder,
    private readonly servicioFichajes: ServicioFichajes
  ) {
    this.formularioPolitica = fb.nonNullable.group({
      textoTransparencia: [''],
      permitirFichajeSinGps: [false],
      precisionMaximaMetros: [100]
    });
  }

  ngOnInit(): void {
    this.cargar();
    this.servicioFichajes.obtenerPolitica().subscribe({
      next: (p) => {
        this.textoPolitica.set(p.textoTransparencia);
        this.formularioPolitica.patchValue({
          textoTransparencia: p.textoTransparencia,
          permitirFichajeSinGps: p.permitirFichajeSinGps,
          precisionMaximaMetros: p.precisionMaximaMetros
        });
      }
    });
  }

  cargar(): void {
    this.cargando.set(true);
    this.servicioFichajes.listarConfiguraciones().subscribe({
      next: (lista) => {
        this.configuraciones.set(lista);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(err.error?.mensaje ?? 'Error al cargar');
        this.cargando.set(false);
      }
    });
  }

  toggleMovil(config: ConfiguracionFichajeEmpleadoResumen): void {
    this.actualizar(config, {
      fichajeMovilHabilitado: !config.fichajeMovilHabilitado,
      restringirSoloZonasAsignadas: config.restringirSoloZonasAsignadas
    });
  }

  toggleRestringir(config: ConfiguracionFichajeEmpleadoResumen): void {
    this.actualizar(config, {
      fichajeMovilHabilitado: config.fichajeMovilHabilitado,
      restringirSoloZonasAsignadas: !config.restringirSoloZonasAsignadas
    });
  }

  alternarPolitica(): void {
    this.mostrarPolitica.update((v) => !v);
  }

  guardarPolitica(): void {
    this.guardando.set(true);
    this.servicioFichajes.actualizarPolitica(this.formularioPolitica.getRawValue()).subscribe({
      next: (p) => {
        this.textoPolitica.set(p.textoTransparencia);
        this.guardando.set(false);
        this.mostrarPolitica.set(false);
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(err.error?.mensaje ?? 'Error al guardar política');
      }
    });
  }

  private actualizar(
    config: ConfiguracionFichajeEmpleadoResumen,
    solicitud: { fichajeMovilHabilitado: boolean; restringirSoloZonasAsignadas: boolean }
  ): void {
    this.guardando.set(true);
    this.servicioFichajes.actualizarConfiguracion(config.empleadoId, solicitud).subscribe({
      next: () => {
        this.guardando.set(false);
        this.cargar();
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(err.error?.mensaje ?? 'Error al actualizar');
      }
    });
  }
}
