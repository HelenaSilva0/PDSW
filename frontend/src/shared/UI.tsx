import { forwardRef, type ButtonHTMLAttributes, type InputHTMLAttributes, type ReactNode, type SelectHTMLAttributes } from 'react';

export function Button({ className = '', loading, children, ...props }: ButtonHTMLAttributes<HTMLButtonElement> & { loading?: boolean }) {
  return <button className={`button ${className}`} disabled={props.disabled || loading} {...props}>{loading ? <><span className="spinner" aria-hidden="true" /> Enviando…</> : children}</button>;
}
export const Input = forwardRef<HTMLInputElement, InputHTMLAttributes<HTMLInputElement> & { label: string; error?: string }>(function Input({ label, error, id, ...props }, ref) {
  const inputId = id ?? props.name; return <label className="field" htmlFor={inputId}><span>{label}{props.required && <span aria-hidden="true"> *</span>}</span><input ref={ref} id={inputId} aria-invalid={Boolean(error)} aria-describedby={error ? `${inputId}-error` : undefined} {...props} />{error && <small id={`${inputId}-error`} className="field__error">{error}</small>}</label>;
});
export const Select = forwardRef<HTMLSelectElement, SelectHTMLAttributes<HTMLSelectElement> & { label: string; error?: string; children: ReactNode }>(function Select({ label, error, id, children, ...props }, ref) {
  const inputId = id ?? props.name; return <label className="field" htmlFor={inputId}><span>{label}</span><select ref={ref} id={inputId} aria-invalid={Boolean(error)} {...props}>{children}</select>{error && <small className="field__error">{error}</small>}</label>;
});
export function Card({ children, className = '' }: { children: ReactNode; className?: string }) { return <section className={`card ${className}`}>{children}</section>; }
export function Loading({ label = 'Carregando…' }: { label?: string }) { return <div className="state" role="status"><span className="spinner spinner--large" aria-hidden="true" />{label}</div>; }
export function SkeletonList() { return <div aria-label="Carregando" role="status">{[1, 2, 3].map(i => <div className="skeleton" key={i} />)}</div>; }
export function EmptyState({ title, children }: { title: string; children?: ReactNode }) { return <div className="state"><strong>{title}</strong>{children && <p>{children}</p>}</div>; }
export function ErrorState({ error, retry }: { error: Error; retry?: () => void }) { return <div className="state state--error" role="alert"><strong>Algo não saiu como esperado</strong><p>{error.message}</p>{retry && <Button type="button" onClick={retry}>Tentar novamente</Button>}</div>; }
