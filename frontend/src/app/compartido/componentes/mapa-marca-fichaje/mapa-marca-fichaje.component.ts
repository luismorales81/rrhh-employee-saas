import {
  AfterViewInit,
  Component,
  ElementRef,
  Input,
  OnChanges,
  OnDestroy,
  SimpleChanges,
  inject,
  signal,
  viewChild
} from '@angular/core';
import { EmpleadoResumen } from '../../../nucleo/servicios/servicio-empleados';
import { ServicioCargaGoogleMaps } from '../../../nucleo/servicios/servicio-carga-google-maps';
import {
  MarcaFichajeResumen,
  ServicioFichajes,
  ZonaFichajeResumen
} from '../../../nucleo/servicios/servicio-fichajes';

@Component({
  selector: 'app-mapa-marca-fichaje',
  standalone: true,
  template: `
    @if (mensajeResumen()) {
      <p class="resumen-ubicacion" [class.resumen-ok]="coincideConZona()" [class.resumen-alerta]="!coincideConZona()">
        {{ mensajeResumen() }}
      </p>
    }
    @if (errorCarga()) {
      <p class="mapa-error">{{ errorCarga() }}</p>
    }
    <div class="mapa-marca-contenedor" #contenedorMapa></div>
    <ul class="leyenda-mapa">
      <li><span class="muestra muestra-marca"></span> Ubicación de la fichada</li>
      <li><span class="muestra muestra-zona-ok"></span> Zona coincidente</li>
      <li><span class="muestra muestra-zona-esperada"></span> Zona habilitada para el empleado</li>
    </ul>
  `,
  styles: `
    .mapa-marca-contenedor {
      width: 100%;
      height: 300px;
      border-radius: var(--radio-md);
      border: 1px solid var(--color-borde);
      margin-top: 0.5rem;
    }

    .mapa-error {
      margin: 0.5rem 0 0;
      color: var(--color-peligro);
      font-size: 0.875rem;
    }

    .resumen-ubicacion {
      margin: 0;
      padding: 0.625rem 0.75rem;
      border-radius: var(--radio-md);
      font-size: 0.875rem;
      font-weight: 500;
    }

    .resumen-ok {
      background: #ecfdf5;
      color: #047857;
      border: 1px solid #a7f3d0;
    }

    .resumen-alerta {
      background: #fffbeb;
      color: #b45309;
      border: 1px solid #fde68a;
    }

    .leyenda-mapa {
      list-style: none;
      margin: 0.75rem 0 0;
      padding: 0;
      display: flex;
      flex-wrap: wrap;
      gap: 0.75rem 1.25rem;
      font-size: 0.75rem;
      color: var(--color-texto-secundario);
    }

    .leyenda-mapa li {
      display: flex;
      align-items: center;
      gap: 0.35rem;
    }

    .muestra {
      width: 12px;
      height: 12px;
      border-radius: 50%;
      flex-shrink: 0;
    }

    .muestra-marca {
      background: #2563eb;
      border: 2px solid #fff;
      box-shadow: 0 0 0 1px #2563eb;
    }

    .muestra-zona-ok {
      background: #2bb89a;
    }

    .muestra-zona-esperada {
      background: #9ca3af;
    }
  `
})
export class MapaMarcaFichajeComponent implements AfterViewInit, OnChanges, OnDestroy {
  @Input({ required: true }) marca!: MarcaFichajeResumen;
  @Input() empleado: EmpleadoResumen | null = null;
  @Input() restringirSoloZonasAsignadas = false;

  readonly contenedorMapa = viewChild<ElementRef<HTMLDivElement>>('contenedorMapa');

  private readonly servicioMaps = inject(ServicioCargaGoogleMaps);
  private readonly servicioFichajes = inject(ServicioFichajes);

  readonly errorCarga = signal('');
  readonly mensajeResumen = signal('');

  private mapa?: google.maps.Map;
  private limpiarMarcador?: () => void;
  private circuloPrecision?: google.maps.Circle;
  private poligonos: google.maps.Polygon[] = [];
  private zonasCargadas: ZonaFichajeResumen[] = [];
  private vistaLista = false;

