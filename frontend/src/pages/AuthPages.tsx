import { zodResolver } from '@hookform/resolvers/zod';
import { useState, type InputHTMLAttributes, type ReactNode } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { useAuth } from '../auth/AuthContext';
import { authService } from '../services';
import { Button, Card, Input, Select } from '../shared/UI';
import { useToast } from '../shared/ToastContext';
import type { Role } from '../types/api';

const loginSchema = z.object({ email: z.email('Informe um e-mail válido.'), senha: z.string().min(1, 'Informe sua senha.') });
type LoginData = z.infer<typeof loginSchema>;

export function LoginPage() {
  const { login } = useAuth(); const navigate = useNavigate(); const location = useLocation(); const notify = useToast();
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<LoginData>({ resolver: zodResolver(loginSchema) });
  const submit = handleSubmit(async data => { try { const session = await login(data.email, data.senha); notify('Bem-vindo(a) de volta!', 'success'); const from = (location.state as { from?: string } | null)?.from; navigate(from || (session.role === 'PACIENTE' ? '/paciente' : '/medico'), { replace: true }); } catch (error) { notify((error as Error).message, 'error'); } });
  return <div className="auth-shell"><Card><span className="eyebrow">Acesso seguro</span><h1>Entrar no Viva Mama</h1><p className="muted">Use o e-mail e a senha cadastrados.</p><form className="form" onSubmit={submit} noValidate><Input label="E-mail" type="email" autoComplete="email" error={errors.email?.message} {...register('email')} /><Input label="Senha" type="password" autoComplete="current-password" error={errors.senha?.message} {...register('senha')} /><Button type="submit" loading={isSubmitting}>Entrar</Button></form><p className="auth-foot">Ainda não tem conta? <Link to="/cadastro">Cadastre-se</Link></p></Card></div>;
}

const requiredAgreement = (message: string) => z.boolean().refine(value => value, message);
const registerSchema = z.object({
  role: z.enum(['PACIENTE', 'MEDICO']),
  nome: z.string().trim().min(2, 'Informe seu nome.'),
  email: z.email('Informe um e-mail válido.'),
  telefone: z.string().trim().min(8, 'Informe o telefone.'),
  senha: z.string().min(8, 'Use pelo menos 8 caracteres.'),
  dataNascimento: z.string().optional(), genero: z.string().optional(), observacoes: z.string().optional(), crm: z.string().optional(), especialidade: z.string().optional(),
  aceiteTermos: requiredAgreement('Aceite os Termos de Uso.'),
  cienciaPrivacidade: requiredAgreement('Confirme a leitura da Política de Privacidade.'),
  consentimentoDadosSaude: z.boolean().optional(), compromissoConfidencialidade: z.boolean().optional(),
}).superRefine((data, ctx) => {
  if (data.role === 'MEDICO') {
    if (!data.crm?.trim()) ctx.addIssue({ code: 'custom', path: ['crm'], message: 'CRM é obrigatório.' });
    if (!data.especialidade?.trim()) ctx.addIssue({ code: 'custom', path: ['especialidade'], message: 'Especialidade é obrigatória.' });
    if (!data.compromissoConfidencialidade) ctx.addIssue({ code: 'custom', path: ['compromissoConfidencialidade'], message: 'Aceite o compromisso de confidencialidade.' });
  }
  if (data.role === 'PACIENTE' && !data.consentimentoDadosSaude) ctx.addIssue({ code: 'custom', path: ['consentimentoDadosSaude'], message: 'Autorize o tratamento dos dados de saúde.' });
});
type RegisterData = z.infer<typeof registerSchema>;

function ConsentCheckbox({ children, error, ...props }: InputHTMLAttributes<HTMLInputElement> & { children: ReactNode; error?: string }) {
  return <label className="consent-field field--full"><input type="checkbox" {...props} /><span>{children}</span>{error && <small className="field__error">{error}</small>}</label>;
}

