import { describe, expect, it } from 'vitest';
import { parseHistory } from './HistoryPage';
describe('parseHistory', () => { it('interpreta as respostas versionadas', () => { const values = parseHistory('1) Minha mãe\n2) 48 anos'); expect(values.get(1)).toBe('Minha mãe'); expect(values.get(2)).toBe('48 anos'); }); });
