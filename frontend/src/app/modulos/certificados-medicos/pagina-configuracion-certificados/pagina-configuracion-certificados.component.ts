import { Component, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {
  ConfiguracionAuditoriaMedica,
  ServicioCertificados,
  SolicitudActualizarConfiguracionAuditoriaMedica
} from '../../../nucleo/servicios/servicio-certificados';

@Component({
  selector: 'app-pagina-configuracion-certificados',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './pagina-configuracion-certificados.component.html'
})
export class PaginaConfiguracionCertificadosComponent implements OnInit {
  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly error = signal<string | null>(null);
  readonly exito = signal<string | null>(null);
  readonly configActual = signal<ConfiguracionAuditoriaMedica | null>(null);
  readonly modelosDisponibles = signal<string[]>([]);

  readonly formulario;

  private credencialesModificadas = false;

  constructor(
    fb: FormBuilder,
    private readonly servicioCertificados: ServicioCertificados
  ) {
    this.formulario = fb.nonNullable.group({
      diasReposoAlertaLeve: [5, [Validators.required, Validators.min(1), Validators.max(90)]],
      maxLicenciasCortasPeriodo: [3, [Validators.required, Validators.min(1), Validators.max(20)]],
      periodoEvaluacionDias: [90, [Validators.required, Validators.min(30), Validators.max(365)]],
      requiereRevisionManualSiempre: [true],
      proyectoGcp: [''],
      regionVertex: [''],
      modeloGemini: [''],
      credencialesServicioJson: [''],
      maxIntentosExtraccion: [3, [Validators.min(1), Validators.max(10)]]
    });
  }

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.servicioCertificados.obtenerConfiguracion().subscribe({
      next: (config) => {
        this.configActual.set(config);
        this.modelosDisponibles.set(config.modelosDisponibles ?? []);
        this.formulario.patchValue({
          diasReposoAlertaLeve: config.diasReposoAlertaLeve,
          maxLicenciasCortasPeriodo: config.maxLicenciasCortasPeriodo,
          periodoEvaluacionDias: config.periodoEvaluacionDias,
          requiereRevisionManualSiempre: config.requiereRevisionManualSiempre,
          proyectoGcp: config.proyectoGcp ?? '',
          regionVertex: config.regionVertex ?? '',
          modeloGemini: config.modeloGemini ?? '',
          maxIntentosExtraccion: config.maxIntentosExtraccion
        });
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(err.error?.mensaje ?? 'No se pudo cargar la configuración');
        this.cargando.set(false);
      }
    });
  }

  marcarCredencialesModificadas(): void {
    this.credencialesModificadas = true;
  }

  guardar(): void {
    if (this.formulario.invalid) {
      return;
    }
    const valores = this.formulario.getRawValue();
    const solicitud: SolicitudActualizarConfiguracionAuditoriaMedica = {
      diasReposoAlertaLeve: valores.diasReposoAlertaLeve,
      maxLicenciasCortasPeriodo: valores.maxLicenciasCortasPeriodo,
      periodoEvaluacionDias: valores.periodoEvaluacionDias,
      requiereRevisionManualSiempre: valores.requiereRevisionManualSiempre,
      proyectoGcp: valores.proyectoGcp || null,
      regionVertex: valores.regionVertex || null,
      modeloGemini: valores.modeloGemini || null,
      maxIntentosExtraccion: valores.maxIntentosExtraccion
    };
    if (this.credencialesModificadas && valores.credencialesServicioJson.trim()) {
      solicitud.credencialesServicioJson = valores.credencialesServicioJson.trim();
    }

    this.guardando.set(true);
    this.error.set(null);
    this.exito.set(null);
    this.servicioCertificados.actualizarConfiguracion(solicitud).subscribe({
      next: (config) => {
        this.configActual.set(config);
        this.guardando.set(false);
        this.exito.set('Configuración guardada correctamente');
        this.credencialesModificadas = false;
        this.formulario.patchValue({ credencialesServicioJson: '' });
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo guardar la configuración');
      }
    });
  }
}
