import { UserRole } from './auth/auth.models';

export function homeRouteFor(role: UserRole | undefined | null): string {
  return role === 'INSPECTOR' ? '/inspector' : '/browse';
}
