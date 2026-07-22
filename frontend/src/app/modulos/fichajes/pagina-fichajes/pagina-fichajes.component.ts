import { DatePipe } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DirectivaTienePermiso } from '../../../nucleo/directivas/directiva-tiene-permiso';
import { MapaMarcaFichajeComponent } from '../../../compartido/componentes/mapa-marca-fichaje/mapa-marca-fichaje.component';
import {
  EmpleadoResumen,
  ServicioEmpleados
} from '../../../nucleo/servicios/servicio-empleados';
import {
  EstadoMarca,
  MarcaFichajeResumen,
  MotivoEstado,
  ResumenFichajeEmpleado,
  ServicioFichajes
} from '../../../nucleo/servicios/servicio-fichajes';

@Component({
  selector: 'app-pagina-fichajes',
  standalone: true,
  imports: [ReactiveFormsModule, DirectivaTienePermiso, RouterLink, DatePipe, MapaMarcaFichajeComponent],
  templateUrl: './pagina-fichajes.component.html'
})
export class PaginaFichajesComponent implements OnInit {
  readonly pestana = signal<'todas' | 'pendientes' | 'resumen'>('todas');
  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly error = signal<string | null>(null);
  readonly marcas = signal<MarcaFichajeResumen[]>([]);
  readonly resumen = signal<ResumenFichajeEmpleado[]>([]);
  readonly empleados = signal<EmpleadoResumen[]>([]);
  readonly marcaDetalle = signal<MarcaFichajeResumen | null>(null);
  readonly empleadoDetalle = signal<EmpleadoResumen | null>(null);
  readonly restringirZonasDetalle = signal(false);
  readonly notaRechazo = signal('');
  readonly conteoPendientesRevision = signal(0);

  readonly filtros;

  constructor(
    fb: FormBuilder,
    private readonly servicioFichajes: ServicioFichajes,
    private readonly servicioEmpleados: ServicioEmpleados
  ) {
    const hoy = new Date();
    const inicioMes = new Date(hoy.getFullYear(), hoy.getMonth(), 1);
    this.filtros = fb.nonNullable.group({
      empleadoId: [''],
      estado: ['' as EstadoMarca | ''],
      desde: [inicioMes.toISOString().slice(0, 10)],
      hasta: [hoy.toISOString().slice(0, 10)]
    });
  }

  ngOnInit(): void {
    this.servicioEmpleados.listar().subscribe({
      next: (lista) => this.empleados.set(lista)
    });
    this.cargar();
  }

  cambiarPestana(p: 'todas' | 'pendientes' | 'resumen'): void {
    this.pestana.set(p);
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);
    if (this.pestana() === 'pendientes') {
      this.servicioFichajes.listarPendientesRevision().subscribe({
        next: (lista) => {
          this.marcas.set(lista);
          this.conteoPendientesRevision.set(lista.length);
          this.cargando.set(false);
        },
        error: (err) => this.manejarError(err)
      });
      return;
    }
    if (this.pestana() === 'resumen') {
      const { desde, hasta } = this.filtros.getRawValue();
      this.servicioFichajes.obtenerResumen(desde || undefined, hasta || undefined).subscribe({
        next: (lista) => {
          this.resumen.set(lista);
          this.cargando.set(false);
        },
        error: (err) => this.manejarError(err)
      });
      return;
    }
    const f = this.filtros.getRawValue();
    this.servicioFichajes.listarMarcas({
      empleadoId: f.empleadoId ? Number(f.empleadoId) : undefined,
      estado: f.estado || undefined,
      desde: f.desde || undefined,
      hasta: f.hasta || undefined
    }).subscribe({
      next: (lista) => {
        this.marcas.set(lista);
        this.cargando.set(false);
      },
      error: (err) => this.manejarError(err)
    });
  }

  exportarCsv(): void {
    const f = this.filtros.getRawValue();
    this.servicioFichajes.exportarCsv(
      f.desde || undefined,
      f.hasta || undefined,
      f.empleadoId ? Number(f.empleadoId) : undefined
    ).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `fichajes_${f.desde}.csv`;
        a.click();
        URL.revokeObjectURL(url);
      }
    });
  }

  verDetalle(marca: MarcaFichajeResumen): void {
    this.marcaDetalle.set(marca);
    this.notaRechazo.set('');
    const empleado = this.empleados().find((e) => e.id === marca.empleadoId) ?? null;
    this.empleadoDetalle.set(empleado);
    this.restringirZonasDetalle.set(false);

    this.servicioFichajes.listarConfiguraciones().subscribe({
      next: (configs) => {
        const config = configs.find((c) => c.empleadoId === marca.empleadoId);
        this.restringirZonasDetalle.set(config?.restringirSoloZonasAsignadas ?? false);
      }
    });
  }

  cerrarDetalle(): void {
    this.marcaDetalle.set(null);
    this.empleadoDetalle.set(null);
  }

  aprobar(id: number): void {
    this.guardando.set(true);
    this.servicioFichajes.aprobarMarca(id).subscribe({
      next: () => {
        this.guardando.set(false);
        this.cerrarDetalle();
        this.cargar();
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(err.error?.mensaje ?? 'Error al aprobar');
      }
    });
  }

  rechazar(id: number): void {
    const nota = this.notaRechazo().trim();
    if (nota.length < 5) {
      this.error.set('Indicá un motivo de rechazo (mín. 5 caracteres)');
      return;
    }
    this.guardando.set(true);
    this.servicioFichajes.rechazarMarca(id, nota).subscribe({
      next: () => {
        this.guardando.set(false);
        this.cerrarDetalle();
        this.cargar();
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(err.error?.mensaje ?? 'Error al rechazar');
      }
    });
  }

  etiquetaEstado(estado: string): string {
    const mapa: Record<string, string> = {
      VALIDADA: 'Validada',
      PENDIENTE_REVISION: 'Pendiente',
      RECHAZADA: 'Rechazada'
    };
    return mapa[estado] ?? estado;
  }

  etiquetaMotivo(motivo: MotivoEstado | null): string {
    if (!motivo) {
      return '—';
    }
    const mapa: Record<MotivoEstado, string> = {
      FUERA_DE_ZONA: 'Fuera de zona',
      SIN_ZONA_HABILITADA: 'Sin zona habilitada',
      PRECISION_BAJA: 'Precisión GPS baja'
    };
    return mapa[motivo] ?? motivo;
  }

  etiquetaUbicacion(marca: MarcaFichajeResumen): string {
    if (marca.latitud == null || marca.longitud == null) {
      return 'Sin GPS';
    }
    if (marca.estado === 'VALIDADA' && marca.zonaFichajeNombre) {
      return `En ${marca.zonaFichajeNombre}`;
    }
    if (marca.motivoEstado === 'FUERA_DE_ZONA') {
      return 'Fuera de zona';
    }
    return 'Con GPS';
  }

  enlaceGoogleMaps(marca: MarcaFichajeResumen): string | null {
    if (marca.latitud == null || marca.longitud == null) {
      return null;
    }
    return `https://www.google.com/maps?q=${marca.latitud},${marca.longitud}`;
  }

  private manejarError(err: { error?: { mensaje?: string } }): void {
    this.error.set(err.error?.mensaje ?? 'Error al cargar datos');
    this.cargando.set(false);
  }
}
