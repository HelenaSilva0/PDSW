import type { AuthResponse, Role } from '../types/api';

const KEY = 'viva-mama-session';
export interface Session { token: string; userId: number; role: Role }

export function readSession(): Session | null {
  try {
    const raw = sessionStorage.getItem(KEY);
    if (!raw) return null;
    const value = JSON.parse(raw) as Partial<Session>;
    return value.token && value.userId && (value.role === 'PACIENTE' || value.role === 'MEDICO') ? value as Session : null;
  } catch { return null; }
}
export function saveSession(auth: AuthResponse): Session {
  const session = { token: auth.token, userId: auth.userId, role: auth.role };
  sessionStorage.setItem(KEY, JSON.stringify(session));
  return session;
}
export function clearSession() { sessionStorage.removeItem(KEY); }
