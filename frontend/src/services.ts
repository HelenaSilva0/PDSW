import { downloadFile, request } from './lib/api';
import type { ChatMessage, ChatSummary, Exam, HistoryAttachment, HistorySnapshot, Patient, PatientDashboard, RegisterResponse, Role } from './types/api';
export interface RegisterData { email: string; senha: string; telefone: string; role: Role; nome: string; dataNascimento?: string; genero?: string; observacoes?: string; crm?: string; especialidade?: string; aceiteTermos: boolean; cienciaPrivacidade: boolean; consentimentoDadosSaude?: boolean; compromissoConfidencialidade?: boolean }
export const authService = { register: (data: RegisterData) => request<RegisterResponse>({ url: '/auth/register', method: 'POST', data }) };
export const accountService = { deleteMe: () => request<void>({ url: '/auth/me', method: 'DELETE' }) };
export const patientService = {
  me: () => request<Patient>({ url: '/pacientes/me' }),
  updateMe: (data: Partial<Patient>) => request<Patient>({ url: '/pacientes/me', method: 'PUT', data }),
  dashboard: () => request<PatientDashboard[]>({ url: '/pacientes/dashboard' }),
  byId: (id: number) => request<Patient>({ url: `/pacientes/${id}` })
};
export const examService = {
  list: (patientId: number) => request<Exam[]>({ url: `/exames/paciente/${patientId}` }),
  upload: (patientId: number, descricao: string, file: File, onProgress?: (value: number) => void) => { const data = new FormData(); data.append('pacienteId', String(patientId)); data.append('descricao', descricao); data.append('arquivo', file); return request<Exam>({ url: '/exames', method: 'POST', data, onUploadProgress: e => onProgress?.(e.total ? Math.round(e.loaded * 100 / e.total) : 0) }); },
  download: (exam: Exam) => downloadFile(`/exames/${exam.id}`, exam.nomeOriginal)
};
export const historyService = {
  list: (patientId: number) => request<HistorySnapshot[]>({ url: `/historico-familiar/paciente/${patientId}` }),
  create: (patientId: number, textoHistorico: string) => request<HistorySnapshot>({ url: '/historico-familiar/snapshots', method: 'POST', data: { pacienteId: patientId, textoHistorico } }),
  attach: (historyId: number, files: File[]) => { const data = new FormData(); files.forEach(file => data.append('exames', file)); return request<HistoryAttachment[]>({ url: `/historico-familiar/snapshots/${historyId}/anexos`, method: 'POST', data }); },
  download: (attachment: HistoryAttachment) => downloadFile(`/historico-familiar/anexos/${attachment.id}`, attachment.nomeOriginal)
};
export const chatService = {
  list: () => request<ChatSummary[]>({ url: '/chats' }), start: (patientId: number) => request<number>({ url: '/chats/start', method: 'POST', data: { pacienteId: patientId } }),
  messages: (id: number) => request<ChatMessage[]>({ url: `/chats/${id}/messages` }), send: (id: number, texto: string) => request<ChatMessage>({ url: `/chats/${id}/messages`, method: 'POST', data: { texto } })
};
