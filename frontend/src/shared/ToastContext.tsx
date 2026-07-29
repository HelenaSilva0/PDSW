import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react';
type Tone = 'success' | 'error' | 'info';
interface Toast { id: number; message: string; tone: Tone }
const Context = createContext<((message: string, tone?: Tone) => void) | null>(null);
export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([]);
  const notify = useCallback((message: string, tone: Tone = 'info') => { const id = Date.now(); setToasts(items => [...items, { id, message, tone }]); window.setTimeout(() => setToasts(items => items.filter(item => item.id !== id)), 4000); }, []);
  const value = useMemo(() => notify, [notify]);
  return <Context.Provider value={value}>{children}<div className="toast-region" aria-live="polite" aria-atomic="true">{toasts.map(t => <div className={`toast toast--${t.tone}`} role={t.tone === 'error' ? 'alert' : 'status'} key={t.id}>{t.message}</div>)}</div></Context.Provider>;
}
export function useToast() { const value = useContext(Context); if (!value) throw new Error('useToast requer ToastProvider'); return value; }
