import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthApi } from './auth.api';
import { AuthStore } from '../../core/auth/auth.store';
import { AuthResponse } from '../../core/auth/auth.models';
import { environment } from '../../../environments/environment';

const RESPONSE: AuthResponse = {
  token: 'tok',
  id: 'id',
  email: 'a@b.c',
  role: 'PASSENGER',
  firstName: 'a',
  lastName: 'b',
};

describe('AuthApi', () => {
  let api: AuthApi;
  let httpMock: HttpTestingController;
  let store: AuthStore;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    api = TestBed.inject(AuthApi);
    httpMock = TestBed.inject(HttpTestingController);
    store = TestBed.inject(AuthStore);
  });

  afterEach(() => httpMock.verify());

  it('POSTs to /login and stores the session on success', () => {
    let received: AuthResponse | undefined;

    api.login({ email: 'a@b.c', password: 'pw' }).subscribe((r) => (received = r));

    const req = httpMock.expectOne(`${environment.apiUrl}/login`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email: 'a@b.c', password: 'pw' });
    req.flush(RESPONSE);

    expect(received).toEqual(RESPONSE);
    expect(store.user()).toEqual(RESPONSE);
  });

  it('POSTs to /register and stores the session on success', () => {
    const body = { email: 'a@b.c', password: 'pw', firstName: 'a', lastName: 'b' };

    api.register(body).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/register`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush(RESPONSE);

    expect(store.isLoggedIn()).toBeTrue();
  });

  it('does not touch the store when login fails', () => {
    api.login({ email: 'a', password: 'b' }).subscribe({ error: () => {} });

    httpMock.expectOne(`${environment.apiUrl}/login`).flush(
      {},
      { status: 401, statusText: 'Unauthorized' },
    );

    expect(store.isLoggedIn()).toBeFalse();
  });
});
