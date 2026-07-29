import { useQuery } from '@tanstack/react-query';
import { useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import { chatService } from '../services';
import { Button, ErrorState, Loading } from '../shared/UI';
import { ChatPage } from './ChatPage';
import { ExamsPage } from './ExamsPage';
import { HistoryPage } from './HistoryPage';
export function DoctorHistory() { const { patientId } = useOutletContext<{ patientId: number }>(); return <HistoryPage patientId={patientId} readOnly />; }
export function DoctorExams() { const { patientId } = useOutletContext<{ patientId: number }>(); return <ExamsPage patientId={patientId} readOnly />; }
type ClinicalPanel = 'history' | 'exams' | null;
export function DoctorChat() {
  const { patientId } = useOutletContext<{ patientId: number }>();
  const [panel, setPanel] = useState<ClinicalPanel>(null);
  const chat = useQuery({ queryKey: ['patient-chat', patientId], queryFn: () => chatService.start(patientId), staleTime: Infinity });
  if (chat.isPending) return <Loading label="Preparando conversa…" />;
  if (chat.error) return <ErrorState error={chat.error} retry={() => void chat.refetch()} />;
  const toggle = (next: Exclude<ClinicalPanel, null>) => setPanel(current => current === next ? null : next);
  return <div className="doctor-chat-context">
    <div className="clinical-toolbar" aria-label="Informações clínicas do paciente">
      <div><strong>Informações do paciente</strong><span>Consulte somente quando necessário para a conversa.</span></div>
      <Button type="button" className={panel === 'history' ? '' : 'button--ghost'} aria-expanded={panel === 'history'} aria-controls="clinical-panel" onClick={() => toggle('history')}>Histórico familiar</Button>
      <Button type="button" className={panel === 'exams' ? '' : 'button--ghost'} aria-expanded={panel === 'exams'} aria-controls="clinical-panel" onClick={() => toggle('exams')}>Exames</Button>
    </div>
    {panel && <section id="clinical-panel" className="clinical-panel" aria-label={panel === 'history' ? 'Histórico familiar do paciente' : 'Exames do paciente'}>
      <div className="clinical-panel__top"><strong>{panel === 'history' ? 'Histórico familiar' : 'Exames'}</strong><Button type="button" className="button--ghost" onClick={() => setPanel(null)}>Ocultar</Button></div>
      {panel === 'history' ? <HistoryPage patientId={patientId} readOnly /> : <ExamsPage patientId={patientId} readOnly />}
    </section>}
    <ChatPage lockedChatId={chat.data} compact />
  </div>;
}