export function RegisterPage() {
  const [role, setRole] = useState<Role>('PACIENTE'); const navigate = useNavigate(); const notify = useToast();
  const { register, handleSubmit, setValue, formState: { errors, isSubmitting } } = useForm<RegisterData>({ resolver: zodResolver(registerSchema), defaultValues: { role: 'PACIENTE', genero: 'N', aceiteTermos: false, cienciaPrivacidade: false, consentimentoDadosSaude: false, compromissoConfidencialidade: false } });
  const switchRole = (next: Role) => { setRole(next); setValue('role', next); setValue('consentimentoDadosSaude', false); setValue('compromissoConfidencialidade', false); };
  const submit = handleSubmit(async data => { try { await authService.register(data); notify('Cadastro concluído. Agora você já pode entrar.', 'success'); navigate('/login'); } catch (error) { notify((error as Error).message, 'error'); } });
  return <div className="auth-shell auth-shell--wide"><Card><span className="eyebrow">Nova conta</span><h1>Crie seu acesso</h1><div className="segmented" role="group" aria-label="Tipo de conta"><Button type="button" className={role === 'PACIENTE' ? '' : 'button--ghost'} aria-pressed={role === 'PACIENTE'} onClick={() => switchRole('PACIENTE')}>Sou paciente</Button><Button type="button" className={role === 'MEDICO' ? '' : 'button--ghost'} aria-pressed={role === 'MEDICO'} onClick={() => switchRole('MEDICO')}>Sou médico(a)</Button></div><form className="form form--grid" onSubmit={submit} noValidate><Input label="Nome completo" error={errors.nome?.message} required {...register('nome')} /><Input label="E-mail" type="email" autoComplete="email" error={errors.email?.message} required {...register('email')} /><Input label="Telefone" type="tel" autoComplete="tel" error={errors.telefone?.message} required {...register('telefone')} /><Input label="Senha" type="password" autoComplete="new-password" error={errors.senha?.message} required {...register('senha')} />{role === 'PACIENTE' ? <><Input label="Data de nascimento" type="date" error={errors.dataNascimento?.message} {...register('dataNascimento')} /><Select label="Gênero" {...register('genero')}><option value="N">Prefiro não informar</option><option value="F">Feminino</option><option value="M">Masculino</option><option value="O">Outro</option></Select><label className="field field--full"><span>Observações</span><textarea {...register('observacoes')} /></label></> : <><Input label="CRM" error={errors.crm?.message} required {...register('crm')} /><Input label="Especialidade" error={errors.especialidade?.message} required {...register('especialidade')} /><p className="field--full muted">Após o cadastro, seu acesso ficará disponível conforme as regras da plataforma.</p></>}
      <div className="consent-box field--full"><strong>Termos e privacidade</strong><ConsentCheckbox error={errors.aceiteTermos?.message} {...register('aceiteTermos')}>Li e concordo com os <Link to="/termos" target="_blank" rel="noreferrer">Termos de Uso</Link>.</ConsentCheckbox><ConsentCheckbox error={errors.cienciaPrivacidade?.message} {...register('cienciaPrivacidade')}>Li a <Link to="/privacidade" target="_blank" rel="noreferrer">Política de Privacidade</Link> e estou ciente de como meus dados serão tratados.</ConsentCheckbox>{role === 'PACIENTE' ? <ConsentCheckbox error={errors.consentimentoDadosSaude?.message} {...register('consentimentoDadosSaude')}>Autorizo, de forma específica e destacada, o tratamento dos meus dados de saúde — histórico, exames e conversas — para meu acompanhamento dentro da plataforma.</ConsentCheckbox> : <ConsentCheckbox error={errors.compromissoConfidencialidade?.message} {...register('compromissoConfidencialidade')}>Comprometo-me a manter sigilo e acessar dados de pacientes somente para finalidades assistenciais autorizadas.</ConsentCheckbox>}<small className="consent-note">Os aceites são registrados com a versão e a data para fins de comprovação.</small></div><Button className="field--full" type="submit" loading={isSubmitting}>Criar conta</Button></form><p className="auth-foot">Já tem conta? <Link to="/login">Entrar</Link></p></Card></div>;
}
