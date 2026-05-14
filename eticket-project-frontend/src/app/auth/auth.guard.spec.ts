import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { authGuard } from './auth.guard';
import { AuthStore } from './auth.store';
import { AuthUser } from './auth.models';

const SAMPLE_USER: AuthUser = {
  token: 't',
  id: 'id',
  email: 'a@b.c',
  role: 'PASSENGER',
  firstName: 'a',
  lastName: 'b',
};

function runGuard(url: string) {
  const state = { url } as RouterStateSnapshot;
  const route = {} as ActivatedRouteSnapshot;
  return TestBed.runInInjectionContext(() => authGuard(route, state));
}

describe('authGuard', () => {
  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({});
  });

  it('returns true when the user is logged in', () => {
    TestBed.inject(AuthStore).setSession(SAMPLE_USER);

    const result = runGuard('/tickets');

    expect(result).toBeTrue();
  });

  it('redirects to /login with returnUrl when logged out', () => {
    const router = TestBed.inject(Router);
    spyOn(router, 'createUrlTree').and.callThrough();

    const result = runGuard('/tickets') as UrlTree;

    expect(router.createUrlTree).toHaveBeenCalledWith(
      ['/login'],
      { queryParams: { returnUrl: '/tickets' } },
    );
    expect(result instanceof UrlTree).toBeTrue();
  });
});
