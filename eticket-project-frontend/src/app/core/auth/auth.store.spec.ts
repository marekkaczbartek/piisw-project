import { TestBed } from '@angular/core/testing';
import { AuthStore } from './auth.store';
import { AuthUser } from './auth.models';

const SAMPLE_USER: AuthUser = {
  token: 'abc.def.ghi',
  id: '11111111-1111-1111-1111-111111111111',
  email: 'jane@example.com',
  role: 'PASSENGER',
  firstName: 'Jane',
  lastName: 'Doe',
};

describe('AuthStore', () => {
  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({});
  });

  it('starts logged out when localStorage is empty', () => {
    const store = TestBed.inject(AuthStore);

    expect(store.user()).toBeNull();
    expect(store.token()).toBeNull();
    expect(store.isLoggedIn()).toBeFalse();
  });

  it('rehydrates from localStorage on construction', () => {
    localStorage.setItem('auth', JSON.stringify(SAMPLE_USER));

    const store = TestBed.inject(AuthStore);

    expect(store.user()).toEqual(SAMPLE_USER);
    expect(store.token()).toBe('abc.def.ghi');
    expect(store.isLoggedIn()).toBeTrue();
  });

  it('persists session to localStorage when setSession is called', () => {
    const store = TestBed.inject(AuthStore);

    store.setSession(SAMPLE_USER);

    expect(store.isLoggedIn()).toBeTrue();
    expect(store.user()).toEqual(SAMPLE_USER);
    expect(JSON.parse(localStorage.getItem('auth')!)).toEqual(SAMPLE_USER);
  });

  it('clears both signal and localStorage on clear()', () => {
    const store = TestBed.inject(AuthStore);
    store.setSession(SAMPLE_USER);

    store.clear();

    expect(store.user()).toBeNull();
    expect(store.isLoggedIn()).toBeFalse();
    expect(localStorage.getItem('auth')).toBeNull();
  });

  it('ignores corrupted localStorage payload and wipes it', () => {
    localStorage.setItem('auth', '{not valid json');

    const store = TestBed.inject(AuthStore);

    expect(store.user()).toBeNull();
    expect(localStorage.getItem('auth')).toBeNull();
  });
});
