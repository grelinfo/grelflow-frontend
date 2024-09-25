import { CommonModule, JsonPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { SbbButtonModule } from '@sbb-esta/angular/button';
import { SbbFormFieldModule } from '@sbb-esta/angular/form-field';
import { SbbInputModule } from '@sbb-esta/angular/input';
import { SbbIcon } from '@sbb-esta/angular/icon';
import { FeatureStore } from '../shared/feature.store';
import { FeatureService } from '../shared/feature.service';
import { SbbTableModule } from '@sbb-esta/angular/table';
import { TrackingStatusComponent } from '../tracking-status/tracking-status.component';
import { WorkItemStatusPipe } from '../shared/feature.pipe';


@Component({
  selector: 'app-feature-list',
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    SbbFormFieldModule,
    SbbInputModule,
    SbbButtonModule,
    SbbIcon,
    SbbTableModule,
    JsonPipe,
    CommonModule,
    TrackingStatusComponent,
    WorkItemStatusPipe
],
  providers: [FeatureStore],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './feature-list.component.html',
  styleUrl: './feature-list.component.scss'
})
export class FeatureListComponent {
  readonly featureStore = inject(FeatureStore);
  private _formBuilder = inject(FormBuilder);
  private FeatureService = inject(FeatureService);

  displayedColumns: string[] = ['id', 'status',  'budgetTrackingStatus', 'budgetUsagePercentage', 'timeTrackingStatus', 'completionPercentage', 'recordTimestamp', 'actions',];

  formGroup = this._formBuilder.group({
    id: ['', [Validators.required, Validators.minLength(3)]],
  });

  dataSource = this.featureStore.entities;

  remove(id: string) {
    this.featureStore.remove(id);
  }
  refresh(id: string) {
    const featureTimeTracking = this.FeatureService.get(id);

    featureTimeTracking.subscribe((data) => {
      this.featureStore.set(data);
    });
  }

  refreshAll() {
    this.featureStore.entities().forEach((feature) => {
      const featureTimeTracking = this.FeatureService.get(feature.id);
      featureTimeTracking.subscribe((data) => {
        this.featureStore.set(data);
      });
    });
  }

  removeAll() {
    this.featureStore.entities().forEach((feature) => {
      this.featureStore.remove(feature.id);
    });
  }

  handleSubmit() {
    if (!this.formGroup.valid) {
      return;
    }
    const id = this.formGroup.get('id')?.value;
    if (!id) {
      return;
    }

    const featureTimeTracking = this.FeatureService.get(id);


    featureTimeTracking.subscribe((data) => {
      this.featureStore.add(data);
    });
    
  }
}