import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Feature } from './feature.model';

@Injectable({
  providedIn: 'root'
})
export class FeatureService {

  private baseUrl = 'http://localhost:8080/api/v1/featureTimeTracking';

  constructor(private http: HttpClient) {}

  fetch(id: string): Observable<Feature> {
    return this.http.get<Feature>(`${this.baseUrl}/${id}`);
  }
}


