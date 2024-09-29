import { NgClass } from '@angular/common';
import { ChangeDetectionStrategy, Component, Input, ViewEncapsulation } from '@angular/core';
import { SbbIcon } from '@sbb-esta/angular/icon';

@Component({
  selector: 'tracking-status',
  standalone: true,
  imports: [NgClass, SbbIcon],
  templateUrl: './tracking-status.component.html',
  styleUrl: './tracking-status.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  encapsulation: ViewEncapsulation.None,
  host: {
    class: 'tracking-status',
  },
})
export class TrackingStatusComponent {
  /** Set the status. */
  @Input() type: 'ontrack' | 'overspent' | 'underspent' = 'ontrack';
  /** Enable or disable the message. */
  @Input() showMessage = true;

  /** @docs-private */
  get _iconClass() {
    return this.type ? { [`tracking-status-icon-${this.type}`]: true } : {};
  }
}