import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { MakePurchaseRequest, PurchaseResponse } from './purchase.types';

@Injectable({ providedIn: 'root' })
export class PurchaseApi {
  private readonly http = inject(HttpClient);

  makePurchase(body: MakePurchaseRequest): Observable<PurchaseResponse> {
    return this.http.post<PurchaseResponse>(`${environment.apiUrl}/purchases`, body);
  }
}

/** LocalDateTime-shaped (`YYYY-MM-DDTHH:mm:ss`, no timezone) for Spring's `LocalDateTime` fields. */
export function nowLocal(): string {
  return new Date().toISOString().slice(0, 19);
}
