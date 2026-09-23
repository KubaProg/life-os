import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/main-page/main-page').then((component) => component.MainPage)
  },
  {
    path: 'finanse',
    loadComponent: () =>
      import('./pages/topics/finance-dashboard/finance-dashboard').then(
        (component) => component.FinanceDashboard
      )
  },
  {
    path: 'finanse/konta/:accountId',
    loadComponent: () =>
      import('./pages/topics/finance-dashboard/account-details/account-details').then(
        (component) => component.AccountDetails
      )
  },
  {
    path: '**',
    redirectTo: ''
  }
];
