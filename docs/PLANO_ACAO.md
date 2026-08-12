# Plano de Ação — Viva Mama

## Objetivo

Resolver os riscos de segurança, falhas funcionais, problemas de consistência e pendências de qualidade identificados na auditoria do projeto.

A política atual considerada neste plano é que médicos podem consultar todos os pacientes. O acesso deve continuar sendo autenticado, auditável e limitado às operações permitidas.

## 1. Baseline e validação inicial

Executar, antes das correções:

```bash
mvn test
cd frontend
npm ci
npm run lint
npm test
npm run build
```

Registrar os erros encontrados e preservar a alteração local existente em `src/main/resources/static/Index.html`.

Java 17 e Node.js 20+ são pré-requisitos do ambiente. JDK, `node_modules`, caches Maven, `data/` e `uploads/` não devem ser versionados.

## 2. Segurança e autenticação

### Cadastro médico

- Manter cadastro público para pacientes.
- Substituir progressivamente o código médico compartilhado por aprovação administrativa.
- Criar status de usuário: `PENDENTE`, `ATIVO` e `BLOQUEADO`.
- Bloquear login de médicos pendentes ou bloqueados.
- Criar operações administrativas para listar, aprovar e rejeitar médicos.
- Garantir que o papel administrativo não possa ser criado pelo endpoint público.
- Registrar aprovação, rejeição, responsável e data da operação.

Endpoints previstos:

```text
GET  /admin/medicos/pendentes
POST /admin/medicos/{id}/aprovar
POST /admin/medicos/{id}/rejeitar
```

### Login e sessão

- Manter mensagens genéricas para e-mail inexistente e senha incorreta.
- Adicionar rate limiting, atraso progressivo e bloqueio temporário após tentativas excessivas.
- Registrar tentativas suspeitas sem armazenar senhas.
- Migrar futuramente o JWT do armazenamento JavaScript para cookie `HttpOnly`, `Secure` e `SameSite`.
- Manter `401` para sessão inválida e `403` para falta de permissão, sem deslogar o usuário em `403`.

## 3. Configuração e banco de dados

- Usar H2 somente em desenvolvimento e testes.
- Usar PostgreSQL como banco recomendado para produção.
- Adicionar o driver PostgreSQL ao `pom.xml`.
- Exigir em produção:
  - `DB_URL`;
  - `DB_DRIVER`;
  - `DB_USERNAME`;
  - `DB_PASSWORD`;
  - `JWT_SECRET`;
  - `CORS_ALLOWED_ORIGINS`.
- Manter `ddl-auto=validate` em produção.
- Criar migrations versionadas para as alterações de schema.
- Fazer backup antes de executar migrations em ambiente real.

As migrations devem cobrir:

- unicidade de chat por médico e paciente;
- unicidade de versão por paciente e tipo de documento;
- status de aprovação de usuários médicos;
- índices de busca por paciente, médico e data.

## 4. Autorização de dados médicos

- Remover qualquer exposição direta de entidades JPA.
- Usar DTOs em pacientes, médicos, perfis e respostas de API.
- Manter todos os acessos médicos autenticados e registrados em auditoria.
- Restringir criação, alteração e exclusão de perfis médicos ao próprio médico ou a administrador.
- Confirmar que a interface de médico e o backend possuem a mesma política de leitura e escrita.
- Se médicos forem somente leitores de histórico, remover permissões de criação de snapshots e anexos.

## 5. Uploads e downloads

- Remover o acesso estático direto a `/uploads/**`.
- Servir exames e anexos somente por endpoints autenticados.
- Validar autorização antes de cada download.
- Validar extensão, MIME, tamanho e assinatura real do arquivo.
- Manter nomes físicos gerados pelo servidor, sem usar nomes fornecidos pelo usuário como caminho.
- Excluir arquivos físicos quando a gravação do banco falhar.
- Criar rotina periódica para localizar e remover arquivos órfãos.
- Evitar retornar metadados internos de armazenamento na API.

Testar especialmente:

- PDF ou imagem falsificada;
- arquivo maior que 10 MB;
- tentativa de path traversal;
- download por paciente diferente;
- download sem autenticação;
- arquivo registrado no banco, mas ausente no disco.

## 6. Validação e contratos de API

