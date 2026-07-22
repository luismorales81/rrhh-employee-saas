import { Component, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ServicioAutenticacion } from '../../../nucleo/servicios/servicio-autenticacion';
import { ServicioPermisos } from '../../../nucleo/servicios/servicio-permisos';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-pagina-login',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './pagina-login.component.html',
  styleUrl: './pagina-login.component.scss'
})
export class PaginaLoginComponent implements OnInit {
  readonly cargando = signal(false);
  readonly error = signal<string | null>(null);
  readonly esProduccion = environment.produccion;
  readonly formulario;

  constructor(
    fb: FormBuilder,
    private readonly servicioAutenticacion: ServicioAutenticacion,
    private readonly servicioPermisos: ServicioPermisos,
    private readonly router: Router,
    private readonly route: ActivatedRoute
  ) {
    this.formulario = fb.nonNullable.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    if (this.route.snapshot.queryParamMap.get('sesionExpirada') === '1') {
      this.error.set('Tu sesión expiró. Volvé a iniciar sesión.');
    }
  }

  enviar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.cargando.set(true);
    this.error.set(null);

    this.servicioAutenticacion.iniciarSesion(this.formulario.getRawValue()).subscribe({
      next: () => {
        this.cargando.set(false);
        this.router.navigate([this.servicioPermisos.rutaInicio()]);
      },
      error: (err) => {
        this.cargando.set(false);
        this.error.set(err.error?.mensaje ?? 'Credenciales inválidas');
      }
    });
  }
}
