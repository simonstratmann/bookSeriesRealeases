import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./dashboard/dashboard').then(m => m.Dashboard)
  },
  {
    path: 'series',
    loadComponent: () => import('./series-list/series-list').then(m => m.SeriesList)
  }
];
