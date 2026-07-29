const dateFormat = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'medium' });
const dateTimeFormat = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' });
export const formatDate = (value?: string | null) => value ? dateFormat.format(new Date(`${value}T00:00:00`)) : 'Não informada';
export const formatDateTime = (value?: string | null) => value ? dateTimeFormat.format(new Date(value)) : 'Não informado';
export const formatBytes = (bytes?: number | null) => bytes == null ? '' : new Intl.NumberFormat('pt-BR', { style: 'unit', unit: bytes >= 1_000_000 ? 'megabyte' : 'kilobyte', maximumFractionDigits: 1 }).format(bytes / (bytes >= 1_000_000 ? 1_000_000 : 1_000));
export const genderLabel = (value: string) => ({ F: 'Feminino', M: 'Masculino', O: 'Outro', N: 'Não informado' }[value] ?? 'Não informado');
