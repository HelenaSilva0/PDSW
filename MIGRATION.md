# Migração do frontend Viva Mama

## Arquitetura adotada

A nova interface fica em `frontend/` e é uma SPA. React Router substitui os arquivos HTML e `window.location`; TanStack Query centraliza cache, estados e polling; Axios concentra autenticação, erros e downloads; React Hook Form + Zod cuidam dos formulários.

## Contratos preservados

Todos os endpoints listados no escopo foram reutilizados. O formato público de paciente mantém `idPaciente`, `nome`, `dataNascimento`, `historicoFamiliar`, `observacoes` e `genero`, mas deixou de incluir a entidade `User`. Essa é uma correção compatível para o novo frontend e evita expor telefone, e-mail, perfil e detalhes internos por associação JPA.

## Correções principais

- sessão única e expiração tratada em `401/403`;
- rotas protegidas por `PACIENTE` e `MEDICO`;
- cadastro não recria perfil;
- botões bloqueados durante envio;
- polling existe apenas na tela de chat e é limpo ao desmontar;
- mensagens são deduplicadas por ID;
- downloads autenticados não dependem de links públicos;
- arquivos têm validação de extensão, MIME e tamanho nos dois lados;
- caminhos de download são normalizados e confinados ao diretório de uploads;
- datas usam `Intl.DateTimeFormat('pt-BR')`;
- estados de carregamento, erro, vazio e sucesso são consistentes;
- console H2 e SQL ficam no perfil `dev`; produção exige segredo JWT externo;
- CORS de desenvolvimento é configurável.

## Compatibilidade e pendências deliberadas

Os arquivos legados e endpoints de redirect/salvamento antigo não foram removidos: devem permanecer até testes de aceitação em navegador confirmarem a nova SPA. `MedicoController` ainda expõe entidade em endpoints administrativos que a SPA não usa; sua migração para DTO pode ser feita sem pressionar esta entrega. O chat ainda usa polling porque o backend não oferece WebSocket/SSE.
