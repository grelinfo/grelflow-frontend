import { Pipe, PipeTransform } from "@angular/core";
import { WorkItemStatus, TrackingStatus } from "./feature.model";

@Pipe({
  name: 'workItemStatus',
  standalone: true
})
export class WorkItemStatusPipe implements PipeTransform {
  transform(value: string): string {
    switch (value) {
      case WorkItemStatus.TODO:
        return 'To Do';
      case WorkItemStatus.INPROGRESS:
        return 'In Progress';
      case WorkItemStatus.DONE:
        return 'Done';
      default:
        console.error(`Unknown work item status: ${value}`);
        return 'Unknown';
    }
  }
}


@Pipe({
  name: 'trackingStatus',
  standalone: true
})
export class TrackingStatusPipe implements PipeTransform {
  transform(value: string): string {
    switch (value) {
      case TrackingStatus.ONTRACK:
        return 'On Track';
      case TrackingStatus.OVERSPENT:
        return 'Over Spent';
      case TrackingStatus.UNDERSPENT:
        return 'Under Spent';
      default:
        console.error(`Unknown tracking status: ${value}`);
        return 'Unknown';
    }
  }
}