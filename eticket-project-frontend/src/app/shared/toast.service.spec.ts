import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { ToastService } from './toast.service';

describe('ToastService', () => {
  let toast: ToastService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    toast = TestBed.inject(ToastService);
  });

  it('starts with no message', () => {
    expect(toast.message()).toBeNull();
  });

  it('sets the message immediately when show() is called', () => {
    toast.show('Hello');
    expect(toast.message()).toBe('Hello');
  });

  it('clears the message after the default TTL of 2500ms', fakeAsync(() => {
    toast.show('Bye');
    tick(2499);
    expect(toast.message()).toBe('Bye');
    tick(1);
    expect(toast.message()).toBeNull();
  }));

  it('respects a custom TTL', fakeAsync(() => {
    toast.show('Quick', 500);
    tick(499);
    expect(toast.message()).toBe('Quick');
    tick(1);
    expect(toast.message()).toBeNull();
  }));

  it('cancels the previous timer when show() is called again before it fires', fakeAsync(() => {
    toast.show('First', 1000);
    tick(500);
    toast.show('Second', 1000);

    // 500ms after the second call — well before the first timer would have fired
    tick(600);
    expect(toast.message()).toBe('Second');

    // 1000ms after the second call — the original timer would have cleared by now
    tick(400);
    expect(toast.message()).toBeNull();
  }));
});