  ngAfterViewInit(): void {
    this.vistaLista = true;
    setTimeout(() => this.renderizar(), 0);
  }

  ngOnChanges(cambios: SimpleChanges): void {
    if (cambios['marca'] && !cambios['marca'].firstChange && this.vistaLista) {
      setTimeout(() => this.renderizar(), 0);
    }
  }

  ngOnDestroy(): void {
    this.limpiarCapas();
  }

  coincideConZona(): boolean {
    return this.marca.estado === 'VALIDADA' && this.marca.zonaFichajeId != null;
  }

  private async renderizar(): Promise<void> {
    if (this.marca.latitud == null || this.marca.longitud == null) {
      this.errorCarga.set('Esta fichada no tiene coordenadas GPS registradas.');
      this.mensajeResumen.set('');
      return;
    }

    this.errorCarga.set('');
    try {
      await this.servicioMaps.cargar();
      await this.cargarZonas();
      await this.inicializarMapa();
      await this.dibujarEnMapa();
      this.actualizarResumen();
    } catch (error) {
      this.errorCarga.set(
        error instanceof Error
          ? error.message
          : 'No se pudo cargar el mapa. Verificá la clave de Google Maps.'
      );
    }
  }

  private async cargarZonas(): Promise<void> {
    this.zonasCargadas = await new Promise((resolve, reject) => {
      this.servicioFichajes.listarZonas().subscribe({
        next: (zonas) => resolve(zonas),
        error: () => reject(new Error('No se pudieron cargar las zonas de fichaje'))
      });
    });
  }

  private async inicializarMapa(): Promise<void> {
    const elemento = this.contenedorMapa()?.nativeElement;
    if (!elemento) {
      return;
    }

    const centro = { lat: this.marca.latitud!, lng: this.marca.longitud! };
    if (!this.mapa) {
      this.mapa = new google.maps.Map(elemento, this.servicioMaps.opcionesMapa(centro, 16));
    } else {
      this.mapa.setCenter(centro);
    }
  }

  private async dibujarEnMapa(): Promise<void> {
    if (!this.mapa || this.marca.latitud == null || this.marca.longitud == null) {
      return;
    }

    this.limpiarCapas();

    const posicion = { lat: this.marca.latitud, lng: this.marca.longitud };
    const zonasAplicables = this.obtenerZonasAplicables();
    const bounds = new google.maps.LatLngBounds();
    bounds.extend(posicion);

    for (const zona of zonasAplicables) {
      if (zona.coordenadas.length < 3) {
        continue;
      }
      const esCoincidente = this.marca.zonaFichajeId === zona.id;
      const paths = zona.coordenadas.map((c) => ({ lat: c[0], lng: c[1] }));
      paths.forEach((p) => bounds.extend(p));

      const poligono = new google.maps.Polygon({
        paths,
        strokeColor: esCoincidente ? '#2bb89a' : '#6b7280',
        strokeWeight: esCoincidente ? 3 : 2,
        strokeOpacity: 0.9,
        fillColor: esCoincidente ? '#2bb89a' : '#9ca3af',
        fillOpacity: esCoincidente ? 0.25 : 0.12,
        map: this.mapa
      });
      this.poligonos.push(poligono);
    }

    const marcador = await this.servicioMaps.colocarMarcadorPunto(this.mapa, posicion, {
      titulo: 'Ubicación de la fichada',
      color: this.coincideConZona() ? '#2bb89a' : '#2563eb'
    });
    this.limpiarMarcador = marcador.limpiar;

    if (this.marca.precisionMetros && this.marca.precisionMetros > 0) {
      this.circuloPrecision = new google.maps.Circle({
        map: this.mapa,
        center: posicion,
        radius: this.marca.precisionMetros,
        fillColor: '#2563eb',
        fillOpacity: 0.12,
        strokeColor: '#2563eb',
        strokeOpacity: 0.35,
        strokeWeight: 1
      });
      const delta = this.marca.precisionMetros / 111_320;
      bounds.extend({ lat: posicion.lat + delta, lng: posicion.lng + delta });
      bounds.extend({ lat: posicion.lat - delta, lng: posicion.lng - delta });
    }

    if (!bounds.isEmpty()) {
      this.mapa.fitBounds(bounds, 48);
      const listener = google.maps.event.addListenerOnce(this.mapa, 'bounds_changed', () => {
        const zoom = this.mapa?.getZoom();
        if (zoom != null && zoom > 18) {
          this.mapa?.setZoom(18);
        }
      });
      void listener;
    }
  }

