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
    path: 'finanse/inwestycje/:investmentId',
    loadComponent: () => import('./pages/topics/finance-dashboard/deposit-details/deposit-details').then(component => component.DepositDetails)
  },
  {
    path: 'finanse/lokaty/:investmentId',
    loadComponent: () => import('./pages/topics/finance-dashboard/deposit-details/deposit-details').then(component => component.DepositDetails)
  },
  {
    path: 'finanse/obligacje/:investmentId',
    loadComponent: () => import('./pages/topics/finance-dashboard/securities-account-details/securities-account-details').then(component => component.SecuritiesAccountDetails)
  },
  {
    path: 'finanse/rachunki-maklerskie/:investmentId',
    loadComponent: () => import('./pages/topics/finance-dashboard/securities-account-details/securities-account-details').then(component => component.SecuritiesAccountDetails)
  },
  {
    path: '**',
    redirectTo: ''
  }
];
