import { Component, effect, inject } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { RouterOutlet } from '@angular/router';
import { ServicioAutenticacion } from './nucleo/servicios/servicio-autenticacion';
import { ServicioContextoEmpresa } from './nucleo/servicios/servicio-contexto-empresa';
import { ServicioPermisos } from './nucleo/servicios/servicio-permisos';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: '<router-outlet />',
  styles: ':host { display: block; min-height: 100vh; }'
})
export class AppComponent {
  private readonly titulo = inject(Title);
  private readonly servicioAutenticacion = inject(ServicioAutenticacion);
  private readonly servicioContextoEmpresa = inject(ServicioContextoEmpresa);
  private readonly servicioPermisos = inject(ServicioPermisos);

  constructor() {
    effect(() => {
      const sesion = this.servicioAutenticacion.sesionActual();
      const nombreEmpresa = this.servicioContextoEmpresa.empresaSeleccionadaNombre();

      if (!sesion) {
        this.titulo.setTitle('Ligo');
        return;
      }

      if (nombreEmpresa) {
        this.titulo.setTitle(`${nombreEmpresa} · Ligo`);
        return;
      }

      if (this.servicioPermisos.esUsuarioPlataforma() && !this.servicioContextoEmpresa.modoSoporte()) {
        this.titulo.setTitle('Plataforma · Ligo');
        return;
      }

      this.titulo.setTitle('Ligo');
    });
  }
}
