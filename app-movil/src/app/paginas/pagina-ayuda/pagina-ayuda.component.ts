import { Component } from '@angular/core';
import {
  IonBackButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonTitle,
  IonToolbar
} from '@ionic/angular/standalone';
import { EncabezadoPaginaComponent } from '../../compartido/componentes/encabezado-pagina/encabezado-pagina.component';

@Component({
  selector: 'app-pagina-ayuda',
  standalone: true,
  imports: [
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButtons,
    IonBackButton,
    IonContent,
    EncabezadoPaginaComponent
  ],
  template: `
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button defaultHref="/tabs/perfil"></ion-back-button>
        </ion-buttons>
        <ion-title>Ayuda</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding pagina-fondo">
      <app-encabezado-pagina
        titulo="Ayuda"
        subtitulo="Respuestas rápidas para el día a día"
      />

      <section class="faq">
        <article>
          <h2>¿Cómo firmo un recibo?</h2>
          <p>Andá a Mis papeles → Recibos → Pendientes. Abrí el recibo, leé el PDF y tocá “Todo bien, confirmar”.</p>
        </article>
        <article>
          <h2>¿Cómo pido vacaciones?</h2>
          <p>En Mi tiempo → Vacaciones → Pedir vacaciones. Elegí fechas, revisá el resumen y enviá.</p>
        </article>
        <article>
          <h2>¿Cómo ficho?</h2>
          <p>En Mi tiempo → Fichar. La app pide ubicación solo en ese momento, no te rastrea todo el día.</p>
        </article>
        <article>
          <h2>¿Cómo subo un certificado?</h2>
          <p>En Mi tiempo → Ausencias → Certificados → Subir certificado. Podés usar cámara, galería o PDF.</p>
        </article>
        <article>
          <h2>¿Olvidé mi contraseña?</h2>
          <p>Pedile a tu equipo de RRHH que restablezca tu acceso. Desde el login también está la opción “¿Olvidaste tu contraseña?”.</p>
        </article>
      </section>

      <div class="contacto">
        <h2>¿Necesitás a alguien de tu empresa?</h2>
        <p>Escribile a tu equipo de RRHH o a tu líder. Ellos pueden ayudarte con legajo, accesos y aprobaciones.</p>
      </div>
    </ion-content>
  `,
  styles: `
    .faq {
      display: flex;
      flex-direction: column;
      gap: var(--espacio-3);
    }

    article, .contacto {
      background: var(--color-superficie);
      border: 1px solid var(--color-borde-suave);
      border-radius: var(--radio-lg);
      padding: var(--espacio-4);
      box-shadow: var(--sombra-sm);
    }

    h2 {
      margin: 0 0 0.35rem;
      font-size: 1rem;
      color: var(--color-texto);
    }

    p {
      margin: 0;
      color: var(--color-texto-secundario);
      font-size: 0.9rem;
      line-height: 1.45;
    }

    .contacto {
      margin-top: var(--espacio-5);
      margin-bottom: 2rem;
    }
  `
})
export class PaginaAyudaComponent {}
