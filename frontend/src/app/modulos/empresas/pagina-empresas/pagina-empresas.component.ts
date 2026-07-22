import { Component, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import {
  EmpresaResumen,
  ServicioEmpresas
} from '../../../nucleo/servicios/servicio-empresas';
import { ServicioContextoEmpresa } from '../../../nucleo/servicios/servicio-contexto-empresa';
import { ServicioAutenticacion } from '../../../nucleo/servicios/servicio-autenticacion';

@Component({
  selector: 'app-pagina-empresas',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './pagina-empresas.component.html',
  styleUrl: './pagina-empresas.component.scss'
})
export class PaginaEmpresasComponent implements OnInit {
  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly error = signal<string | null>(null);
  readonly empresas = signal<EmpresaResumen[]>([]);
  readonly mostrarFormulario = signal(false);
  readonly formularioCrear;

  constructor(
    fb: FormBuilder,
    private readonly servicioEmpresas: ServicioEmpresas,
    private readonly servicioContextoEmpresa: ServicioContextoEmpresa,
    private readonly servicioAutenticacion: ServicioAutenticacion,
    private readonly router: Router
  ) {
    this.formularioCrear = fb.nonNullable.group({
      nombre: ['', Validators.required],
      razonSocial: [''],
      cuit: [''],
      email: ['', Validators.email],
      telefono: ['']
    });
  }

  ngOnInit(): void {
    this.cargarEmpresas();
  }

  cargarEmpresas(): void {
    this.cargando.set(true);
    this.servicioEmpresas.listar().subscribe({
      next: (lista) => {
        this.empresas.set(lista);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar las empresas');
        this.cargando.set(false);
      }
    });
  }

  abrirFicha(empresa: EmpresaResumen): void {
    this.router.navigate(['/plataforma/empresas', empresa.id]);
  }

  gestionarUsuarios(empresa: EmpresaResumen, evento: Event): void {
    evento.stopPropagation();
    this.servicioContextoEmpresa.establecer(empresa.id, empresa.nombre);
    this.router.navigate(['/plataforma/empresas', empresa.id, 'usuarios']);
  }

  gestionarEmpleados(empresa: EmpresaResumen, evento: Event): void {
    evento.stopPropagation();
    this.servicioContextoEmpresa.activarModoSoporte(empresa.id, empresa.nombre);
    this.router.navigate(['/empleados']);
  }

  alternarFormulario(): void {
    this.mostrarFormulario.update((v) => !v);
  }

  crearEmpresa(): void {
    if (this.formularioCrear.invalid) {
      this.formularioCrear.markAllAsTouched();
      return;
    }

    this.guardando.set(true);
    this.error.set(null);

    this.servicioEmpresas.crear(this.formularioCrear.getRawValue()).subscribe({
      next: (empresa) => {
        this.guardando.set(false);
        this.formularioCrear.reset();
        this.mostrarFormulario.set(false);
        this.router.navigate(['/plataforma/empresas', empresa.id]);
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(err.error?.mensaje ?? 'No se pudo crear la empresa');
      }
    });
  }

  esVistaPlataforma(): boolean {
    return this.servicioAutenticacion.sesionActual()?.esSuperAdmin ?? false;
  }
}
