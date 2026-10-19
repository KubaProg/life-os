import {
  ApplicationConfig,
  provideBrowserGlobalErrorListeners,
  provideZonelessChangeDetection
} from '@angular/core';
import { provideHttpClient } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideRouter } from '@angular/router';
import Aura from '@primeuix/themes/aura';
import { providePrimeNG } from 'primeng/config';

import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZonelessChangeDetection(),
    provideHttpClient(),
    provideRouter(routes),
    provideAnimationsAsync(),

    providePrimeNG({
      ripple: true,
      translation: {
        monthNames: ['styczeń', 'luty', 'marzec', 'kwiecień', 'maj', 'czerwiec', 'lipiec', 'sierpień', 'wrzesień', 'październik', 'listopad', 'grudzień'],
        monthNamesShort: ['sty', 'lut', 'mar', 'kwi', 'maj', 'cze', 'lip', 'sie', 'wrz', 'paź', 'lis', 'gru'],
        chooseDate: 'Wybierz datę',
        chooseYear: 'Wybierz rok',
        prevYear: 'Poprzedni rok',
        nextYear: 'Następny rok',
        prevDecade: 'Poprzednia dekada',
        nextDecade: 'Następna dekada'
      },
      theme: {
        preset: Aura,
        options: {
          darkModeSelector: 'none'
        }
      }
    })
  ]
};
