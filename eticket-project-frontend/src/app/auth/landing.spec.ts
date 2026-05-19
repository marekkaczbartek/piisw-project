import { landingUrlFor } from './landing';

describe('landingUrlFor', () => {
  it('sends INSPECTOR to the inspector page', () => {
    expect(landingUrlFor('INSPECTOR')).toBe('/inspector');
  });

  it('sends PASSENGER to the browse page', () => {
    expect(landingUrlFor('PASSENGER')).toBe('/browse');
  });

  it('defaults to /browse for unknown/missing roles', () => {
    expect(landingUrlFor(null)).toBe('/browse');
    expect(landingUrlFor(undefined)).toBe('/browse');
  });
});
