import { Component, OnInit, signal, ViewChild } from '@angular/core';
import {
  AsignacionDocumentoResumen,
  EvidenciaFirmaResumen,
  ServicioDocumentos
} from '../../../nucleo/servicios/servicio-documentos';
import { EsqueletoCargaComponent } from '../../../compartido/componentes/esqueleto-carga/esqueleto-carga.component';
import { CanvasFirmaComponent } from '../../../compartido/componentes/canvas-firma/canvas-firma.component';
import { VisorDocumentoComponent } from '../../../compartido/componentes/visor-documento/visor-documento.component';

@Component({
  selector: 'app-pagina-mi-documentos',
  standalone: true,
  imports: [CanvasFirmaComponent, EsqueletoCargaComponent, VisorDocumentoComponent],
  templateUrl: './pagina-mi-documentos.component.html'
})
export class PaginaMiDocumentosComponent implements OnInit {
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly asignaciones = signal<AsignacionDocumentoResumen[]>([]);
  readonly asignacionFirmandoId = signal<number | null>(null);
  readonly evidenciaFirma = signal<EvidenciaFirmaResumen | null>(null);
  readonly documentoEnVista = signal<AsignacionDocumentoResumen | null>(null);

  @ViewChild(CanvasFirmaComponent) canvasFirma?: CanvasFirmaComponent;

  constructor(private readonly servicioDocumentos: ServicioDocumentos) {}

  ngOnInit(): void {
    this.recargar();
  }

  recargar(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.servicioDocumentos.listarMisAsignaciones().subscribe({
      next: (lista) => {
        this.asignaciones.set(lista);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar tus documentos');
        this.cargando.set(false);
      }
    });
  }

  abrirDocumento(asignacion: AsignacionDocumentoResumen): void {
    if (!asignacion.urlArchivo) {
      return;
    }
    this.error.set(null);
    this.documentoEnVista.set(asignacion);
  }

  cerrarDocumento(): void {
    this.documentoEnVista.set(null);
  }

  confirmarLectura(id: number): void {
    this.servicioDocumentos.confirmarLectura(id).subscribe({
      next: () => this.recargar(),
      error: (err) => this.error.set(err.error?.mensaje ?? 'No se pudo confirmar la lectura')
    });
  }

  iniciarFirma(id: number): void {
    this.asignacionFirmandoId.set(id);
  }

  cancelarFirma(): void {
    this.asignacionFirmandoId.set(null);
  }

  confirmarFirma(): void {
    const id = this.asignacionFirmandoId();
    if (!id || !this.canvasFirma?.emitirFirma()) {
      this.error.set('Dibujá tu firma antes de confirmar');
      return;
    }
  }

  enviarFirma(imagenBase64: string): void {
    const id = this.asignacionFirmandoId();
    if (!id) return;

    this.servicioDocumentos.firmar(id, {
      imagenFirmaBase64: imagenBase64,
      dispositivo: navigator.userAgent
    }).subscribe({
      next: () => {
        this.asignacionFirmandoId.set(null);
        this.recargar();
        if (id) {
          this.verEvidenciaFirma(id);
        }
      },
      error: (err) => this.error.set(err.error?.mensaje ?? 'No se pudo firmar el documento')
    });
  }

  puedeConfirmarLectura(asignacion: AsignacionDocumentoResumen): boolean {
    return asignacion.estado === 'PENDIENTE';
  }

  puedeFirmar(asignacion: AsignacionDocumentoResumen): boolean {
    return asignacion.requiereFirma
      && !asignacion.firmado
      && asignacion.estado !== 'FIRMADO'
      && asignacion.estado !== 'RECHAZADO';
  }

  verEvidenciaFirma(id: number): void {
    this.servicioDocumentos.obtenerEvidenciaFirma(id).subscribe({
      next: (evidencia) => this.evidenciaFirma.set(evidencia),
      error: (err) => this.error.set(err.error?.mensaje ?? 'No hay evidencia de firma')
    });
  }

  cerrarEvidenciaFirma(): void {
    this.evidenciaFirma.set(null);
  }
}
