import { Component, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DirectivaTienePermiso } from '../../../nucleo/directivas/directiva-tiene-permiso';
import { SelectorArchivoComponent } from '../../../compartido/componentes/selector-archivo/selector-archivo.component';
import { VisorDocumentoComponent } from '../../../compartido/componentes/visor-documento/visor-documento.component';
import {
  AsignacionDocumentoResumen,
  CategoriaDocumentoResumen,
  DocumentoResumen,
  EvidenciaFirmaResumen,
  ServicioDocumentos
} from '../../../nucleo/servicios/servicio-documentos';
import { EmpleadoResumen, ServicioEmpleados } from '../../../nucleo/servicios/servicio-empleados';

type PestanaDocumentos = 'catalogo' | 'categorias' | 'asignaciones';

@Component({
  selector: 'app-pagina-documentos',
  standalone: true,
  imports: [ReactiveFormsModule, DirectivaTienePermiso, SelectorArchivoComponent, VisorDocumentoComponent],
  templateUrl: './pagina-documentos.component.html'
})
export class PaginaDocumentosComponent implements OnInit {
  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly error = signal<string | null>(null);
  readonly documentos = signal<DocumentoResumen[]>([]);
  readonly categorias = signal<CategoriaDocumentoResumen[]>([]);
  readonly asignaciones = signal<AsignacionDocumentoResumen[]>([]);
  readonly empleados = signal<EmpleadoResumen[]>([]);
  readonly pestanaActiva = signal<PestanaDocumentos>('catalogo');
  readonly mostrarFormularioCategoria = signal(false);
  readonly mostrarFormularioDocumento = signal(false);
  readonly mostrarFormularioAsignar = signal(false);
  readonly empleadosSeleccionados = signal<Set<number>>(new Set());
  readonly evidenciaFirma = signal<EvidenciaFirmaResumen | null>(null);
  readonly documentoEnVista = signal<DocumentoResumen | null>(null);
  readonly asignacionEnVista = signal<AsignacionDocumentoResumen | null>(null);
  readonly formularioCategoria;
  readonly formularioDocumento;
  readonly formularioAsignar;

  constructor(
    fb: FormBuilder,
    private readonly servicioDocumentos: ServicioDocumentos,
    private readonly servicioEmpleados: ServicioEmpleados
  ) {
    this.formularioCategoria = fb.nonNullable.group({
      nombre: ['', Validators.required],
      descripcion: ['']
    });
    this.formularioDocumento = fb.nonNullable.group({
      categoriaId: [0, Validators.required],
      titulo: ['', Validators.required],
      descripcion: [''],
      version: ['1.0'],
      urlArchivo: ['', Validators.required],
      requiereFirma: [false]
    });
    this.formularioAsignar = fb.nonNullable.group({
      documentoId: [0, Validators.required],
      fechaVencimiento: ['']
    });
  }

  ngOnInit(): void {
    this.servicioEmpleados.listar().subscribe({
      next: (lista) => this.empleados.set(lista)
    });
    this.recargar();
  }

  recargar(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.servicioDocumentos.listarCategorias().subscribe({
      next: (lista) => this.categorias.set(lista)
    });
    this.servicioDocumentos.listarDocumentos().subscribe({
      next: (lista) => this.documentos.set(lista)
    });
    this.servicioDocumentos.listarAsignaciones().subscribe({
      next: (lista) => {
        this.asignaciones.set(lista);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar los documentos');
        this.cargando.set(false);
      }
    });
  }

  cambiarPestana(pestana: PestanaDocumentos): void {
    this.pestanaActiva.set(pestana);
    this.mostrarFormularioCategoria.set(false);
    this.mostrarFormularioDocumento.set(false);
    this.mostrarFormularioAsignar.set(false);
  }

