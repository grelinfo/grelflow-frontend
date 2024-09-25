import { patchState, signalStore, withMethods } from '@ngrx/signals';
import { Feature } from './feature.model';
import { addEntity, removeEntity, withEntities, setEntity } from  '@ngrx/signals/entities';
import { withStorageSync } from '@angular-architects/ngrx-toolkit';

export const FeatureStore = signalStore(
    withEntities<Feature>(),
    withMethods((store) => ({
        add(feature: Feature): void {
            patchState(store, addEntity(feature));
        },
        set(feature: Feature): void {
            patchState(store, setEntity(feature))
        },
        remove(id: string): void {
            patchState(store, removeEntity(id));
        },
    })),
    withStorageSync({
        key: 'synced',
        autoSync: true,
        storage: () => localStorage,
    })
);

