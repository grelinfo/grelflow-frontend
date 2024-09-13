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
        path: 'jira',
        children: [
            {
                path: '',
                loadComponent: () => import('./jira/jira.component').then((m) => m.JiraComponent),
            },
        ],
    }
];