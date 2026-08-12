export type Role = 'PACIENTE' | 'MEDICO';
export type Gender = 'F' | 'M' | 'O' | 'N';

export interface AuthResponse { token: string; userId: number; role: Role }
export interface RegisterResponse { userId: number; role: Role; status: 'PENDENTE' | 'ATIVO' | 'BLOQUEADO' }
export interface Patient { idPaciente: number; nome: string; dataNascimento: string | null; historicoFamiliar?: string; observacoes: string; genero: Gender }
export interface PatientDashboard extends Patient { temChat: boolean; chatId: number | null; chatCriadoEm: string | null; ultimaMensagemEm: string | null; ultimaMensagemSender: Role | null; ultimaMensagemTexto: string | null; exameCount: number; ultimoExameEm: string | null; historicoCount: number; ultimoHistoricoEm: string | null }
export interface Exam { id: number; pacienteId: number; versao: number; descricao: string; nomeOriginal: string; contentType: string; tamanho: number; enviadoEm: string; enviadoPorUserId: number }
export interface HistoryAttachment { id: number; nomeOriginal: string; contentType: string; tamanho: number; enviadoEm: string }
export interface HistorySnapshot { id: number; pacienteId: number; versao: number; criadoEm: string; criadoPorUserId: number; textoHistorico: string; anexos: HistoryAttachment[] }
export interface ChatSummary { chatId: number; pacienteId: number | null; pacienteNome: string | null; medicoLabel: string | null; criadoEm: string; ultimaMensagemEm: string | null; ultimaMensagemSender: Role | null; ultimaMensagemTexto: string | null }
export interface ChatMessage { id: number; sender: Role; texto: string; enviadoEm: string }
