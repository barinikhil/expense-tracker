import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

interface HealthResponse {
  status: string;
}

interface MessageResponse {
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class BackendService {
  private readonly apiBaseUrl = 'http://localhost:8081/api';

  constructor(private readonly http: HttpClient) {}

  getHealth(): Observable<HealthResponse> {
    return this.http.get<HealthResponse>(`${this.apiBaseUrl}/health`);
  }

  getMessage(): Observable<MessageResponse> {
    return this.http.get<MessageResponse>(`${this.apiBaseUrl}/message`);
  }
}
