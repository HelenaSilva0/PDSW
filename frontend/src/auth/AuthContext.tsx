import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { request } from '../lib/api';
import { clearSession, readSession, saveSession, type Session } from '../lib/session';
import type { AuthResponse } from '../types/api';

interface AuthContextValue { session: Session | null; login(email: string, senha: string): Promise<Session>; logout(): void }
const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<Session | null>(() => readSession());
  const logout = useCallback(() => { clearSession(); setSession(null); }, []);
  useEffect(() => { window.addEventListener('auth:expired', logout); return () => window.removeEventListener('auth:expired', logout); }, [logout]);
  const login = useCallback(async (email: string, senha: string) => {
    const auth = await request<AuthResponse>({ url: '/auth/login', method: 'POST', data: { email, senha } });
    const next = saveSession(auth); setSession(next); return next;
  }, []);
  const value = useMemo(() => ({ session, login, logout }), [session, login, logout]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
export function useAuth() { const value = useContext(AuthContext); if (!value) throw new Error('useAuth requer AuthProvider'); return value; }
