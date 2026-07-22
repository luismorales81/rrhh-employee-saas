import { Component, AfterViewInit, ElementRef, OnDestroy, OnInit, computed, signal, viewChild } from '@angular/core';

import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { RouterLink } from '@angular/router';

import { forkJoin } from 'rxjs';

import { DirectivaTienePermiso } from '../../../nucleo/directivas/directiva-tiene-permiso';

import { BuscadorEmpleadoComponent } from '../../../compartido/componentes/buscador-empleado/buscador-empleado.component';
import {
  BuscadorOpcionComponent,
  OpcionBuscador
} from '../../../compartido/componentes/buscador-opcion/buscador-opcion.component';

import { ServicioCargaGoogleMaps } from '../../../nucleo/servicios/servicio-carga-google-maps';

import {

  AlcanceZona,

  ServicioFichajes,

  SolicitudAsignacionZona,

  ZonaFichajeResumen

} from '../../../nucleo/servicios/servicio-fichajes';

import { EmpleadoResumen, ServicioEmpleados } from '../../../nucleo/servicios/servicio-empleados';

import { ServicioOrganizacion } from '../../../nucleo/servicios/servicio-organizacion';



const TIPOS_DESTINO_ASIGNACION = [
  { valor: 'SECTOR', etiqueta: 'Sector (equipo completo)' },
  { valor: 'AREA', etiqueta: 'Área' },
  { valor: 'GERENCIA', etiqueta: 'Gerencia' },
  { valor: 'EMPLEADO', etiqueta: 'Empleado individual' }
] as const;

type TipoDestinoAsignacionZona = (typeof TIPOS_DESTINO_ASIGNACION)[number]['valor'];



const RADIO_ZONA_DEFAULT_METROS = 150;

const RADIO_TIERRA_METROS = 6378137;

/** Centro de respaldo si el navegador no concede geolocalización. */
const CENTRO_MAPA_RESPALDO = { lat: -34.6037, lng: -58.3816 };



@Component({

  selector: 'app-pagina-fichajes-zonas',

  standalone: true,

  imports: [
    ReactiveFormsModule,
    DirectivaTienePermiso,
    RouterLink,
    BuscadorEmpleadoComponent,
    BuscadorOpcionComponent
  ],

  templateUrl: './pagina-fichajes-zonas.component.html',

  styleUrl: './pagina-fichajes-zonas.component.scss'

})

export class PaginaFichajesZonasComponent implements OnInit, AfterViewInit, OnDestroy {

  readonly mapaRef = viewChild<ElementRef<HTMLDivElement>>('contenedorMapa');



  readonly cargando = signal(true);

  readonly guardando = signal(false);

  readonly obteniendoUbicacion = signal(false);

  readonly error = signal<string | null>(null);

  readonly zonas = signal<ZonaFichajeResumen[]>([]);

  readonly empleados = signal<EmpleadoResumen[]>([]);

  readonly mostrarFormulario = signal(false);

  readonly zonaEditando = signal<ZonaFichajeResumen | null>(null);

  readonly coordenadasDibujadas = signal<number[][]>([]);

  readonly zonaAsignacion = signal<ZonaFichajeResumen | null>(null);

  readonly modoDibujoManual = signal(false);

  readonly radioMetros = signal(RADIO_ZONA_DEFAULT_METROS);

  readonly tiposDestinoAsignacion = TIPOS_DESTINO_ASIGNACION;

  readonly opcionesAreas = signal<OpcionBuscador[]>([]);

  readonly opcionesSectores = signal<OpcionBuscador[]>([]);

  readonly opcionesGerencias = signal<OpcionBuscador[]>([]);

  readonly tipoDestinoAsignacion = signal<TipoDestinoAsignacionZona>('SECTOR');

  readonly empleadosParaAsignacion = computed(() => {
    const zona = this.zonaAsignacion();
    const idsAsignados = new Set(
      (zona?.asignaciones ?? [])
        .map((a) => a.empleadoId)
        .filter((id): id is number => id != null)
    );
    return this.empleados().filter((e) => !idsAsignados.has(e.id));
  });

  readonly opcionesSectoresDisponibles = computed(() =>
    this.filtrarOpcionesAsignadas(this.opcionesSectores(), 'sectorId')
  );

  readonly opcionesAreasDisponibles = computed(() =>
    this.filtrarOpcionesAsignadas(this.opcionesAreas(), 'areaId')
  );

  readonly opcionesGerenciasDisponibles = computed(() =>
    this.filtrarOpcionesAsignadas(this.opcionesGerencias(), 'gerenciaId')
  );



  readonly formularioZona;

