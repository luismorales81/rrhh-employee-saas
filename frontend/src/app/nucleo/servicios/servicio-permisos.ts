import { Injectable, inject } from '@angular/core';
import { ServicioAutenticacion } from './servicio-autenticacion';
import { ServicioContextoEmpresa } from './servicio-contexto-empresa';

export interface ItemMenu {
  ruta: string;
  etiqueta: string;
  icono: string;
  permiso?: string;
  permisos?: string[];
  seccion?: string;
}

export interface SeccionMenu {
  id: string;
  etiqueta: string;
  items: ItemMenu[];
}

const CLAVE_SECCIONES_COLAPSADAS = 'ligo.menu.seccionesColapsadas';

@Injectable({ providedIn: 'root' })
export class ServicioPermisos {
  private readonly servicioAutenticacion = inject(ServicioAutenticacion);
  private readonly servicioContextoEmpresa = inject(ServicioContextoEmpresa);

  esSuperAdmin(): boolean {
    const sesion = this.servicioAutenticacion.sesionActual();
    if (!sesion) {
      return false;
    }
    return sesion.esSuperAdmin || sesion.permisos.includes('empresa.crear');
  }

  esUsuarioPlataforma(): boolean {
    return this.esSuperAdmin();
  }

  tienePermiso(codigo: string): boolean {
    const sesion = this.servicioAutenticacion.sesionActual();
    if (!sesion) {
      return false;
    }
    if (this.esUsuarioPlataforma() && this.servicioContextoEmpresa.modoSoporte()) {
      return true;
    }
    return sesion.permisos.includes(codigo);
  }

  tieneAlgunPermiso(codigos: string[]): boolean {
    return codigos.some((c) => this.tienePermiso(c));
  }

  /** Empleado self-service sin permisos de gestión. */
  esPortalEmpleado(): boolean {
    const sesion = this.servicioAutenticacion.sesionActual();
    if (!sesion || this.esUsuarioPlataforma()) {
      return false;
    }
    if (
      this.tienePermiso('empresa.crear') ||
      this.tienePermiso('usuario.crear') ||
      this.tienePermiso('solicitud.aprobar')
    ) {
      return false;
    }
    return this.tieneAlgunPermiso([
      'solicitud.crear',
      'comunicacion.mis.ver',
      'documento.mis.ver',
      'recibo.mis.ver'
    ]);
  }

  /**
   * Supervisor: puede aprobar pero no es admin pleno (sin alta de usuarios/empleados).
   * Entra a “Mi equipo” en lugar del menú RRHH completo.
   */
  esPortalSupervisor(): boolean {
    if (this.esUsuarioPlataforma() || this.esPortalEmpleado()) {
      return false;
    }
    if (!this.tienePermiso('solicitud.aprobar')) {
      return false;
    }
    if (this.tienePermiso('usuario.crear') || this.tienePermiso('empleado.crear')) {
      return false;
    }
    return true;
  }

  esPortalRrhh(): boolean {
    const sesion = this.servicioAutenticacion.sesionActual();
    if (!sesion || this.esPortalEmpleado()) {
      return false;
    }
    if (this.esUsuarioPlataforma()) {
      return this.servicioContextoEmpresa.modoSoporte();
    }
    return true;
  }

  menuPlataforma(): ItemMenu[] {
    return [
      { ruta: '/plataforma/inicio', etiqueta: 'Inicio', icono: 'home' },
      { ruta: '/plataforma/empresas', etiqueta: 'Empresas', icono: 'building' }
    ];
  }

  /** Menú corto para supervisores. */
  menuSupervisor(): ItemMenu[] {
    return [
      { ruta: '/equipo', etiqueta: 'Mi equipo', icono: 'users', permiso: 'solicitud.aprobar' },
      { ruta: '/equipo/aprobaciones', etiqueta: 'Aprobaciones', icono: 'clipboard-list', permiso: 'solicitud.aprobar' },
      { ruta: '/vacaciones', etiqueta: 'Vacaciones', icono: 'tree-palm', permisos: ['vacacion.ver', 'vacacion.politica.ver'] },
      { ruta: '/solicitudes', etiqueta: 'Solicitudes', icono: 'clipboard-list', permiso: 'solicitud.ver' }
    ].filter((item) => this.itemVisible(item));
  }

