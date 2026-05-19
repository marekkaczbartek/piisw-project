import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ValidationApi } from './validation.api';
import { TicketValidationRequest, TicketValidationResponse } from './validation.types';
import { environment } from '../../environments/environment';

describe('ValidationApi', () => {
  let api: ValidationApi;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    api = TestBed.inject(ValidationApi);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('POSTs the body to /validations and forwards the response', () => {
    const body: TicketValidationRequest = {
      purchaseId: 'p-1',
      checkedIn: 'BUS-42',
    };
    const response: TicketValidationResponse = { valid: true };
    let received: TicketValidationResponse | undefined;

    api.validate(body).subscribe((r) => (received = r));

    const req = httpMock.expectOne(`${environment.apiUrl}/validations`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush(response);

    expect(received).toEqual(response);
  });

  it('propagates a `valid: false` verdict to the caller', () => {
    let received: TicketValidationResponse | undefined;

    api
      .validate({ purchaseId: 'p-2', checkedIn: 'BUS-7' })
      .subscribe((r) => (received = r));

    httpMock.expectOne(`${environment.apiUrl}/validations`).flush({ valid: false });

    expect(received).toEqual({ valid: false });
  });

  it('surfaces HTTP errors to the subscriber', () => {
    let status: number | undefined;

    api
      .validate({ purchaseId: 'p-3', checkedIn: 'BUS-1' })
      .subscribe({ next: () => fail('should error'), error: (e) => (status = e.status) });

    httpMock
      .expectOne(`${environment.apiUrl}/validations`)
      .flush({ message: 'not found' }, { status: 404, statusText: 'Not Found' });

    expect(status).toBe(404);
  });
});