  crearCategoria(): void {
    if (this.formularioCategoria.invalid) {
      this.formularioCategoria.markAllAsTouched();
      return;
    }

    const datos = this.formularioCategoria.getRawValue();
    this.guardando.set(true);
    this.servicioDocumentos.crearCategoria({
      nombre: datos.nombre,
      descripcion: datos.descripcion || undefined
    }).subscribe({
      next: () => {
        this.guardando.set(false);
        this.formularioCategoria.reset();
        this.mostrarFormularioCategoria.set(false);
        this.recargar();
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo crear la categoría');
      }
    });
  }

  establecerUrlArchivo(url: string): void {
    this.formularioDocumento.patchValue({ urlArchivo: url });
  }

  crearDocumento(): void {
    if (this.formularioDocumento.invalid || this.formularioDocumento.getRawValue().categoriaId === 0) {
      this.formularioDocumento.markAllAsTouched();
      return;
    }

    const datos = this.formularioDocumento.getRawValue();
    this.guardando.set(true);
    this.servicioDocumentos.crearDocumento({
      categoriaId: Number(datos.categoriaId),
      titulo: datos.titulo,
      descripcion: datos.descripcion || undefined,
      version: datos.version || undefined,
      urlArchivo: datos.urlArchivo,
      requiereFirma: datos.requiereFirma
    }).subscribe({
      next: () => {
        this.guardando.set(false);
        this.formularioDocumento.reset({
          categoriaId: 0,
          version: '1.0',
          requiereFirma: false
        });
        this.mostrarFormularioDocumento.set(false);
        this.recargar();
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo crear el documento');
      }
    });
  }

  alternarEmpleado(empleadoId: number): void {
    this.empleadosSeleccionados.update((set) => {
      const copia = new Set(set);
      if (copia.has(empleadoId)) {
        copia.delete(empleadoId);
      } else {
        copia.add(empleadoId);
      }
      return copia;
    });
  }

  empleadoMarcado(empleadoId: number): boolean {
    return this.empleadosSeleccionados().has(empleadoId);
  }

  asignarDocumento(): void {
    if (this.formularioAsignar.invalid || this.formularioAsignar.getRawValue().documentoId === 0) {
      this.formularioAsignar.markAllAsTouched();
      return;
    }

    if (this.empleadosSeleccionados().size === 0) {
      this.error.set('Seleccioná al menos un empleado');
      return;
    }

    const datos = this.formularioAsignar.getRawValue();
    this.guardando.set(true);
    this.error.set(null);

    this.servicioDocumentos.asignar(Number(datos.documentoId), {
      empleadoIds: Array.from(this.empleadosSeleccionados()),
      fechaVencimiento: datos.fechaVencimiento || undefined
    }).subscribe({
      next: () => {
        this.guardando.set(false);
        this.formularioAsignar.reset({ documentoId: 0 });
        this.empleadosSeleccionados.set(new Set());
        this.mostrarFormularioAsignar.set(false);
        this.recargar();
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo asignar el documento');
      }
    });
  }

  abrirDocumentoCatalogo(documento: DocumentoResumen): void {
    if (!documento.urlArchivo) {
      return;
    }
    this.error.set(null);
    this.asignacionEnVista.set(null);
    this.evidenciaFirma.set(null);
    this.documentoEnVista.set(documento);
  }

  abrirDocumentoAsignacion(asignacion: AsignacionDocumentoResumen): void {
    if (!asignacion.urlArchivo) {
      return;
    }
    this.error.set(null);
    this.documentoEnVista.set(null);
    this.evidenciaFirma.set(null);
    this.asignacionEnVista.set(asignacion);
  }

  cerrarDocumento(): void {
    this.documentoEnVista.set(null);
    this.asignacionEnVista.set(null);
  }

  verEvidenciaFirma(asignacionId: number): void {
    this.error.set(null);
    this.documentoEnVista.set(null);
    this.asignacionEnVista.set(null);
    this.servicioDocumentos.obtenerEvidenciaFirma(asignacionId).subscribe({
      next: (evidencia) => this.evidenciaFirma.set(evidencia),
      error: (err) => this.error.set(err.error?.mensaje ?? 'No hay evidencia de firma disponible')
    });
  }

  cerrarEvidenciaFirma(): void {
    this.evidenciaFirma.set(null);
  }
}
