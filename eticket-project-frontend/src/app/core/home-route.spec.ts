import { homeRouteFor } from './home-route';

describe('homeRouteFor', () => {
  it('sends INSPECTOR to the inspector page', () => {
    expect(homeRouteFor('INSPECTOR')).toBe('/inspector');
  });

  it('sends PASSENGER to the browse page', () => {
    expect(homeRouteFor('PASSENGER')).toBe('/browse');
  });

  it('defaults to /browse for unknown/missing roles', () => {
    expect(homeRouteFor(null)).toBe('/browse');
    expect(homeRouteFor(undefined)).toBe('/browse');
  });
});
