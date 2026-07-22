import { EsqueletoCargaComponent } from '../../../compartido/componentes/esqueleto-carga/esqueleto-carga.component';
import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { EmpleadoResumen, ServicioEmpleados } from '../../../nucleo/servicios/servicio-empleados';
import { ServicioAutenticacion } from '../../../nucleo/servicios/servicio-autenticacion';
import { BotonComponent, InsigniaComponent, etiquetaEstado, varianteEstado } from '../../../ui';

@Component({
  selector: 'app-pagina-mi-perfil',
  standalone: true,
  imports: [DatePipe, EsqueletoCargaComponent, BotonComponent, InsigniaComponent],
  templateUrl: './pagina-mi-perfil.component.html'
})
export class PaginaMiPerfilComponent implements OnInit {
  private readonly servicioAutenticacion = inject(ServicioAutenticacion);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly empleado = signal<EmpleadoResumen | null>(null);

  constructor(private readonly servicioEmpleados: ServicioEmpleados) {}

  ngOnInit(): void {
    this.servicioEmpleados.obtenerPropio().subscribe({
      next: (datos) => {
        this.empleado.set(datos);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar tus datos');
        this.cargando.set(false);
      }
    });
  }

  cerrarSesion(): void {
    this.servicioAutenticacion.cerrarSesion();
  }

  etiqueta(estado: string): string {
    return etiquetaEstado(estado);
  }

  variante(estado: string) {
    return varianteEstado(estado);
  }
}
