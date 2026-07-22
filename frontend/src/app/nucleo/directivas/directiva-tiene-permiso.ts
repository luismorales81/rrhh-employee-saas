import { Directive, Input, TemplateRef, ViewContainerRef, inject, OnChanges } from '@angular/core';
import { ServicioPermisos } from '../servicios/servicio-permisos';

@Directive({
  selector: '[tienePermiso]',
  standalone: true
})
export class DirectivaTienePermiso implements OnChanges {
  @Input({ required: true }) tienePermiso!: string;

  private readonly plantilla = inject(TemplateRef<unknown>);
  private readonly contenedor = inject(ViewContainerRef);
  private readonly servicioPermisos = inject(ServicioPermisos);

  ngOnChanges(): void {
    this.contenedor.clear();
    if (this.servicioPermisos.tienePermiso(this.tienePermiso)) {
      this.contenedor.createEmbeddedView(this.plantilla);
    }
  }
}