  readonly formularioAsignacion;



  private mapa?: google.maps.Map;

  private poligonoTemporal?: google.maps.Polygon;

  private poligonosVista: google.maps.Polygon[] = [];

  private marcadorUbicacion?: { limpiar: () => void };

  private listenerClicMapa?: google.maps.MapsEventListener;

  private listenersPoligono: google.maps.MapsEventListener[] = [];



  constructor(

    fb: FormBuilder,

    private readonly servicioFichajes: ServicioFichajes,

    private readonly servicioEmpleados: ServicioEmpleados,

    private readonly servicioOrganizacion: ServicioOrganizacion,

    private readonly servicioMaps: ServicioCargaGoogleMaps

  ) {

    this.formularioZona = fb.nonNullable.group({

      nombre: ['', Validators.required],

      descripcion: [''],

      alcance: ['GLOBAL' as AlcanceZona, Validators.required],

      activa: [true]

    });

    this.formularioAsignacion = fb.nonNullable.group({

      tipoDestino: ['SECTOR' as TipoDestinoAsignacionZona, Validators.required],

      referenciaId: [null as number | null, Validators.required],

      tipo: ['INCLUYE' as const]

    });

    this.formularioAsignacion.get('tipoDestino')?.valueChanges.subscribe((tipo) => {
      this.tipoDestinoAsignacion.set(tipo as TipoDestinoAsignacionZona);
      this.formularioAsignacion.patchValue({ referenciaId: null });
    });

  }



  ngOnInit(): void {

    this.cargarZonas();

    this.servicioEmpleados.listar().subscribe({

      next: (lista) => this.empleados.set(lista),

      error: () => this.empleados.set([])

    });

    forkJoin({
      areas: this.servicioOrganizacion.listarAreas(),
      sectores: this.servicioOrganizacion.listarSectores(),
      gerencias: this.servicioOrganizacion.listarGerencias()
    }).subscribe({
      next: ({ areas, sectores, gerencias }) => {
        const mapaAreas = new Map(areas.map((a) => [a.id, a.nombre]));
        this.opcionesAreas.set(
          areas.map((a) => ({
            id: a.id,
            etiqueta: a.nombre,
            detalle: a.gerenciaNombre
          }))
        );
        this.opcionesSectores.set(
          sectores.map((s) => ({
            id: s.id,
            etiqueta: s.nombre,
            detalle: s.areaId ? (mapaAreas.get(s.areaId) ?? null) : null
          }))
        );
        this.opcionesGerencias.set(
          gerencias.map((g) => ({
            id: g.id,
            etiqueta: g.nombre,
            detalle: g.descripcion
          }))
        );
      }
    });

  }



  ngAfterViewInit(): void {

    setTimeout(() => this.inicializarMapa().then(() => this.renderizarZonasEnMapa()), 0);

  }



  ngOnDestroy(): void {

    this.desactivarDibujoManual();

    this.limpiarPoligonosVista();

    this.marcadorUbicacion?.limpiar();

  }



  alternarFormulario(zona?: ZonaFichajeResumen): void {

    if (zona) {

      this.zonaEditando.set(zona);

      this.coordenadasDibujadas.set(zona.coordenadas);

      this.modoDibujoManual.set(false);

      this.formularioZona.patchValue({

        nombre: zona.nombre,

        descripcion: zona.descripcion ?? '',

        alcance: zona.alcance,

        activa: zona.activa

      });

      this.mostrarFormulario.set(true);

      this.dibujarPoligonoEnMapa(zona.coordenadas);

    } else if (this.mostrarFormulario()) {

      this.cerrarFormulario();

    } else {

      this.zonaEditando.set(null);

      this.coordenadasDibujadas.set([]);

      this.modoDibujoManual.set(false);

      this.radioMetros.set(RADIO_ZONA_DEFAULT_METROS);

      this.formularioZona.reset({ alcance: 'GLOBAL', activa: true });

      this.mostrarFormulario.set(true);

      void this.usarMiUbicacion();

    }

  }



  cerrarFormulario(): void {

    this.mostrarFormulario.set(false);

    this.zonaEditando.set(null);

    this.coordenadasDibujadas.set([]);

    this.modoDibujoManual.set(false);

    this.desactivarDibujoManual();

    this.poligonoTemporal?.setMap(null);

    this.poligonoTemporal = undefined;

    this.marcadorUbicacion?.limpiar();

    this.marcadorUbicacion = undefined;

    this.renderizarZonasEnMapa();

  }



