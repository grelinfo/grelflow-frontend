import { JsonPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { SbbButtonModule } from '@sbb-esta/angular/button';
import { SbbFormFieldModule } from '@sbb-esta/angular/form-field';
import { SbbInputModule } from '@sbb-esta/angular/input';
import { SbbIcon } from '@sbb-esta/angular/icon';
import { FeaturesStore } from './features.store';

@Component({
  selector: 'app-jira',
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    SbbFormFieldModule,
    SbbInputModule,
    SbbButtonModule,
    SbbIcon,
    JsonPipe,
  ],
  providers: [FeaturesStore],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './jira.component.html',
  styleUrl: './jira.component.scss'
})
export class JiraComponent {
  readonly featureStore = inject(FeaturesStore);
  private _formBuilder = inject(FormBuilder);

  formGroup = this._formBuilder.group({
    issueKey: ['', [Validators.required, Validators.minLength(3)]],
  });


  handleSubmit() {
    if (!this.formGroup.valid) {
      return;
    }
    const issueKey = this.formGroup.get('issueKey')?.value;

    if (issueKey) {
      console.log('issueKey', issueKey);
      
      this.featureStore.addTimeTracking({
        issueKey: issueKey,
        timestamp: Date.now().toString(),
        originalEstimateSeconds: 0,
        computedEstimateSeconds: 0,
        computedRemainingEstimateSeconds: 0,
        computedTimeSpentSeconds: 0,
      });
    }
  }
}
