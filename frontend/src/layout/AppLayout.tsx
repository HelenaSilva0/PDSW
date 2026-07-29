import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { Button } from '../shared/UI';
export function AppLayout() {
  const { session, logout } = useAuth(); const navigate = useNavigate();
  const leave = () => { logout(); navigate('/'); };
  return <><header className="header"><NavLink className="brand" to="/" aria-label="Viva Mama — início"><span className="brand__mark" aria-hidden="true">♥</span><span>Viva Mama</span></NavLink><nav aria-label="Navegação principal">{session?.role === 'PACIENTE' && <><NavLink to="/paciente">Meu painel</NavLink><NavLink to="/paciente/historico-familiar">Histórico</NavLink><NavLink to="/paciente/exames">Exames</NavLink><NavLink to="/paciente/chat">Chat</NavLink></>}{session?.role === 'MEDICO' && <><NavLink to="/medico">Pacientes</NavLink><NavLink to="/medico/chat">Chat</NavLink></>}{session ? <Button type="button" className="button--ghost" onClick={leave}>Sair</Button> : <><NavLink to="/login">Entrar</NavLink><NavLink className="nav-cta" to="/cadastro">Criar conta</NavLink></>}</nav></header><main id="main" className="container"><Outlet /></main><footer>Viva Mama · Informação não substitui avaliação médica.</footer></>;
}