  usarMiUbicacion(): Promise<void> {

    if (!navigator.geolocation) {

      this.error.set('Tu navegador no soporta geolocalización');

      return Promise.resolve();

    }

    this.obteniendoUbicacion.set(true);

    this.error.set(null);

    this.modoDibujoManual.set(false);

    this.desactivarDibujoManual();



    return new Promise((resolve) => {

      navigator.geolocation.getCurrentPosition(

        (posicion) => {

          const lat = posicion.coords.latitude;

          const lng = posicion.coords.longitude;

          this.obteniendoUbicacion.set(false);

          void this.inicializarMapa().then(() => {

            if (!this.mapa) {

              resolve();

              return;

            }

            this.mapa.setCenter({ lat, lng });

            this.mapa.setZoom(17);

            this.mostrarMarcadorUbicacion(lat, lng);

            this.aplicarZonaCircular(lat, lng, this.radioMetros());

          });

          resolve();

        },

        (err) => {

          this.obteniendoUbicacion.set(false);

          const mensajes: Record<number, string> = {

            1: 'Permiso de ubicación denegado. Activá la ubicación en el navegador o dibujá el polígono manualmente.',

            2: 'No se pudo obtener la ubicación. Verificá la conexión o dibujá el polígono manualmente.',

            3: 'Tiempo de espera agotado al obtener la ubicación.'

          };

          this.error.set(mensajes[err.code] ?? 'No se pudo obtener la ubicación');

          resolve();

        },

        { enableHighAccuracy: true, timeout: 15000, maximumAge: 0 }

      );

    });

  }



  actualizarRadioZona(): void {

    const coords = this.coordenadasDibujadas();

    if (coords.length < 3 || this.modoDibujoManual()) {

      return;

    }

    const centro = this.calcularCentroPoligono(coords);

    this.aplicarZonaCircular(centro.lat, centro.lng, this.radioMetros());

  }



  activarDibujoManual(): void {

    this.modoDibujoManual.set(true);

    this.coordenadasDibujadas.set([]);

    this.poligonoTemporal?.setMap(null);

    this.poligonoTemporal = undefined;

    this.configurarListenerClicMapa();

  }



  limpiarPoligono(): void {

    this.coordenadasDibujadas.set([]);

    this.poligonoTemporal?.setMap(null);

    this.poligonoTemporal = undefined;

    if (this.modoDibujoManual()) {

      this.configurarListenerClicMapa();

    }

  }



  guardarZona(): void {

    if (this.formularioZona.invalid) {

      this.formularioZona.markAllAsTouched();

      return;

    }

    const coords = this.coordenadasDibujadas();

    if (coords.length < 3) {

      this.error.set('Definí una zona con al menos 3 vértices (usá tu ubicación o el dibujo manual)');

      return;

    }

    const datos = this.formularioZona.getRawValue();

    this.guardando.set(true);

    this.error.set(null);

    const editando = this.zonaEditando();

    const peticion = editando

      ? this.servicioFichajes.actualizarZona(editando.id, {

          nombre: datos.nombre,

          descripcion: datos.descripcion,

          coordenadas: coords,

          alcance: datos.alcance,

          activa: datos.activa

        })

      : this.servicioFichajes.crearZona({

          nombre: datos.nombre,

          descripcion: datos.descripcion,

          coordenadas: coords,

          alcance: datos.alcance,

          activa: datos.activa

        });



    peticion.subscribe({

      next: () => {

        this.guardando.set(false);

        this.cerrarFormulario();

        this.cargarZonas();

      },

      error: (err) => {

        this.guardando.set(false);

        this.error.set(err.error?.mensaje ?? 'Error al guardar la zona');

      }

    });

  }



  eliminarZona(zona: ZonaFichajeResumen): void {

    if (!confirm(`¿Eliminar la zona "${zona.nombre}"?`)) {

      return;

    }

    this.servicioFichajes.eliminarZona(zona.id).subscribe({

      next: () => this.cargarZonas(),

      error: (err) => this.error.set(err.error?.mensaje ?? 'Error al eliminar')

    });

  }



  abrirAsignacion(zona: ZonaFichajeResumen): void {

    this.zonaAsignacion.set(zona);

    this.tipoDestinoAsignacion.set('SECTOR');

    this.formularioAsignacion.reset({ tipoDestino: 'SECTOR', referenciaId: null, tipo: 'INCLUYE' });

  }



  cerrarAsignacion(): void {

    this.zonaAsignacion.set(null);

  }



