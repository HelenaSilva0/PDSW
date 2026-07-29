import { describe, expect, it } from 'vitest';
import { validateFile } from './FileUpload';
describe('validateFile', () => {
  it('aceita PDF dentro do limite', () => expect(validateFile(new File(['pdf'], 'exame.pdf', { type: 'application/pdf' }))).toBeNull());
  it('rejeita extensão não permitida', () => expect(validateFile(new File(['x'], 'script.html', { type: 'text/html' }))).toMatch(/PDF/));
  it('rejeita arquivo maior que 10 MB', () => expect(validateFile(new File([new Uint8Array(10 * 1024 * 1024 + 1)], 'exame.pdf', { type: 'application/pdf' }))).toMatch(/10 MB/));
});
