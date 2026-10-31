import { useQuery } from '@tanstack/react-query';
import { useMemo, useState } from 'react';
import { Link, Navigate, useParams } from 'react-router-dom';
import { formatDate, formatDateTime, genderLabel } from '../lib/format';
import { patientService } from '../services';
import { Card, EmptyState, ErrorState, Input, Loading, Select } from '../shared/UI';
import { DoctorClinicalWorkspace } from './DoctorNested';

const PATIENTS_PER_PAGE = 30;

function age(date?: string | null) {
  if (!date) return null;
  const birth = new Date(`${date}T00:00:00`);
  const now = new Date();
  let value = now.getFullYear() - birth.getFullYear();
  if (now < new Date(now.getFullYear(), birth.getMonth(), birth.getDate())) value--;
  return value;
}

export function DoctorDashboardPage() {
  const query = useQuery({ queryKey: ['patients', 'dashboard'], queryFn: patientService.dashboard });
  const [search, setSearch] = useState('');
  const [gender, setGender] = useState('');
  const [ages, setAges] = useState('');
  const [order, setOrder] = useState('name');
  const [page, setPage] = useState(1);
  const rows = useMemo(() => (query.data ?? [])
    .filter(p => order !== 'mine' || p.temChat)
    .filter(p => p.nome.toLocaleLowerCase('pt-BR').includes(search.toLocaleLowerCase('pt-BR')))
    .filter(p => !gender || p.genero === gender)
    .filter(p => {
      const value = age(p.dataNascimento);
      if (!ages) return true;
      if (value == null) return false;
      const [min, max] = ages.split('-').map(Number);
      return value >= (min ?? 0) && (!max || value <= max);
    })
    .sort((a, b) => order === 'recent'
      ? (Date.parse(b.ultimaMensagemEm ?? '') || 0) - (Date.parse(a.ultimaMensagemEm ?? '') || 0)
      : order === 'exams'
        ? b.exameCount - a.exameCount
      : a.nome.localeCompare(b.nome, 'pt-BR')), [query.data, search, gender, ages, order]);
  const totalPages = Math.max(1, Math.ceil(rows.length / PATIENTS_PER_PAGE));
  const currentPage = Math.min(page, totalPages);
  const firstItem = (currentPage - 1) * PATIENTS_PER_PAGE;
  const visibleRows = rows.slice(firstItem, firstItem + PATIENTS_PER_PAGE);

  if (query.isPending) return <Loading />;
  if (query.error) return <ErrorState error={query.error} retry={() => void query.refetch()} />;
  return <>
    <div className="page-heading"><div><span className="eyebrow">Área médica</span><h1>Pacientes</h1><p>Consulte informações clínicas compartilhadas e conversas.</p></div><span className="result-count">{rows.length} resultado{rows.length === 1 ? '' : 's'}</span></div>
    <Card><div className="filters"><Input label="Buscar por nome" type="search" value={search} onChange={e => { setSearch(e.target.value); setPage(1); }} /><Select label="Gênero" value={gender} onChange={e => { setGender(e.target.value); setPage(1); }}><option value="">Todos</option><option value="F">Feminino</option><option value="M">Masculino</option><option value="O">Outro</option><option value="N">Não informado</option></Select><Select label="Idade" value={ages} onChange={e => { setAges(e.target.value); setPage(1); }}><option value="">Todas</option><option value="0-29">Até 29</option><option value="30-49">30 a 49</option><option value="50-69">50 a 69</option><option value="70-">70 ou mais</option></Select><Select label="Ordenar" value={order} onChange={e => { setOrder(e.target.value); setPage(1); }}><option value="name">Nome</option><option value="recent">Mensagem recente</option><option value="exams">Mais exames</option><option value="mine">Meus pacientes</option></Select></div></Card>
    <div className="patient-list">{rows.length ? visibleRows.map(p => <Link to={`/medico/paciente/${p.idPaciente}`} key={p.idPaciente}><Card><div className="patient-avatar" aria-hidden="true">{p.nome.charAt(0)}</div><div className="patient-main"><h2>{p.nome}</h2><p>{genderLabel(p.genero)} · {age(p.dataNascimento) ?? 'idade não informada'} anos</p><small>{p.ultimaMensagemTexto ? `Última mensagem: ${p.ultimaMensagemTexto}` : 'Sem mensagens'}</small></div><div className="patient-stats"><span><strong>{p.exameCount}</strong> exames</span><span><strong>{p.historicoCount}</strong> históricos</span><span>{formatDateTime(p.ultimaMensagemEm)}</span></div><span className="chevron" aria-hidden="true">›</span></Card></Link>) : <EmptyState title="Nenhum paciente encontrado">Revise os filtros de busca.</EmptyState>}</div>
    {rows.length > PATIENTS_PER_PAGE && <nav className="patient-pagination" aria-label="Paginação de pacientes"><button type="button" className="button button--ghost" disabled={currentPage === 1} onClick={() => setPage(value => Math.max(1, value - 1))}>Anterior</button><span>Página {currentPage} de {totalPages} · {firstItem + 1}–{Math.min(firstItem + PATIENTS_PER_PAGE, rows.length)} de {rows.length}</span><button type="button" className="button button--ghost" disabled={currentPage === totalPages} onClick={() => setPage(value => Math.min(totalPages, value + 1))}>Próxima</button></nav>}
  </>;
}

export function DoctorPatientLayout() {
  const { pacienteId } = useParams();
  const id = Number(pacienteId);
  const validId = Number.isInteger(id) && id > 0;
  const patient = useQuery({ queryKey: ['patient', id], queryFn: () => patientService.byId(id), enabled: validId });
  if (!validId) return <Navigate to="/medico" replace />;
  if (patient.isPending) return <Loading />;
  if (patient.error) return <ErrorState error={patient.error} retry={() => void patient.refetch()} />;
  return <>
    <Link className="back-link" to="/medico">← Voltar para pacientes</Link>
    <div className="page-heading doctor-patient-heading"><div><span className="eyebrow">Prontuário compartilhado</span><h1>{patient.data.nome}</h1><p>{genderLabel(patient.data.genero)} · nascimento em {formatDate(patient.data.dataNascimento)}</p></div></div>
    <div className="patient-summary"><span><strong>Nascimento</strong>{formatDate(patient.data.dataNascimento)}</span><span><strong>Gênero</strong>{genderLabel(patient.data.genero)}</span><span className="patient-summary__notes"><strong>Observações</strong>{patient.data.observacoes || 'Nenhuma observação informada.'}</span></div>
    <DoctorClinicalWorkspace patientId={id} />
  </>;
}