  agregarAsignacion(): void {

    const zona = this.zonaAsignacion();

    if (!zona) {

      return;

    }

    if (this.formularioAsignacion.invalid) {

      this.formularioAsignacion.markAllAsTouched();

      this.error.set('Seleccioná un destinatario para la asignación');

      return;

    }

    const datos = this.formularioAsignacion.getRawValue();

    const referenciaId = datos.referenciaId;

    if (!referenciaId) {

      this.error.set('Seleccioná un destinatario para la asignación');

      return;

    }

    const solicitud: SolicitudAsignacionZona = { tipo: 'INCLUYE' };

    switch (datos.tipoDestino) {

      case 'EMPLEADO':
        solicitud.empleadoId = referenciaId;
        break;
      case 'SECTOR':
        solicitud.sectorId = referenciaId;
        break;
      case 'AREA':
        solicitud.areaId = referenciaId;
        break;
      case 'GERENCIA':
        solicitud.gerenciaId = referenciaId;
        break;
    }

    this.servicioFichajes.agregarAsignacion(zona.id, solicitud).subscribe({

      next: () => {

        this.cerrarAsignacion();

        this.cargarZonas();

      },

      error: (err) => this.error.set(err.error?.mensaje ?? 'Error al asignar')

    });

  }



  esAsignacionEmpleado(): boolean {

    return this.tipoDestinoAsignacion() === 'EMPLEADO';

  }



  esAsignacionSector(): boolean {

    return this.tipoDestinoAsignacion() === 'SECTOR';

  }



  esAsignacionArea(): boolean {

    return this.tipoDestinoAsignacion() === 'AREA';

  }



  esAsignacionGerencia(): boolean {

    return this.tipoDestinoAsignacion() === 'GERENCIA';

  }



  private filtrarOpcionesAsignadas(
    opciones: OpcionBuscador[],
    campo: 'sectorId' | 'areaId' | 'gerenciaId'
  ): OpcionBuscador[] {
    const zona = this.zonaAsignacion();
    if (!zona) {
      return opciones;
    }
    const idsAsignados = new Set(
      zona.asignaciones
        .map((a) => a[campo])
        .filter((id): id is number => id != null)
    );
    return opciones.filter((o) => !idsAsignados.has(o.id));
  }



  quitarAsignacion(zona: ZonaFichajeResumen, asignacionId: number): void {

    this.servicioFichajes.eliminarAsignacion(zona.id, asignacionId).subscribe({

      next: () => this.cargarZonas()

    });

  }



  private cargarZonas(): void {

    this.cargando.set(true);

    this.servicioFichajes.listarZonas().subscribe({

      next: async (lista) => {

        this.zonas.set(lista);

        this.cargando.set(false);

        await this.inicializarMapa();

        this.renderizarZonasEnMapa();

      },

      error: (err) => {

        this.cargando.set(false);

        this.error.set(err.error?.mensaje ?? 'Error al cargar zonas');

      }

    });

  }



  private obtenerUbicacionNavegador(): Promise<{ lat: number; lng: number } | null> {

    if (!navigator.geolocation) {

      return Promise.resolve(null);

    }

    return new Promise((resolve) => {

      navigator.geolocation.getCurrentPosition(

        (posicion) => {

          resolve({

            lat: posicion.coords.latitude,

            lng: posicion.coords.longitude

          });

        },

        () => resolve(null),

        { enableHighAccuracy: true, timeout: 10000, maximumAge: 60000 }

      );

    });

  }



  private async inicializarMapa(): Promise<void> {

    if (this.mapa) {

      return;

    }

    try {

      const [ubicacion] = await Promise.all([

        this.obtenerUbicacionNavegador(),

        this.servicioMaps.cargar()

      ]);

      const el = this.mapaRef()?.nativeElement;

      if (!el) {

        return;

      }

      const centro = ubicacion ?? CENTRO_MAPA_RESPALDO;

      const zoom = ubicacion ? 15 : 14;

      this.mapa = new google.maps.Map(el, this.servicioMaps.opcionesMapa(centro, zoom));

    } catch (e) {

      this.error.set(

        e instanceof Error ? e.message : 'Configurá la clave de Google Maps en claves-local.ts'

      );

    }

  }



  private aplicarZonaCircular(lat: number, lng: number, radioMetros: number): void {

    const coords = this.generarPoligonoCircular(lat, lng, radioMetros);

    this.coordenadasDibujadas.set(coords);

    this.dibujarPoligonoEnMapa(coords);

  }



