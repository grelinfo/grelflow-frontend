import { CommonModule, JsonPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { SbbButtonModule } from '@sbb-esta/angular/button';
import { SbbFormFieldModule } from '@sbb-esta/angular/form-field';
import { SbbInputModule } from '@sbb-esta/angular/input';
import { SbbIcon } from '@sbb-esta/angular/icon';
import { FeatureStore } from '../shared/feature.store';
import { SbbTableModule } from '@sbb-esta/angular/table';
import { TrackingStatusComponent } from '../shared/tracking-status/tracking-status.component';
import { WorkItemStatusPipe } from '../shared/feature.pipe';
import { OnInit } from '@angular/core';
import { WorkItemStatusComponent } from '../shared/work-item-status/work-item-status.component';

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
    WorkItemStatusPipe,
    WorkItemStatusComponent
],
  providers: [FeatureStore],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './feature-list.component.html',
  styleUrl: './feature-list.component.scss'
})
export class FeatureListComponent implements OnInit {
  readonly featureStore = inject(FeatureStore);
  private _formBuilder = inject(FormBuilder);

  formGroup = this._formBuilder.group({
    id: ['', [Validators.required, Validators.minLength(3)]],
  });

  dataSource = this.featureStore.entities;
  displayedColumns: string[] = ['id', 'name', 'status',  'budgetTrackingStatus', 'budgetUsagePercentage', 'timeTrackingStatus', 'completionPercentage', 'actions', 'recordedTimestamp'];


  remove(id: string):void {
    this.featureStore.remove(id);
  }
  refresh(id: string):void {
    this.featureStore.refresh(id);
  }
  refreshAll():void {
    this.featureStore.refreshAll();
  }
  removeAll():void {
    this.featureStore.removeAll();
  }

  handleSubmit() {
    if (!this.formGroup.valid) {
      return;
    }
    const id = this.formGroup.get('id')?.value;
    if (!id) {
      return;
    }

    this.featureStore.load(id);    
  }

  ngOnInit() {
    this.refreshAll();
  }
}