- Criar DTOs de entrada para todos os endpoints.
- Aplicar `@Valid` no backend.
- Definir limites para e-mail, telefone, nome, CRM, especialidade, observações, descrições e mensagens.
- Validar formato de e-mail e datas.
- Rejeitar mensagens vazias ou acima de 2.000 caracteres.
- Padronizar respostas de erro com status, mensagem e timestamp.
- Evitar exceções de banco causadas por entrada inválida.

## 7. Integridade e concorrência

### Versionamento

- Não depender apenas de `MAX(versao) + 1`.
- Criar contador de versão por paciente e tipo de documento, protegido por transação/lock.
- Manter índice único como proteção adicional.
- Retentar conflitos transitórios de versão de forma controlada.

### Chats

- Manter índice único para médico/paciente.
- Criar chats dentro de transação.
- Em caso de conflito, buscar e retornar o chat já existente.
- Evitar mensagens duplicadas no frontend e no backend.

## 8. Exclusão e privacidade

- Definir exclusão física ou anonimização conforme a política de retenção.
- Remover ou anonimizar usuário, perfil, chats, mensagens, exames, anexos e históricos de forma transacional.
- Criar testes de exclusão com e sem dados relacionados.
- Criar rotina de limpeza física para arquivos cuja remoção falhar.
- Remover dados médicos reais do repositório.
- Caso dados reais tenham sido publicados em commits anteriores, limpar o histórico Git e rotacionar credenciais relacionadas.

## 9. Frontend

- Corrigir retries de queries de paciente, exames e históricos.
- Validar IDs de rota antes de buscar dados.
- Limpar corretamente o input de upload após sucesso ou erro.
- Tratar `401` e `403` de forma diferente.
- Validar intervalo de polling.
- Adicionar estados claros para carregamento, erro, vazio e perfil inexistente.
- Tornar React a única interface oficial após validação da migração.
- Remover gradualmente `app.js`, HTMLs legados e autenticação baseada em `localStorage`.
- Eliminar a duplicidade entre `Index.html` e `index.html` sem sobrescrever a alteração local antes de validá-la.
- Evitar acumulação de assets antigos no diretório de produção.

## 10. Performance

- Eliminar consultas N+1 no dashboard médico e na lista de chats.
- Buscar últimas mensagens e contagens com consultas agregadas.
- Adicionar paginação para pacientes, mensagens, exames e históricos.
- Criar índices para filtros, ordenação e relacionamentos mais usados.

## 11. Testes

### Backend

Criar testes para:

- cadastro de paciente;
- bloqueio de cadastro médico sem aprovação;
- aprovação e rejeição de médico;
- login inválido e rate limiting;
- autorização por papel;
- acesso global médico conforme política definida;
- download autorizado e não autorizado;
- uploads inválidos;
- exclusão completa de conta;
- concorrência de versões;
- concorrência de chats;
- validação de payloads.

### Frontend

Criar testes para:

- login, logout e expiração;
- preservação da sessão em `403`;
- encerramento em `401`;
- proteção de rotas por papel;
- retry correto das queries;
- rota médica com ID inválido;
- upload e limpeza do campo;
- cadastro médico pendente;
- download autenticado.

## 12. CI/CD e critérios de aceite

A pipeline deve executar:

```bash
mvn verify
cd frontend
npm ci
npm run lint
npm test
npm run build
```

O trabalho será considerado concluído quando:

- backend e frontend compilarem sem erros;
- todos os testes automatizados passarem;
- não houver dados médicos reais no Git;
- downloads exigirem autorização;
- cadastro médico não conceder acesso sem aprovação;
- exclusão remover todos os dados associados;
- versões e chats não duplicarem em testes concorrentes;
- produção iniciar sem segredos ou banco padrão;
- React for a única interface oficial;
- CI bloquear merges com falhas de qualidade ou segurança.

## Ordem de execução

1. Baseline e testes atuais.
2. Higienização de dados e configurações.
3. Autenticação, aprovação médica e autorização.
4. Uploads, downloads e exclusão.
5. Validação e integridade de dados.
6. Correções do frontend.
7. Performance.
8. Testes automatizados e CI/CD.

## Assumptions

- Médicos podem consultar todos os pacientes, conforme decisão atual.
- PostgreSQL será o banco de produção recomendado.
- H2 permanecerá restrito a desenvolvimento e testes.
- Java e Node.js já estão instalados no ambiente.
- A alteração local em `Index.html` será preservada.
