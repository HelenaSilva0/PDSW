import { Link } from 'react-router-dom';
import { Card } from '../shared/UI';
const tips = [
  ['Conheça seu corpo', 'Observe mudanças persistentes nas mamas e procure avaliação profissional quando perceber algo diferente.'],
  ['Rastreamento importa', 'Exames podem identificar alterações precocemente. Siga a orientação da sua equipe de saúde.'],
  ['Histórico familiar conta', 'Registrar casos na família ajuda profissionais a individualizar o acompanhamento.'],
  ['Cuidado contínuo', 'Organize exames, histórico e conversas em um só lugar, com acesso protegido por perfil.']
];
export function HomePage() { return <><section className="hero"><div><span className="eyebrow">Cuidado que aproxima</span><h1>Sua saúde merece informação clara e acompanhamento humano.</h1><p>O Viva Mama conecta pacientes e médicos para acompanhar exames, histórico familiar e conversas com mais tranquilidade.</p><div className="hero__actions"><Link className="button" to="/cadastro">Começar agora</Link><Link className="button button--ghost" to="/login">Já tenho conta</Link></div></div><div className="hero__art" aria-hidden="true"><span>♥</span></div></section><section aria-labelledby="info-title"><div className="section-heading"><span className="eyebrow">Informação e prevenção</span><h2 id="info-title">Pequenos cuidados, acompanhamento mais consciente</h2></div><div className="card-grid">{tips.map(([title, text], i) => <Card key={title}><span className="tip-number">0{i + 1}</span><h3>{title}</h3><p>{text}</p></Card>)}</div></section></>; }
