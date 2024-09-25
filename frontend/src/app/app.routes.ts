import { Routes } from '@angular/router';
export const routes: Routes = [
    {
        path: '',
        children: [
            {
                path: '',
                loadComponent: () => import('./home/home.component').then((m) => m.HomeComponent),
            },
        ],
    },
    {
        path: 'safe-time-tracking/features',
        children: [
            {
                path: '',
                loadComponent: () => import('./safe-time-tracking/feature-list/feature-list.component').then((m) => m.FeatureListComponent),
            },
        ],
    }
];