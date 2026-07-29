import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { Link } from 'react-router-dom';
import { z } from 'zod';
import { patientService } from '../services';
import { useToast } from '../shared/ToastContext';
import { Button, Card, ErrorState, Input, Loading, Select } from '../shared/UI';
import type { Patient } from '../types/api';
const schema = z.object({ nome: z.string().trim().min(2, 'Informe seu nome.'), dataNascimento: z.string().nullable(), genero: z.enum(['F', 'M', 'O', 'N']), observacoes: z.string() });
type FormData = z.infer<typeof schema>;
export function PatientPage() {
  const notify = useToast(); const client = useQueryClient(); const query = useQuery({ queryKey: ['patient', 'me'], queryFn: patientService.me });
  const { register, handleSubmit, reset, formState: { errors } } = useForm<FormData>({ resolver: zodResolver(schema), defaultValues: { nome: '', dataNascimento: null, genero: 'N', observacoes: '' } });
  useEffect(() => { if (query.data) reset({ nome: query.data.nome, dataNascimento: query.data.dataNascimento, genero: query.data.genero, observacoes: query.data.observacoes ?? '' }); }, [query.data, reset]);
  const mutation = useMutation({ mutationFn: (data: FormData) => patientService.updateMe(data as Partial<Patient>), onSuccess: data => { client.setQueryData(['patient', 'me'], data); notify('Dados atualizados.', 'success'); }, onError: e => notify(e.message, 'error') });
  if (query.isPending) return <Loading />; if (query.error) return <ErrorState error={query.error} retry={() => void query.refetch()} />;
  return <><div className="page-heading"><div><span className="eyebrow">Área da paciente</span><h1>Olá, {query.data.nome.split(' ')[0]}</h1><p>Atualize seus dados e acesse seu acompanhamento.</p></div></div><div className="dashboard-grid"><Card className="dashboard-grid__profile"><h2>Dados pessoais</h2><form className="form" onSubmit={handleSubmit(data => mutation.mutate(data))}><Input label="Nome" error={errors.nome?.message} required {...register('nome')} /><Input label="Data de nascimento" type="date" error={errors.dataNascimento?.message} {...register('dataNascimento')} /><Select label="Gênero" {...register('genero')}><option value="N">Não informado</option><option value="F">Feminino</option><option value="M">Masculino</option><option value="O">Outro</option></Select><label className="field"><span>Observações</span><textarea rows={4} {...register('observacoes')} /></label><Button type="submit" loading={mutation.isPending}>Salvar alterações</Button></form></Card><div className="quick-links"><Link to="/paciente/historico-familiar"><Card><span className="quick-icon">◎</span><h2>Histórico familiar</h2><p>Responda ao formulário e consulte versões anteriores.</p></Card></Link><Link to="/paciente/exames"><Card><span className="quick-icon">⇧</span><h2>Meus exames</h2><p>Envie e baixe exames com segurança.</p></Card></Link><Link to="/paciente/chat"><Card><span className="quick-icon">◇</span><h2>Conversas</h2><p>Fale com os profissionais que acompanham você.</p></Card></Link></div></div></>;
}