  /** Lista plana (Command Palette / compatibilidad). */
  menuRrhhPlano(): ItemMenu[] {
    return this.menuRrhhSecciones().flatMap((s) => s.items);
  }

  /** @deprecated Preferir menuRrhhSecciones; se mantiene como lista plana filtrada. */
  menuRrhh(): ItemMenu[] {
    return this.menuRrhhPlano();
  }

  menuRrhhSecciones(): SeccionMenu[] {
    const secciones: SeccionMenu[] = [
      {
        id: 'inicio',
        etiqueta: 'Inicio',
        items: [
          { ruta: '/panel', etiqueta: 'Atención hoy', icono: 'layout-dashboard', permiso: 'empleado.ver', seccion: 'inicio' },
          { ruta: '/equipo', etiqueta: 'Mi equipo', icono: 'users-round', permiso: 'solicitud.aprobar', seccion: 'inicio' }
        ]
      },
      {
        id: 'personas',
        etiqueta: 'Personas',
        items: [
          { ruta: '/empleados', etiqueta: 'Equipo', icono: 'users', permiso: 'empleado.ver', seccion: 'personas' },
          { ruta: '/organizacion', etiqueta: 'Organización', icono: 'building-2', permiso: 'organizacion.gestionar', seccion: 'personas' },
          { ruta: '/activos', etiqueta: 'Activos', icono: 'laptop', permisos: ['activo.ver', 'activo.gestionar'], seccion: 'personas' }
        ]
      },
      {
        id: 'tiempo',
        etiqueta: 'Tiempo',
        items: [
          { ruta: '/solicitudes', etiqueta: 'Solicitudes', icono: 'clipboard-list', permiso: 'solicitud.ver', seccion: 'tiempo' },
          { ruta: '/vacaciones', etiqueta: 'Vacaciones', icono: 'tree-palm', permisos: ['vacacion.ver', 'vacacion.politica.ver'], seccion: 'tiempo' },
          { ruta: '/ausentismo', etiqueta: 'Ausencias', icono: 'heart-pulse', permiso: 'ausencia.ver', seccion: 'tiempo' },
          {
            ruta: '/certificados-medicos',
            etiqueta: 'Certificados',
            icono: 'stethoscope',
            permisos: ['certificado.ver_resumen', 'certificado.ver_completo', 'certificado.validar', 'certificado.configurar'],
            seccion: 'tiempo'
          },
          { ruta: '/fichajes', etiqueta: 'Asistencia', icono: 'clock', permisos: ['fichaje.ver', 'fichaje.zona.ver', 'fichaje.configurar'], seccion: 'tiempo' },
          { ruta: '/novedades', etiqueta: 'Liquidación', icono: 'trending-up', permisos: ['novedad.ver', 'novedad.gestionar'], seccion: 'tiempo' }
        ]
      },
      {
        id: 'documentos',
        etiqueta: 'Documentos',
        items: [
          { ruta: '/documentos', etiqueta: 'Archivos', icono: 'file-text', permisos: ['documento.ver', 'documento.gestionar'], seccion: 'documentos' },
          { ruta: '/recibos', etiqueta: 'Recibos', icono: 'receipt', permiso: 'recibo.ver', seccion: 'documentos' },
          { ruta: '/comunicaciones', etiqueta: 'Avisos', icono: 'megaphone', permiso: 'comunicacion.ver', seccion: 'documentos' }
        ]
      },
      {
        id: 'configuracion',
        etiqueta: 'Configuración',
        items: [
          { ruta: '/usuarios', etiqueta: 'Usuarios', icono: 'key-round', permiso: 'usuario.ver', seccion: 'configuracion' },
          { ruta: '/roles', etiqueta: 'Roles', icono: 'shield', permiso: 'rol.ver', seccion: 'configuracion' },
          { ruta: '/configuracion-workflow', etiqueta: 'Flujos', icono: 'workflow', permisos: ['workflow.configurar', 'workflow.ver'], seccion: 'configuracion' },
          { ruta: '/plantillas-checklists', etiqueta: 'Listas', icono: 'list-checks', permisos: ['onboarding.gestionar', 'offboarding.gestionar'], seccion: 'configuracion' },
          { ruta: '/certificados-medicos/configuracion', etiqueta: 'IA certificados', icono: 'stethoscope', permiso: 'certificado.configurar', seccion: 'configuracion' },
          { ruta: '/fichajes/configuracion', etiqueta: 'Ajustes fichaje', icono: 'clock', permiso: 'fichaje.configurar', seccion: 'configuracion' },
          { ruta: '/fichajes/zonas', etiqueta: 'Zonas', icono: 'map-pin', permiso: 'fichaje.zona.ver', seccion: 'configuracion' }
        ]
      }
    ];

    return secciones
      .map((seccion) => ({
        ...seccion,
        items: seccion.items.filter((item) => this.itemVisible(item))
      }))
      .filter((seccion) => seccion.items.length > 0);
  }

