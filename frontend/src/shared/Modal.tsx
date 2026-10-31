import { useEffect, useRef, type ReactNode } from 'react';
import { Button } from './UI';

export function Modal({ open, title, children, onClose, className = '' }: { open: boolean; title: string; children: ReactNode; onClose(): void; className?: string }) {
  const ref = useRef<HTMLDialogElement>(null);
  useEffect(() => { const dialog = ref.current; if (!dialog) return; if (open && !dialog.open) dialog.showModal(); if (!open && dialog.open) dialog.close(); }, [open]);
  useEffect(() => { const dialog = ref.current; const close = () => onClose(); dialog?.addEventListener('close', close); return () => dialog?.removeEventListener('close', close); }, [onClose]);
  return <dialog ref={ref} className={`modal ${className}`} aria-labelledby="modal-title"><div className="modal__header"><h2 id="modal-title">{title}</h2><Button type="button" className="button--ghost icon-button" aria-label="Fechar" onClick={onClose}>×</Button></div>{children}</dialog>;
}
