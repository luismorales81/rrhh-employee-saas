import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';
import { instalarParcheFocoAriaHidden } from './app/compartido/utilidades/parche-foco-aria-hidden';

instalarParcheFocoAriaHidden();

bootstrapApplication(AppComponent, appConfig).catch((error) => console.error(error));