  /**
   * Bottom-nav empleado: máximo 5 ítems (Inicio, Pedidos, Documentos, Más, Perfil).
   * El resto vive en /mi-mas.
   */
  menuEmpleado(): ItemMenu[] {
    const tabs: ItemMenu[] = [
      { ruta: '/mi-panel', etiqueta: 'Inicio', icono: 'home' },
      { ruta: '/mi-pedidos', etiqueta: 'Pedidos', icono: 'clipboard-list' },
      { ruta: '/mi-archivos', etiqueta: 'Documentos', icono: 'file-text' },
      { ruta: '/mi-mas', etiqueta: 'Más', icono: 'layout-grid' },
      { ruta: '/mi-perfil', etiqueta: 'Perfil', icono: 'user', permiso: 'empleado.ver' }
    ];
    return tabs.filter((item) => this.itemVisible(item) || !item.permiso);
  }

  /** Todos los destinos del portal (palette + página Más). */
  menuEmpleadoCompleto(): ItemMenu[] {
    return [
      { ruta: '/mi-panel', etiqueta: 'Inicio', icono: 'home' },
      { ruta: '/mi-solicitudes', etiqueta: 'Solicitudes', icono: 'clipboard-list', permiso: 'solicitud.crear' },
      { ruta: '/mi-vacaciones', etiqueta: 'Vacaciones', icono: 'tree-palm', permiso: 'vacacion.solicitar' },
      { ruta: '/mi-documentos', etiqueta: 'Documentos', icono: 'file-text', permiso: 'documento.mis.ver' },
      { ruta: '/mis-recibos', etiqueta: 'Recibos', icono: 'receipt', permiso: 'recibo.mis.ver' },
      { ruta: '/mi-comunicaciones', etiqueta: 'Avisos', icono: 'megaphone', permiso: 'comunicacion.mis.ver' },
      { ruta: '/mi-notificaciones', etiqueta: 'Alertas', icono: 'bell', permiso: 'notificacion.ver' },
      { ruta: '/mi-perfil', etiqueta: 'Perfil', icono: 'user', permiso: 'empleado.ver' }
    ].filter((item) => this.itemVisible(item));
  }

  rutaInicio(): string {
    const sesion = this.servicioAutenticacion.sesionActual();
    if (!sesion) {
      return '/iniciar-sesion';
    }

    if (this.esUsuarioPlataforma()) {
      const empresaId = this.servicioContextoEmpresa.obtenerEmpresaIdActiva(sesion);
      if (this.servicioContextoEmpresa.modoSoporte() && empresaId !== null) {
        return '/panel';
      }
      return '/plataforma/inicio';
    }

    const empresaId = this.servicioContextoEmpresa.obtenerEmpresaIdActiva(sesion);
    if (!empresaId) {
      return '/iniciar-sesion';
    }

    if (this.esPortalEmpleado()) {
      return '/mi-panel';
    }

    if (this.esPortalSupervisor()) {
      return '/equipo';
    }

    return '/panel';
  }

  seccionesColapsadas(): Set<string> {
    try {
      const raw = localStorage.getItem(CLAVE_SECCIONES_COLAPSADAS);
      if (!raw) {
        return new Set();
      }
      return new Set(JSON.parse(raw) as string[]);
    } catch {
      return new Set();
    }
  }

  alternarSeccionColapsada(id: string): void {
    const actual = this.seccionesColapsadas();
    if (actual.has(id)) {
      actual.delete(id);
    } else {
      actual.add(id);
    }
    localStorage.setItem(CLAVE_SECCIONES_COLAPSADAS, JSON.stringify([...actual]));
  }

  private itemVisible(item: ItemMenu): boolean {
    if (item.permiso) {
      return this.tienePermiso(item.permiso);
    }
    if (item.permisos) {
      return this.tieneAlgunPermiso(item.permisos);
    }
    return true;
  }
}
