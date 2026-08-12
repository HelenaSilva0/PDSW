import axios, { AxiosError, type AxiosRequestConfig } from 'axios';
import { clearSession, readSession } from './session';

export class ApiError extends Error {
  constructor(public readonly status: number, message: string) { super(message); this.name = 'ApiError'; }
}

const messages: Record<number, string> = { 400: 'Verifique os dados informados.', 401: 'Sua sessão expirou. Entre novamente.', 403: 'Você não tem permissão para esta ação.', 404: 'O conteúdo solicitado não foi encontrado.', 409: 'Já existe um cadastro com esses dados.' };
export const api = axios.create({ baseURL: import.meta.env.VITE_API_URL || '', timeout: 20000, headers: { Accept: 'application/json' } });

api.interceptors.request.use((config) => {
  const session = readSession();
  if (session?.token) config.headers.Authorization = `Bearer ${session.token}`;
  return config;
});

api.interceptors.response.use(response => response, (error: AxiosError) => {
  const status = error.response?.status ?? 0;
  const data = error.response?.data;
  const serverMessage = typeof data === 'string' ? data : (data && typeof data === 'object' && 'message' in data ? String(data.message) : '');
  if (status === 401) {
    clearSession();
    window.dispatchEvent(new CustomEvent('auth:expired'));
  }
  return Promise.reject(new ApiError(status, serverMessage.trim() || messages[status] || 'Não foi possível concluir a solicitação.'));
});

export async function request<T>(config: AxiosRequestConfig): Promise<T> {
  const response = await api.request<T>(config);
  return response.data;
}

export async function downloadFile(url: string, fallbackName: string) {
  const response = await api.get<Blob>(url, { responseType: 'blob' });
  const disposition = response.headers['content-disposition'] as string | undefined;
  const encoded = disposition?.match(/filename\*=UTF-8''([^;]+)/i)?.[1];
  const name = encoded ? decodeURIComponent(encoded) : fallbackName;
  const href = URL.createObjectURL(response.data);
  const anchor = document.createElement('a');
  anchor.href = href; anchor.download = name; document.body.append(anchor); anchor.click(); anchor.remove();
  URL.revokeObjectURL(href);
}
