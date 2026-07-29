import { useState, type ChangeEvent } from 'react';
const ALLOWED = ['application/pdf', 'image/jpeg', 'image/png', 'image/webp'];
const MAX = 10 * 1024 * 1024;
export function validateFile(file: File): string | null {
  const extension = file.name.split('.').pop()?.toLowerCase();
  if (!ALLOWED.includes(file.type) || !extension || !['pdf', 'jpg', 'jpeg', 'png', 'webp'].includes(extension)) return 'Use PDF, JPG, PNG ou WEBP.';
  if (file.size > MAX) return 'O arquivo deve ter no máximo 10 MB.';
  return null;
}
export function FileUpload({ onChange, multiple = false }: { onChange(files: File[]): void; multiple?: boolean }) {
  const [error, setError] = useState(''); const [names, setNames] = useState<string[]>([]);
  const change = (event: ChangeEvent<HTMLInputElement>) => { const files = Array.from(event.target.files ?? []); const issue = files.map(validateFile).find(Boolean); if (issue) { setError(issue); event.target.value = ''; onChange([]); return; } setError(''); setNames(files.map(f => f.name)); onChange(files); };
  return <label className="file-upload"><span>Arquivo{multiple ? 's' : ''} (PDF ou imagem, até 10 MB)</span><input type="file" accept=".pdf,.jpg,.jpeg,.png,.webp,application/pdf,image/jpeg,image/png,image/webp" multiple={multiple} onChange={change} />{names.length > 0 && <small>{names.join(', ')}</small>}{error && <small className="field__error" role="alert">{error}</small>}</label>;
}
