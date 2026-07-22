import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { EsqueletoCargaComponent } from '../../../compartido/componentes/esqueleto-carga/esqueleto-carga.component';
import { VisorDocumentoComponent } from '../../../compartido/componentes/visor-documento/visor-documento.component';
import {
  ReciboResumen,
  ServicioRecibos,
  TEXTO_LEGAL_FIRMA_RECIBO,
  tieneIntegridadVerificable,
  TipoConfirmacionRecibo
} from '../../../nucleo/servicios/servicio-recibos';

@Component({
  selector: 'app-pagina-mis-recibos',
  standalone: true,
  imports: [DatePipe, EsqueletoCargaComponent, VisorDocumentoComponent],
  templateUrl: './pagina-mis-recibos.component.html'
})
export class PaginaMisRecibosComponent implements OnInit {
  private readonly servicioRecibos = inject(ServicioRecibos);

  readonly textoLegal = TEXTO_LEGAL_FIRMA_RECIBO;
  readonly requiereContrasena = false;
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly recibos = signal<ReciboResumen[]>([]);
  readonly reciboSeleccionado = signal<ReciboResumen | null>(null);
  readonly versiones = signal<ReciboResumen[]>([]);
  readonly blobPdf = signal<Blob | null>(null);
  readonly cargandoPdf = signal(false);
  readonly procesando = signal(false);
  readonly mostrarDisconformidad = signal(false);
  readonly comentarioDisconformidad = signal('');
  readonly confirmacionLegal = signal(false);
  readonly contrasenaFirma = signal('');

  ngOnInit(): void {
    this.recargar();
  }

  recargar(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.servicioRecibos.listarPropios().subscribe({
      next: (lista) => {
        this.recibos.set(lista);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar tus recibos');
        this.cargando.set(false);
      }
    });
  }

  abrirRecibo(recibo: ReciboResumen): void {
    this.reciboSeleccionado.set(recibo);
    this.confirmacionLegal.set(false);
    this.contrasenaFirma.set('');
    this.mostrarDisconformidad.set(false);
    this.comentarioDisconformidad.set('');
    this.liberarPdf();
    this.versiones.set([]);

    if (recibo.estado === 'FIRMADO' || recibo.estado === 'DISCONFORMIDAD') {
      this.servicioRecibos.listarVersiones(recibo.id).subscribe({
        next: (lista) => this.versiones.set(lista)
      });
      this.cargarPdf(recibo.id);
      return;
    }

    if (!tieneIntegridadVerificable(recibo)) {
      this.error.set(
        'Este recibo no tiene verificación de integridad. Pedile a tu equipo que lo vuelva a publicar con el PDF.'
      );
      return;
    }

    this.servicioRecibos.visualizar(recibo.id).subscribe({
      next: (actualizado) => {
        this.reciboSeleccionado.set(actualizado);
        this.actualizarEnLista(actualizado);
        this.cargarPdf(actualizado.id);
      },
      error: (err) => this.error.set(err.error?.mensaje ?? 'No se pudo registrar la visualización')
    });
  }

  cerrarDetalle(): void {
    this.reciboSeleccionado.set(null);
    this.versiones.set([]);
    this.liberarPdf();
  }

  puedeConfirmar(estado: string): boolean {
    return estado === 'PENDIENTE' || estado === 'VISTO';
  }

  puedeDescargarComprobante(recibo: ReciboResumen): boolean {
    return recibo.estado === 'FIRMADO' || recibo.estado === 'DISCONFORMIDAD';
  }

  descargarComprobante(recibo: ReciboResumen): void {
    this.servicioRecibos.descargarComprobante(recibo.id).subscribe({
      next: (blob) => this.descargarBlob(blob, `comprobante-${recibo.periodo}.json`),
      error: () => this.error.set('No se pudo descargar el comprobante')
    });
  }

  confirmar(tipo: TipoConfirmacionRecibo): void {
    const recibo = this.reciboSeleccionado();
    if (!recibo) return;

    if (tipo === 'CONFIRMADO' && !this.confirmacionLegal()) {
      this.error.set('Debés aceptar el texto legal y confirmar que leíste el recibo.');
      return;
    }

    if (tipo === 'DISCONFORMIDAD' && this.comentarioDisconformidad().trim().length < 10) {
      this.error.set('Indicá el motivo de disconformidad (mínimo 10 caracteres).');
      return;
    }

    if (this.requiereContrasena && !this.contrasenaFirma().trim()) {
      this.error.set('Debés ingresar tu contraseña para firmar.');
      return;
    }

    this.procesando.set(true);
    this.error.set(null);

    this.servicioRecibos.confirmarRecepcion(recibo.id, {
      tipo,
      comentario: tipo === 'DISCONFORMIDAD' ? this.comentarioDisconformidad().trim() : null,
      contrasena: this.requiereContrasena ? this.contrasenaFirma() : null
    }).subscribe({
      next: (actualizado) => {
        this.procesando.set(false);
        this.actualizarEnLista(actualizado);
        this.reciboSeleccionado.set(actualizado);
        this.mostrarDisconformidad.set(false);
        this.servicioRecibos.listarVersiones(actualizado.id).subscribe({
          next: (lista) => this.versiones.set(lista)
        });
      },
      error: (err) => {
        this.procesando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo confirmar el recibo');
      }
    });
  }

  private cargarPdf(id: number): void {
    this.cargandoPdf.set(true);
    this.blobPdf.set(null);
    this.servicioRecibos.descargarPdf(id).subscribe({
      next: (blob) => {
        this.blobPdf.set(blob);
        this.cargandoPdf.set(false);
      },
      error: () => {
        this.cargandoPdf.set(false);
        this.error.set('No se pudo cargar el PDF del recibo');
      }
    });
  }

  private descargarBlob(blob: Blob, nombre: string): void {
    const url = URL.createObjectURL(blob);
    const enlace = document.createElement('a');
    enlace.href = url;
    enlace.download = nombre;
    enlace.click();
    URL.revokeObjectURL(url);
  }

  private actualizarEnLista(actualizado: ReciboResumen): void {
    this.recibos.update((lista) => lista.map((r) => (r.id === actualizado.id ? actualizado : r)));
  }

  private liberarPdf(): void {
    this.blobPdf.set(null);
    this.cargandoPdf.set(false);
  }
}
