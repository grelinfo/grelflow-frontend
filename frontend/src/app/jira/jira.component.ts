import { JsonPipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { SbbButtonModule } from '@sbb-esta/angular/button';
import { SbbFormFieldModule } from '@sbb-esta/angular/form-field';
import { SbbInputModule } from '@sbb-esta/angular/input';
import { SbbIcon } from '@sbb-esta/angular/icon';

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
  templateUrl: './jira.component.html',
  styleUrl: './jira.component.scss'
})
export class JiraComponent {
  private _formBuilder = inject(FormBuilder);

  formGroup = this._formBuilder.group({
    issueKey: ['', [Validators.required, Validators.minLength(3)]],
  });

  lastSubmission?: { issueKey?: string | null };

  handleSubmit() {
    if (!this.formGroup.valid) {
      return;
    }
    this.lastSubmission = this.formGroup.value;
  }
}
