import { UserRole } from './auth.models';

export function landingUrlFor(role: UserRole | undefined | null): string {
  return role === 'INSPECTOR' ? '/inspector' : '/browse';
}
