import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { TicketValidationRequest, TicketValidationResponse } from './validation.types';

@Injectable({ providedIn: 'root' })
export class ValidationApi {
  private readonly http = inject(HttpClient);

  validate(body: TicketValidationRequest): Observable<TicketValidationResponse> {
    return this.http.post<TicketValidationResponse>(`${environment.apiUrl}/validations`, body);
  }
}
