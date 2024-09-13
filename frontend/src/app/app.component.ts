import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SbbHeaderLeanModule } from '@sbb-esta/angular/header-lean';
import { SbbUsermenuModule } from '@sbb-esta/angular/usermenu';
import { SbbIconModule } from '@sbb-esta/angular/icon';
import { SbbSidebarModule, SbbIconSidebarContainer } from '@sbb-esta/angular/sidebar';
import { RouterModule } from '@angular/router';
import { AsyncPipe } from '@angular/common';
import { SbbButtonModule } from '@sbb-esta/angular/button';
import { environment } from '../environments/environment';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterModule,
    SbbButtonModule,
    SbbHeaderLeanModule,
    SbbIconModule,
    SbbSidebarModule,
    SbbUsermenuModule,
    AsyncPipe,
    RouterModule,
    RouterOutlet,
    SbbHeaderLeanModule,
    SbbUsermenuModule,
    SbbIconModule,
    SbbSidebarModule,
    SbbIconSidebarContainer,
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'grelflow';
  environmentLabel = environment.label;
}

