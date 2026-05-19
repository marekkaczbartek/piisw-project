import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { PurchaseApi, nowLocal } from './purchase.api';
import { MakePurchaseRequest, PurchaseResponse } from './purchase.types';
import { environment } from '../../environments/environment';

const RESPONSE: PurchaseResponse = {
  id: 'p-1',
  ticketType: 'SINGLE_USE',
  discountType: 'NORMAL',
  price: 4.6,
  durationMinutes: null,
  boughtAt: '2026-05-19T12:00:00',
  expiresAt: null,
};

describe('PurchaseApi', () => {
  let api: PurchaseApi;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    api = TestBed.inject(PurchaseApi);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('POSTs the body to /purchases and forwards the response', () => {
    const body: MakePurchaseRequest = {
      ticketType: 'SINGLE_USE',
      discountType: 'NORMAL',
      durationMinutes: null,
      boughtAt: '2026-05-19T12:00:00',
    };
    let received: PurchaseResponse | undefined;

    api.makePurchase(body).subscribe((r) => (received = r));

    const req = httpMock.expectOne(`${environment.apiUrl}/purchases`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush(RESPONSE);

    expect(received).toEqual(RESPONSE);
  });

  it('surfaces HTTP errors to the subscriber', () => {
    let status: number | undefined;

    api
      .makePurchase({
        ticketType: 'PERIOD',
        discountType: 'REDUCED',
        durationMinutes: 30 * 1440,
        boughtAt: '2026-05-19T12:00:00',
      })
      .subscribe({ next: () => fail('should error'), error: (e) => (status = e.status) });

    httpMock
      .expectOne(`${environment.apiUrl}/purchases`)
      .flush({}, { status: 400, statusText: 'Bad Request' });

    expect(status).toBe(400);
  });
});

describe('nowLocal', () => {
  it('returns a LocalDateTime-shaped string (no timezone suffix)', () => {
    const out = nowLocal();
    expect(out).toMatch(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}$/);
  });

  it('matches the slice of the current ISO string', () => {
    jasmine.clock().install();
    try {
      jasmine.clock().mockDate(new Date('2026-05-19T12:34:56.789Z'));
      expect(nowLocal()).toBe('2026-05-19T12:34:56');
    } finally {
      jasmine.clock().uninstall();
    }
  });
});