  private actualizarResumen(): void {
    if (this.marca.estado === 'VALIDADA' && this.marca.zonaFichajeNombre) {
      this.mensajeResumen.set(`La fichada coincide con la zona «${this.marca.zonaFichajeNombre}».`);
      return;
    }

    const zonasAplicables = this.obtenerZonasAplicables();
    if (this.marca.latitud == null || this.marca.longitud == null) {
      this.mensajeResumen.set('');
      return;
    }

    const zonaContenedora = zonasAplicables.find((zona) =>
      this.puntoDentroPoligono(this.marca.latitud!, this.marca.longitud!, zona.coordenadas)
    );

    if (zonaContenedora) {
      this.mensajeResumen.set(
        `El punto está dentro de «${zonaContenedora.nombre}», pero la marca quedó como ${this.etiquetaEstadoMarca()}.`
      );
      return;
    }

    if (zonasAplicables.length === 0) {
      this.mensajeResumen.set(
        'No hay zonas habilitadas para este empleado. La ubicación no pudo validarse contra ninguna geocerca.'
      );
      return;
    }

    const nombres = zonasAplicables.map((z) => z.nombre).join(', ');
    this.mensajeResumen.set(`La fichada está fuera de las zonas habilitadas (${nombres}).`);
  }

  private obtenerZonasAplicables(): ZonaFichajeResumen[] {
    const activas = this.zonasCargadas.filter((z) => z.activa);
    if (!this.empleado) {
      return activas.filter((z) => z.alcance === 'GLOBAL' || z.id === this.marca.zonaFichajeId);
    }

    const aplicables = activas.filter((z) => this.zonaAplicaAEmpleado(z, this.empleado!));
    if (this.marca.zonaFichajeId && !aplicables.some((z) => z.id === this.marca.zonaFichajeId)) {
      const coincidente = activas.find((z) => z.id === this.marca.zonaFichajeId);
      if (coincidente) {
        return [coincidente, ...aplicables];
      }
    }
    return aplicables;
  }

  private zonaAplicaAEmpleado(zona: ZonaFichajeResumen, empleado: EmpleadoResumen): boolean {
    if (zona.alcance === 'GLOBAL' && !this.restringirSoloZonasAsignadas) {
      return true;
    }
    return zona.asignaciones.some((asignacion) =>
      asignacion.empleadoId === empleado.id
      || (asignacion.sectorId != null && asignacion.sectorId === empleado.sectorId)
      || (asignacion.areaId != null && asignacion.areaId === empleado.areaId)
      || (asignacion.gerenciaId != null && asignacion.gerenciaId === empleado.gerenciaId)
    );
  }

  private puntoDentroPoligono(lat: number, lng: number, coordenadas: number[][]): boolean {
    let dentro = false;
    for (let i = 0, j = coordenadas.length - 1; i < coordenadas.length; j = i++) {
      const latI = coordenadas[i][0];
      const lngI = coordenadas[i][1];
      const latJ = coordenadas[j][0];
      const lngJ = coordenadas[j][1];
      const intersecta = ((latI > lat) !== (latJ > lat))
        && (lng < ((lngJ - lngI) * (lat - latI)) / (latJ - latI) + lngI);
      if (intersecta) {
        dentro = !dentro;
      }
    }
    return dentro;
  }

  private etiquetaEstadoMarca(): string {
    const mapa: Record<string, string> = {
      VALIDADA: 'validada',
      PENDIENTE_REVISION: 'pendiente de revisión',
      RECHAZADA: 'rechazada'
    };
    return mapa[this.marca.estado] ?? this.marca.estado.toLowerCase();
  }

  private limpiarCapas(): void {
    this.limpiarMarcador?.();
    this.limpiarMarcador = undefined;
    this.circuloPrecision?.setMap(null);
    this.circuloPrecision = undefined;
    this.poligonos.forEach((p) => p.setMap(null));
    this.poligonos = [];
  }
}
