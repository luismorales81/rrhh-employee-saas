import { Injectable, inject } from '@angular/core';
import { ToastController } from '@ionic/angular/standalone';
import { ServicioHapticos } from './servicio-hapticos';

@Injectable({ providedIn: 'root' })
export class ServicioMensajes {
  private readonly toastController = inject(ToastController);
  private readonly hapticos = inject(ServicioHapticos);

  async exito(mensaje: string): Promise<void> {
    this.hapticos.exito();
    const toast = await this.toastController.create({
      message: mensaje,
      duration: 2200,
      color: 'success',
      position: 'bottom',
      cssClass: 'toast-ligo toast-ligo-exito'
    });
    await toast.present();
  }

  async error(mensaje: string): Promise<void> {
    this.hapticos.error();
    const toast = await this.toastController.create({
      message: mensaje,
      duration: 3200,
      color: 'danger',
      position: 'bottom',
      cssClass: 'toast-ligo toast-ligo-error'
    });
    await toast.present();
  }

  async info(mensaje: string): Promise<void> {
    this.hapticos.seleccion();
    const toast = await this.toastController.create({
      message: mensaje,
      duration: 2200,
      color: 'primary',
      position: 'bottom',
      cssClass: 'toast-ligo'
    });
    await toast.present();
  }
}
