import { Component, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import {
  EmpresaResumen,
  ServicioEmpresas
} from '../../../nucleo/servicios/servicio-empresas';
import { ServicioContextoEmpresa } from '../../../nucleo/servicios/servicio-contexto-empresa';

@Component({
  selector: 'app-pagina-detalle-empresa',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './pagina-detalle-empresa.component.html',
  styleUrl: './pagina-detalle-empresa.component.scss'
})
export class PaginaDetalleEmpresaComponent implements OnInit {
  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly desactivando = signal(false);
  readonly error = signal<string | null>(null);
  readonly exito = signal<string | null>(null);
  readonly empresa = signal<EmpresaResumen | null>(null);
  readonly formularioEditar;
  private empresaId = 0;

  constructor(
    fb: FormBuilder,
    private readonly ruta: ActivatedRoute,
    private readonly router: Router,
    private readonly servicioEmpresas: ServicioEmpresas,
    private readonly servicioContextoEmpresa: ServicioContextoEmpresa
  ) {
    this.formularioEditar = fb.nonNullable.group({
      nombre: ['', Validators.required],
      razonSocial: [''],
      cuit: [''],
      email: ['', Validators.email],
      telefono: ['']
    });
  }

  ngOnInit(): void {
    const idParam = this.ruta.snapshot.paramMap.get('id');
    this.empresaId = Number(idParam);
    if (!Number.isFinite(this.empresaId) || this.empresaId <= 0) {
      this.error.set('Empresa no válida');
      this.cargando.set(false);
      return;
    }
    this.cargarEmpresa();
  }

  cargarEmpresa(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.servicioEmpresas.obtener(this.empresaId).subscribe({
      next: (empresa) => {
        this.empresa.set(empresa);
        this.formularioEditar.patchValue({
          nombre: empresa.nombre,
          razonSocial: empresa.razonSocial ?? '',
          cuit: empresa.cuit ?? '',
          email: empresa.email ?? '',
          telefono: empresa.telefono ?? ''
        });
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar la empresa');
        this.cargando.set(false);
      }
    });
  }

  guardarCambios(): void {
    if (this.formularioEditar.invalid) {
      this.formularioEditar.markAllAsTouched();
      return;
    }

    this.guardando.set(true);
    this.error.set(null);
    this.exito.set(null);

    this.servicioEmpresas.actualizar(this.empresaId, this.formularioEditar.getRawValue()).subscribe({
      next: (empresa) => {
        this.empresa.set(empresa);
        this.guardando.set(false);
        this.exito.set('Empresa actualizada correctamente');
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo actualizar la empresa');
      }
    });
  }

  gestionarUsuarios(): void {
    const actual = this.empresa();
    if (!actual) {
      return;
    }
    this.servicioContextoEmpresa.establecer(actual.id, actual.nombre);
    this.router.navigate(['/plataforma/empresas', actual.id, 'usuarios']);
  }

  gestionarEmpleados(): void {
    const actual = this.empresa();
    if (!actual) {
      return;
    }
    this.servicioContextoEmpresa.activarModoSoporte(actual.id, actual.nombre);
    this.router.navigate(['/empleados']);
  }

  desactivarEmpresa(): void {
    const actual = this.empresa();
    if (!actual) {
      return;
    }
    if (!confirm(`¿Desactivar la empresa "${actual.nombre}"? Dejará de aparecer en el listado.`)) {
      return;
    }

    this.desactivando.set(true);
    this.error.set(null);
    this.servicioEmpresas.desactivar(actual.id).subscribe({
      next: () => {
        this.desactivando.set(false);
        this.router.navigate(['/plataforma/empresas']);
      },
      error: (err) => {
        this.desactivando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo desactivar la empresa');
      }
    });
  }
}
