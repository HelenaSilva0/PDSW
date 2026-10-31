import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { useAuth } from '../auth/AuthContext';
import { accountService, patientService } from '../services';
import { Modal } from '../shared/Modal';
import { useToast } from '../shared/ToastContext';
import { Button, Card, ErrorState, Input, Loading, Select } from '../shared/UI';
import type { Patient } from '../types/api';

const schema = z.object({ nome: z.string().trim().min(2, 'Informe seu nome.'), dataNascimento: z.string().nullable(), genero: z.enum(['F', 'M', 'O', 'N']), observacoes: z.string() });
type FormData = z.infer<typeof schema>;

function formatDate(value: string | null) {
  if (!value) return 'Não informado';
  const [year, month, day] = value.split('-');
  return day && month && year ? `${day}/${month}/${year}` : value;
}

function genderLabel(value: Patient['genero']) {
  return { N: 'Não informado', F: 'Feminino', M: 'Masculino', O: 'Outro' }[value] ?? 'Não informado';
}

export function PatientPage() {
  const notify = useToast();
  const client = useQueryClient();
  const navigate = useNavigate();
  const { logout } = useAuth();
  const [editing, setEditing] = useState(false);
  const [deleteOpen, setDeleteOpen] = useState(false);
  const query = useQuery({ queryKey: ['patient', 'me'], queryFn: patientService.me });
  const { register, handleSubmit, reset, formState: { errors } } = useForm<FormData>({ resolver: zodResolver(schema), defaultValues: { nome: '', dataNascimento: null, genero: 'N', observacoes: '' } });

  useEffect(() => {
    if (query.data) reset({ nome: query.data.nome, dataNascimento: query.data.dataNascimento, genero: query.data.genero, observacoes: query.data.observacoes ?? '' });
  }, [query.data, reset]);

  const mutation = useMutation({
    mutationFn: (data: FormData) => patientService.updateMe(data as Partial<Patient>),
    onSuccess: data => { client.setQueryData(['patient', 'me'], data); setEditing(false); notify('Dados atualizados.', 'success'); },
    onError: e => notify(e.message, 'error'),
  });
  const deleteMutation = useMutation({ mutationFn: accountService.deleteMe, onSuccess: () => { setDeleteOpen(false); logout(); navigate('/', { replace: true }); }, onError: e => notify(e.message, 'error') });
  const leave = () => { logout(); navigate('/'); };

  if (query.isPending) return <Loading />;
  if (query.error) return <ErrorState error={query.error} retry={() => void query.refetch()} />;

  return <>
    <div className="page-heading"><div><span className="eyebrow">Área da paciente</span><h1>Olá, {query.data.nome.split(' ')[0]}</h1><p>Consulte seus dados e acesse seu acompanhamento.</p></div></div>
    <div className="dashboard-grid">
      <Card className="dashboard-grid__profile">
        <div className="profile-card__heading"><h2>Dados pessoais</h2>{!editing && <Button type="button" className="button--ghost button--small" onClick={() => setEditing(true)}>Editar</Button>}</div>
        {editing ? <form className="form" onSubmit={handleSubmit(data => mutation.mutate(data))}>
          <Input label="Nome" error={errors.nome?.message} required {...register('nome')} />
          <Input label="Data de nascimento" type="date" error={errors.dataNascimento?.message} {...register('dataNascimento')} />
          <Select label="Gênero" {...register('genero')}><option value="N">Não informado</option><option value="F">Feminino</option><option value="M">Masculino</option><option value="O">Outro</option></Select>
          <label className="field"><span>Observações</span><textarea rows={3} {...register('observacoes')} /></label>
          <div className="profile-card__actions"><Button type="button" className="button--ghost" onClick={() => { reset(); setEditing(false); }}>Cancelar</Button><Button type="submit" loading={mutation.isPending}>Salvar alterações</Button></div>
        </form> : <div className="profile-details">
          <div><span>Nome</span><strong>{query.data.nome}</strong></div>
          <div><span>Data de nascimento</span><strong>{formatDate(query.data.dataNascimento)}</strong></div>
          <div><span>Gênero</span><strong>{genderLabel(query.data.genero)}</strong></div>
          <div className="profile-details__wide"><span>Observações</span><strong>{query.data.observacoes || 'Nenhuma observação cadastrada.'}</strong></div>
        </div>}
        <div className="profile-card__footer"><Button type="button" className="button--ghost" onClick={leave}>Sair</Button><Button type="button" className="button--ghost" onClick={() => setDeleteOpen(true)}>Excluir conta</Button></div>
      </Card>
      <div className="quick-links"><Link to="/paciente/historico-familiar"><Card><span className="quick-icon">◎</span><h2>Histórico familiar</h2><p>Responda ao formulário e consulte versões anteriores.</p></Card></Link><Link to="/paciente/exames"><Card><span className="quick-icon">⇧</span><h2>Meus exames</h2><p>Envie e baixe exames com segurança.</p></Card></Link><Link to="/paciente/chat"><Card><span className="quick-icon">◇</span><h2>Conversas</h2><p>Fale com os profissionais que acompanham você.</p></Card></Link></div>
    </div>
    <Modal open={deleteOpen} title="Excluir conta definitivamente" onClose={() => !deleteMutation.isPending && setDeleteOpen(false)}><div className="delete-confirmation"><p>Tem certeza? Todos os seus dados, incluindo exames, histórico familiar e conversas, serão removidos permanentemente.</p><div className="modal__actions"><Button type="button" className="button--ghost" disabled={deleteMutation.isPending} onClick={() => setDeleteOpen(false)}>Cancelar</Button><Button type="button" className="button--danger" loading={deleteMutation.isPending} onClick={() => deleteMutation.mutate()}>Excluir definitivamente</Button></div></div></Modal>
  </>;
}
