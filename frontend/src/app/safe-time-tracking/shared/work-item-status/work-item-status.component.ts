import { NgClass } from '@angular/common';
import { ChangeDetectionStrategy, Component, Input, ViewEncapsulation, computed, input } from '@angular/core';
import { SbbIcon } from '@sbb-esta/angular/icon';

@Component({
  selector: 'work-item-status',
  standalone: true,
  imports: [NgClass, SbbIcon],
  templateUrl: './work-item-status.component.html',
  styleUrl: './work-item-status.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  encapsulation: ViewEncapsulation.None,
  host: {
    class: 'work-item-status',
  },
})
export class WorkItemStatusComponent {

  type = input.required<'open' | 'inprogress' | 'done'>();
  showMessage = input<boolean>(true);

  svgIcon = computed(() => {
    switch (this.type()) {
      case 'inprogress':
        return 'punctuality-small';
      case 'done':
        return 'circle-tick-small';
      case 'open':
        return 'route-circle-start';
    }
  });

  message = computed(() => {
    if (!this.showMessage()) {
      return undefined;
    }
    switch (this.type()) {
      case 'done':
        return 'Done';
      case 'inprogress':
        return 'In progress';
      case 'open':
        return 'Open';
    }
  });

  iconClass = computed(() => `work-item-status-icon-${this.type()}`);
}