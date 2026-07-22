import { Component, Input, OnChanges, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EventoCalendarioVacaciones } from '../../../nucleo/servicios/servicio-vacaciones';

interface DiaCalendario {
  fecha: Date | null;
  eventos: EventoCalendarioVacaciones[];
}

@Component({
  selector: 'app-calendario-vacaciones',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './calendario-vacaciones.component.html',
  styleUrl: './calendario-vacaciones.component.css'
})
export class CalendarioVacacionesComponent implements OnChanges {
  @Input({ required: true }) eventos: EventoCalendarioVacaciones[] = [];
  @Input() mostrarEmpleado = true;
  readonly rangoMesCambiado = output<{ desde: string; hasta: string }>();

  readonly mesActual = signal(new Date());
  readonly diasMes = signal<DiaCalendario[]>([]);

  readonly nombresDias = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'];
  readonly nombresMeses = [
    'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
    'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
  ];

  ngOnChanges(): void {
    this.construirCalendario();
  }

  mesAnterior(): void {
    const actual = this.mesActual();
    this.mesActual.set(new Date(actual.getFullYear(), actual.getMonth() - 1, 1));
    this.construirCalendario();
    this.emitirRangoMes();
  }

  mesSiguiente(): void {
    const actual = this.mesActual();
    this.mesActual.set(new Date(actual.getFullYear(), actual.getMonth() + 1, 1));
    this.construirCalendario();
    this.emitirRangoMes();
  }

  tituloMes(): string {
    const fecha = this.mesActual();
    return `${this.nombresMeses[fecha.getMonth()]} ${fecha.getFullYear()}`;
  }

  claseEstado(estado: string): string {
    switch (estado) {
      case 'APROBADO': return 'evento-aprobado';
      case 'EN_REVISION':
      case 'BORRADOR': return 'evento-pendiente';
      case 'RECHAZADO':
      case 'CANCELADO': return 'evento-rechazado';
      default: return 'evento-pendiente';
    }
  }

  private construirCalendario(): void {
    const fecha = this.mesActual();
    const anio = fecha.getFullYear();
    const mes = fecha.getMonth();
    const primerDia = new Date(anio, mes, 1);
    const ultimoDia = new Date(anio, mes + 1, 0).getDate();
    const offset = (primerDia.getDay() + 6) % 7;

    const dias: DiaCalendario[] = [];
    for (let i = 0; i < offset; i++) {
      dias.push({ fecha: null, eventos: [] });
    }
    for (let dia = 1; dia <= ultimoDia; dia++) {
      const fechaDia = new Date(anio, mes, dia);
      const eventosDia = this.eventos.filter((e) => this.eventoEnDia(e, fechaDia));
      dias.push({ fecha: fechaDia, eventos: eventosDia });
    }
    this.diasMes.set(dias);
  }

  private eventoEnDia(evento: EventoCalendarioVacaciones, fecha: Date): boolean {
    const inicio = new Date(evento.fechaInicio + 'T00:00:00');
    const fin = new Date(evento.fechaFin + 'T00:00:00');
    const actual = new Date(fecha.getFullYear(), fecha.getMonth(), fecha.getDate());
    return actual >= inicio && actual <= fin;
  }

  private emitirRangoMes(): void {
    const fecha = this.mesActual();
    const desde = new Date(fecha.getFullYear(), fecha.getMonth(), 1);
    const hasta = new Date(fecha.getFullYear(), fecha.getMonth() + 1, 0);
    this.rangoMesCambiado.emit({
      desde: this.formatearFecha(desde),
      hasta: this.formatearFecha(hasta)
    });
  }

  private formatearFecha(fecha: Date): string {
    const anio = fecha.getFullYear();
    const mes = String(fecha.getMonth() + 1).padStart(2, '0');
    const dia = String(fecha.getDate()).padStart(2, '0');
    return `${anio}-${mes}-${dia}`;
  }
}
