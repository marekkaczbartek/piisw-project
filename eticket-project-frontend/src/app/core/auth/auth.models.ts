export type UserRole = 'PASSENGER' | 'INSPECTOR';

export interface AuthResponse {
  token: string;
  id: string;
  email: string;
  role: UserRole;
  firstName: string;
  lastName: string;
}

export type AuthUser = AuthResponse;

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}
