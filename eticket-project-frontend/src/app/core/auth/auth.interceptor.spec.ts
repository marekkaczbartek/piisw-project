import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptors, HttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { authInterceptor } from './auth.interceptor';
import { AuthStore } from './auth.store';
import { environment } from '../../../environments/environment';
import { AuthUser } from './auth.models';

const SAMPLE_USER: AuthUser = {
  token: 'my-token',
  id: 'id',
  email: 'a@b.c',
  role: 'PASSENGER',
  firstName: 'a',
  lastName: 'b',
};

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let store: AuthStore;
  let router: Router;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    store = TestBed.inject(AuthStore);
    router = TestBed.inject(Router);
  });

  afterEach(() => httpMock.verify());

  it('attaches Authorization header when a token is present and URL matches apiUrl', () => {
    store.setSession(SAMPLE_USER);

    http.get(`${environment.apiUrl}/tickets`).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/tickets`);
    expect(req.request.headers.get('Authorization')).toBe('Bearer my-token');
    req.flush({});
  });

  it('does not attach Authorization header when no token', () => {
    http.get(`${environment.apiUrl}/tickets`).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/tickets`);
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });

  it('does not attach Authorization to URLs outside apiUrl', () => {
    store.setSession(SAMPLE_USER);
    const outside = 'https://third-party.example.com/data';

    http.get(outside).subscribe();

    const req = httpMock.expectOne(outside);
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });

  it('clears the session and redirects to /login on 401', () => {
    store.setSession(SAMPLE_USER);
    const navSpy = spyOn(router, 'navigateByUrl');

    http.get(`${environment.apiUrl}/tickets`).subscribe({
      next: () => fail('should have errored'),
      error: () => {},
    });

    httpMock.expectOne(`${environment.apiUrl}/tickets`).flush(
      { message: 'unauthorized' },
      { status: 401, statusText: 'Unauthorized' },
    );

    expect(store.isLoggedIn()).toBeFalse();
    expect(navSpy).toHaveBeenCalledWith('/login');
  });

  it('clears the session and redirects to /login on 403', () => {
    store.setSession(SAMPLE_USER);
    const navSpy = spyOn(router, 'navigateByUrl');

    http.get(`${environment.apiUrl}/tickets`).subscribe({
      next: () => fail('should have errored'),
      error: () => {},
    });

    httpMock.expectOne(`${environment.apiUrl}/tickets`).flush(
      { message: 'forbidden' },
      { status: 403, statusText: 'Forbidden' },
    );

    expect(store.isLoggedIn()).toBeFalse();
    expect(navSpy).toHaveBeenCalledWith('/login');
  });
});
