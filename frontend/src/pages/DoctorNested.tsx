import { useQuery } from '@tanstack/react-query';
import { useEffect, useRef, useState } from 'react';
import { api } from '../lib/api';
import { formatBytes, formatDateTime } from '../lib/format';
import { chatService, examService, historyService } from '../services';
import { Modal } from '../shared/Modal';
import { Button, EmptyState, ErrorState, Loading } from '../shared/UI';
import { ChatPage } from './ChatPage';
import { HISTORY_QUESTIONS, parseHistory } from './HistoryPage';
import type { Exam, HistorySnapshot } from '../types/api';

function HistoryVersion({ item, expanded, onToggle }: { item: HistorySnapshot; expanded: boolean; onToggle(): void }) {
  const answers = parseHistory(item.textoHistorico);
  return <article className={`clinical-item ${expanded ? 'is-expanded' : ''}`}>
    <button type="button" className="clinical-item__toggle" aria-expanded={expanded} onClick={onToggle}>
      <span><strong>Versão {item.versao}</strong><small>{formatDateTime(item.criadoEm)}</small></span><span className="clinical-item__chevron" aria-hidden="true">⌄</span>
    </button>
    {expanded && <div className="clinical-item__content">
      {answers.size ? <ol className="clinical-answers">{HISTORY_QUESTIONS.map((question, index) => <li key={question}><strong>{question}</strong><p>{answers.get(index + 1) || 'Não informado'}</p></li>)}</ol> : <p className="preserve-lines">{item.textoHistorico}</p>}
      {item.anexos.length > 0 && <div className="attachments"><h4>Anexos</h4>{item.anexos.map(attachment => <span className="clinical-attachment" key={attachment.id}>{attachment.nomeOriginal} · {formatBytes(attachment.tamanho)}</span>)}</div>}
    </div>}
  </article>;
}

function ExamPreview({ exam, onClose }: { exam: Exam | null; onClose(): void }) {
  const [url, setUrl] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const objectUrl = useRef<string | null>(null);
  useEffect(() => {
    if (!exam) return;
    let active = true;
    setLoading(true); setError(null); setUrl(null);
    void api.get<Blob>(`/exames/${exam.id}/visualizar`, { responseType: 'blob' }).then(response => {
      const nextUrl = URL.createObjectURL(response.data);
      objectUrl.current = nextUrl;
      if (active) setUrl(nextUrl); else URL.revokeObjectURL(nextUrl);
    }).catch(() => { if (active) setError('Não foi possível carregar a pré-visualização.'); }).finally(() => { if (active) setLoading(false); });
    return () => { active = false; if (objectUrl.current) { URL.revokeObjectURL(objectUrl.current); objectUrl.current = null; } };
  }, [exam]);
  return <Modal open={Boolean(exam)} title={exam?.descricao || 'Exame'} className="exam-modal" onClose={onClose}>
    {exam && <div className="exam-modal__layout"><div className="exam-modal__meta"><span className="badge">{exam.contentType?.split('/')[1]?.toUpperCase() || 'ARQUIVO'}</span><p>{exam.nomeOriginal} · versão {exam.versao} · {formatBytes(exam.tamanho)}</p><span className="exam-modal__notice">Visualização protegida para acompanhamento clínico.</span></div><div className="exam-modal__preview">{loading && <Loading label="Carregando exame…" />}{error && <ErrorState error={new Error(error)} />}{url && exam.contentType.startsWith('image/') && <img src={url} alt={exam.descricao} />}{url && exam.contentType === 'application/pdf' && <iframe title={exam.descricao} src={url} />}{url && !exam.contentType.startsWith('image/') && exam.contentType !== 'application/pdf' && <p className="state">Este tipo de arquivo não possui pré-visualização.</p>}</div></div>}
  </Modal>;
}

function HistoryColumn({ patientId }: { patientId: number }) {
  const query = useQuery({ queryKey: ['history', patientId], queryFn: () => historyService.list(patientId) });
  const [expanded, setExpanded] = useState<number | null>(null);
  return <section className="doctor-workspace__column doctor-workspace__column--history" aria-label="Histórico familiar">
    <header className="clinical-column__header"><span className="eyebrow">Linha do tempo</span><h2>Histórico familiar</h2><p>Versões preservadas do acompanhamento.</p></header>
    <div className="clinical-column__body">{query.isPending ? <Loading /> : query.error ? <ErrorState error={query.error} retry={() => void query.refetch()} /> : query.data?.length ? query.data.map(item => <HistoryVersion key={item.id} item={item} expanded={expanded === item.id} onToggle={() => setExpanded(current => current === item.id ? null : item.id)} />) : <EmptyState title="Nenhuma versão registrada">O histórico aparecerá aqui após o primeiro envio.</EmptyState>}</div>
  </section>;
}

function ExamsColumn({ patientId }: { patientId: number }) {
  const query = useQuery({ queryKey: ['exams', patientId], queryFn: () => examService.list(patientId) });
  const [selected, setSelected] = useState<Exam | null>(null);
  return <section className="doctor-workspace__column doctor-workspace__column--exams" aria-label="Exames">
    <header className="clinical-column__header"><span className="eyebrow">Documentos clínicos</span><h2>Exames</h2><p>Selecione um item para abrir em tela inteira.</p></header>
    <div className="clinical-column__body">{query.isPending ? <Loading /> : query.error ? <ErrorState error={query.error} retry={() => void query.refetch()} /> : query.data?.length ? query.data.map(exam => <article className="clinical-item" key={exam.id}><button type="button" className="clinical-item__toggle" onClick={() => setSelected(exam)}><span><strong>{exam.descricao}</strong><small>Versão {exam.versao} · {formatDateTime(exam.enviadoEm)}</small></span><span className="clinical-item__open" aria-hidden="true">↗</span></button><div className="clinical-item__summary"><span>{exam.nomeOriginal}</span><span className="badge">{exam.contentType?.split('/')[1]?.toUpperCase() || 'ARQUIVO'}</span></div></article>) : <EmptyState title="Nenhum exame enviado">Os exames adicionados aparecerão aqui.</EmptyState>}</div>
    <ExamPreview exam={selected} onClose={() => setSelected(null)} />
  </section>;
}

export function DoctorClinicalWorkspace({ patientId }: { patientId: number }) {
  const chat = useQuery({ queryKey: ['patient-chat', patientId], queryFn: () => chatService.start(patientId), staleTime: Infinity });
  return <div className="doctor-workspace"><HistoryColumn patientId={patientId} /><section className="doctor-workspace__column doctor-workspace__column--chat" aria-label="Chat com a paciente">{chat.isPending ? <Loading label="Preparando conversa…" /> : chat.error ? <ErrorState error={chat.error} retry={() => void chat.refetch()} /> : <ChatPage lockedChatId={chat.data} compact />}</section><ExamsColumn patientId={patientId} /></div>;
}
