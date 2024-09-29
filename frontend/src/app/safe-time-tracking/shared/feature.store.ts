import { patchState, signalStore, withMethods } from '@ngrx/signals';
import { Feature } from './feature.model';
import {
  addEntity,
  removeEntity,
  withEntities,
  setEntity,
  removeAllEntities,
} from '@ngrx/signals/entities';
import { withStorageSync } from '@angular-architects/ngrx-toolkit';
import { FeatureService } from './feature.service';
import { inject } from '@angular/core';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import {
  pipe,
  switchMap,
  tap,
  filter,
  mergeMap,
  exhaustMap,
  debounceTime,
  concatMap
} from 'rxjs';
import { tapResponse } from '@ngrx/operators';

export const FeatureStore = signalStore(
  withEntities<Feature>(),
  withMethods((store, featureService = inject(FeatureService)) => ({
    load: rxMethod<string>(
      pipe(
        // Skip if the feature is already in the store
        filter((id) => !!id && !store.entityMap()[id]),
        concatMap((id) =>
          featureService.fetch(id).pipe(
            tapResponse(
              (feature) => patchState(store, addEntity(feature)),
              (error) => console.error(error)
            )
          )
        )
      )
    ),
    refresh: rxMethod<string>(
      pipe(
        debounceTime(1000),
        // Skip if the feature is not in the store
        filter((id) => !!id && !!store.entityMap()[id]),
        concatMap((id) =>
          featureService.fetch(id).pipe(
            tapResponse(
              (feature) => patchState(store, setEntity(feature)),
              (error) => console.error(error)
            )
          )
        )
      )
    ),
    refreshAll: rxMethod<void>(
      pipe(
        debounceTime(1000),
        exhaustMap(() => store.entities()),
        mergeMap((feature) =>
          featureService.fetch(feature.id).pipe(
            tapResponse(
              (fetchedFeature) => patchState(store, setEntity(fetchedFeature)),
              (error) => console.error(error)
            )
          )
        )
      )
    ),
    remove: rxMethod<string>(
      // Remove the feature from the store
      tap((id) => patchState(store, removeEntity(id)))
    ),
    removeAll: rxMethod<void>(
      pipe(tap(() => patchState(store, removeAllEntities())))
    ),
  })),
  withStorageSync({
    key: 'safe-time-tracking-feature-store',
    autoSync: true,
    storage: () => localStorage,
  })
);
