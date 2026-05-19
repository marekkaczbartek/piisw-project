import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { roleGuard } from './role.guard';
import { AuthStore } from './auth.store';
import { AuthUser } from './auth.models';

const PASSENGER: AuthUser = {
  token: 't',
  id: 'id',
  email: 'a@b.c',
  role: 'PASSENGER',
  firstName: 'a',
  lastName: 'b',
};

const INSPECTOR: AuthUser = { ...PASSENGER, role: 'INSPECTOR' };

function run(required: 'PASSENGER' | 'INSPECTOR') {
  const route = {} as ActivatedRouteSnapshot;
  const state = { url: '/x' } as RouterStateSnapshot;
  return TestBed.runInInjectionContext(() => roleGuard(required)(route, state));
}

describe('roleGuard', () => {
  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({});
  });

  it('returns true when the user has the required role', () => {
    TestBed.inject(AuthStore).setSession(INSPECTOR);

    expect(run('INSPECTOR')).toBeTrue();
  });

  it('redirects a PASSENGER away from INSPECTOR-only routes to /browse', () => {
    TestBed.inject(AuthStore).setSession(PASSENGER);
    const router = TestBed.inject(Router);
    spyOn(router, 'createUrlTree').and.callThrough();

    const result = run('INSPECTOR') as UrlTree;

    expect(router.createUrlTree).toHaveBeenCalledWith(['/browse']);
    expect(result instanceof UrlTree).toBeTrue();
  });

  it('redirects an INSPECTOR away from PASSENGER-only routes to /inspector', () => {
    TestBed.inject(AuthStore).setSession(INSPECTOR);
    const router = TestBed.inject(Router);
    spyOn(router, 'createUrlTree').and.callThrough();

    run('PASSENGER');

    expect(router.createUrlTree).toHaveBeenCalledWith(['/inspector']);
  });

  it('redirects a logged-out user to /browse (the unknown-role default)', () => {
    const router = TestBed.inject(Router);
    spyOn(router, 'createUrlTree').and.callThrough();

    run('PASSENGER');

    expect(router.createUrlTree).toHaveBeenCalledWith(['/browse']);
  });
});
