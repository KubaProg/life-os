import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/main-page/main-page').then((component) => component.MainPage)
  },
  {
    path: '**',
    redirectTo: ''
  }
];
