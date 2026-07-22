import { EsqueletoCargaComponent } from '../../../compartido/componentes/esqueleto-carga/esqueleto-carga.component';
import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ServicioEmpresas } from '../../../nucleo/servicios/servicio-empresas';

@Component({
  selector: 'app-pagina-inicio-plataforma',
  standalone: true,
  imports: [RouterLink, EsqueletoCargaComponent],
  templateUrl: './pagina-inicio-plataforma.component.html',
  styleUrl: './pagina-inicio-plataforma.component.scss'
})
export class PaginaInicioPlataformaComponent implements OnInit {
  readonly cargando = signal(true);
  readonly totalEmpresas = signal(0);
  readonly empresasActivas = signal(0);

  constructor(private readonly servicioEmpresas: ServicioEmpresas) {}

  ngOnInit(): void {
    this.servicioEmpresas.listar().subscribe({
      next: (lista) => {
        this.totalEmpresas.set(lista.length);
        this.empresasActivas.set(lista.filter((e) => e.estado === 'ACTIVA').length);
        this.cargando.set(false);
      },
      error: () => this.cargando.set(false)
    });
  }
}
