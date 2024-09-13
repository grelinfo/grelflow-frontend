import { patchState, signalStore, withComputed, withMethods, withState, type, } from '@ngrx/signals';
import { FeatureTimeTracking } from './feature-time-tracking.model';
import { addEntity, entityConfig, withEntities } from  '@ngrx/signals/entities';


const timeTrackingConfig = entityConfig({
    entity: type<FeatureTimeTracking>(),
    collection: 'timeTracking',
    selectId: (timeTracking: FeatureTimeTracking) => timeTracking.issueKey + timeTracking.timestamp,
});

export const FeaturesStore = signalStore(
    withEntities(timeTrackingConfig),
    withMethods((store) => ({
        addTimeTracking(timeTracking: FeatureTimeTracking): void {
            patchState(store, addEntity(timeTracking, timeTrackingConfig));
        },
    })),
);