  private generarPoligonoCircular(lat: number, lng: number, radioMetros: number, segmentos = 32): number[][] {

    const coords: number[][] = [];

    const latRad = (lat * Math.PI) / 180;

    for (let i = 0; i < segmentos; i++) {

      const angulo = (2 * Math.PI * i) / segmentos;

      const dx = radioMetros * Math.cos(angulo);

      const dy = radioMetros * Math.sin(angulo);

      const dLat = (dy / RADIO_TIERRA_METROS) * (180 / Math.PI);

      const dLng = (dx / (RADIO_TIERRA_METROS * Math.cos(latRad))) * (180 / Math.PI);

      coords.push([lat + dLat, lng + dLng]);

    }

    return coords;

  }



  private calcularCentroPoligono(coords: number[][]): { lat: number; lng: number } {

    const lat = coords.reduce((sum, c) => sum + c[0], 0) / coords.length;

    const lng = coords.reduce((sum, c) => sum + c[1], 0) / coords.length;

    return { lat, lng };

  }



  private async mostrarMarcadorUbicacion(lat: number, lng: number): Promise<void> {

    if (!this.mapa) {

      return;

    }

    this.marcadorUbicacion?.limpiar();

    this.marcadorUbicacion = await this.servicioMaps.colocarMarcadorPunto(
      this.mapa,
      { lat, lng },
      { titulo: 'Tu ubicación', color: '#2563eb' }
    );

  }



  private configurarListenerClicMapa(): void {

    if (!this.mapa) {

      return;

    }

    this.desactivarDibujoManual();

    this.listenerClicMapa = this.mapa.addListener('click', (evento: google.maps.MapMouseEvent) => {

      if (!this.mostrarFormulario() || !this.modoDibujoManual() || !evento.latLng) {

        return;

      }

      const coords = [...this.coordenadasDibujadas()];

      coords.push([evento.latLng.lat(), evento.latLng.lng()]);

      this.coordenadasDibujadas.set(coords);

      if (coords.length >= 3) {

        this.dibujarPoligonoEnMapa(coords);

      }

    });

  }



  private desactivarDibujoManual(): void {

    if (this.listenerClicMapa) {

      google.maps.event.removeListener(this.listenerClicMapa);

      this.listenerClicMapa = undefined;

    }

  }



  private dibujarPoligonoEnMapa(coordenadas: number[][]): void {

    if (!this.mapa || coordenadas.length < 3) {

      return;

    }

    this.limpiarListenersPoligono();

    this.poligonoTemporal?.setMap(null);

    const paths = coordenadas.map((c) => ({ lat: c[0], lng: c[1] }));

    this.poligonoTemporal = new google.maps.Polygon({

      paths,

      editable: true,

      draggable: false,

      fillColor: '#2bb89a',

      fillOpacity: 0.25,

      strokeColor: '#2bb89a',

      map: this.mapa

    });

    this.actualizarCoordenadasDesdePoligono(this.poligonoTemporal);

    const path = this.poligonoTemporal.getPath();

    this.listenersPoligono.push(

      google.maps.event.addListener(path, 'set_at', () => this.actualizarCoordenadasDesdePoligono(this.poligonoTemporal!)),

      google.maps.event.addListener(path, 'insert_at', () => this.actualizarCoordenadasDesdePoligono(this.poligonoTemporal!)),

      google.maps.event.addListener(path, 'remove_at', () => this.actualizarCoordenadasDesdePoligono(this.poligonoTemporal!))

    );

    const bounds = new google.maps.LatLngBounds();

    paths.forEach((p) => bounds.extend(p));

    this.mapa.fitBounds(bounds);

  }



  private limpiarListenersPoligono(): void {

    this.listenersPoligono.forEach((l) => google.maps.event.removeListener(l));

    this.listenersPoligono = [];

  }



  private actualizarCoordenadasDesdePoligono(poligono: google.maps.Polygon): void {

    const path = poligono.getPath();

    const coords: number[][] = [];

    for (let i = 0; i < path.getLength(); i++) {

      const p = path.getAt(i);

      coords.push([p.lat(), p.lng()]);

    }

    this.coordenadasDibujadas.set(coords);

  }



  private renderizarZonasEnMapa(): void {

    if (!this.mapa) {

      return;

    }

    this.limpiarPoligonosVista();

    for (const zona of this.zonas()) {

      if (!zona.activa || this.mostrarFormulario()) {

        continue;

      }

      const paths = zona.coordenadas.map((c) => ({ lat: c[0], lng: c[1] }));

      const poligono = new google.maps.Polygon({

        paths,

        strokeColor: zona.alcance === 'GLOBAL' ? '#2bb89a' : '#6b7280',

        fillOpacity: 0.15,

        map: this.mapa

      });

      this.poligonosVista.push(poligono);

    }

  }



  private limpiarPoligonosVista(): void {

    this.poligonosVista.forEach((p) => p.setMap(null));

    this.poligonosVista = [];

  }

}


