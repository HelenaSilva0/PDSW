# Viva Mama

Sistema web para comunicação segura entre pacientes e médicos, com foco no acompanhamento contínuo da saúde das mamas.

O projeto é composto por:

- uma API REST em Spring Boot;
- uma aplicação frontend em React, TypeScript e Vite;
- autenticação baseada em JWT;
- cadastro e gerenciamento de pacientes e médicos;
- histórico familiar e anexos de exames;
- comunicação entre paciente e médico por chat;
- banco H2 para desenvolvimento e PostgreSQL para produção.

## Tecnologias

### Backend

- Java 17;
- Spring Boot 3.5.7;
- Spring Web, Spring Data JPA e Spring Security;
- JWT;
- Maven Wrapper;
- H2 em desenvolvimento e PostgreSQL em produção.

### Frontend

- React 19;
- TypeScript;
- Vite;
- React Router;
- TanStack Query;
- Axios;
- React Hook Form e Zod.

## Pré-requisitos

- Java 17 ou superior;
- Node.js 20 ou superior;
- npm;
- Windows PowerShell, Linux ou macOS.

As versões recomendadas pelo projeto já estão configuradas no `pom.xml` e em `frontend/package.json`.

## Como executar em desenvolvimento

O backend e o frontend devem ser executados simultaneamente em dois terminais.

### 1. Iniciar o backend

Na raiz do projeto:

```powershell
$env:JWT_SECRET="uma-chave-local-com-32-ou-mais-caracteres"
.\mvnw.cmd -Pdev spring-boot:run "-Dspring-boot.run.profiles=dev"
```

No Linux ou macOS:

```bash
export JWT_SECRET="uma-chave-local-com-32-ou-mais-caracteres"
./mvnw -Pdev spring-boot:run "-Dspring-boot.run.profiles=dev"
```

O backend será iniciado em `http://localhost:8080`.

> `-Pdev` ativa o perfil Maven que inclui o driver do H2; `-Dspring-boot.run.profiles=dev` ativa o perfil Spring. Sem o `-Pdev` a aplicação falha com `Cannot load driver class: org.h2.Driver` e encerra em segundos.

O perfil `dev` utiliza um banco H2 persistido localmente em `data/vivamama`. Esse diretório pode ser criado automaticamente na primeira execução.

### 2. Iniciar o frontend

Em outro terminal:

```powershell
cd frontend
npm.cmd install
npm.cmd run dev
```

No Linux ou macOS:

```bash
cd frontend
npm install
npm run dev
```

A aplicação ficará disponível em `http://localhost:5173`.

Durante o desenvolvimento, o Vite encaminha as chamadas da API para o backend em `http://localhost:8080`.

> No PowerShell, caso a execução de scripts esteja bloqueada, use `npm.cmd` em vez de `npm`.

### Primeiro uso

- Crie uma conta de **paciente** e outra de **médico** em `/cadastro`. O médico informa CRM e especialidade e já entra com status `ATIVO`.
- Não há usuário administrador criado automaticamente.
- O médico consulta o histórico familiar dos pacientes, mas não pode alterá-lo; apenas o próprio paciente grava histórico e anexos.

## Configurações opcionais

Para alterar a origem permitida pelo CORS:

```powershell
$env:CORS_ALLOWED_ORIGINS="http://localhost:5173"
```

O frontend pode usar outra URL de API através de `frontend/.env.local`:

```env
VITE_API_URL=http://localhost:8080
```

Para o ambiente de desenvolvimento padrão, essa variável pode ser omitida.

## Testes e validações

### Backend

Na raiz do projeto:

```powershell
.\mvnw.cmd test
```

### Frontend

Dentro de `frontend/`:

```powershell
npm.cmd run lint
npm.cmd test
npm.cmd run build
```

## Build integrado

Para gerar o frontend dentro dos arquivos estáticos do Spring Boot:

```powershell
cd frontend
npm.cmd run build
cd ..
.\mvnw.cmd clean package
```

O build do frontend será gravado em `src/main/resources/static/`.

> **Nota:** o build regrava `src/main/resources/static/assets/` e o Git passará a mostrar arquivos novos ou alterados nessa pasta. Os bundles gerados ainda são versionados; remova os antigos que não forem mais referenciados por `index.html` antes de commitar. Depois, o backend pode ser iniciado com o artefato gerado:

```powershell
$env:JWT_SECRET="uma-chave-local-com-32-ou-mais-caracteres"
java -jar target\demo-0.0.1-SNAPSHOT.war
```

## Produção

Em produção, use o perfil `prod` e configure obrigatoriamente:

- `JWT_SECRET`;
- `DB_URL`;
- `DB_DRIVER`;
- `DB_USERNAME`;
- `DB_PASSWORD`;
- `CORS_ALLOWED_ORIGINS`.

Exemplo:

```powershell
$env:JWT_SECRET="segredo-forte-de-producao"
$env:DB_URL="jdbc:postgresql://localhost:5432/vivamama"
$env:DB_DRIVER="org.postgresql.Driver"
$env:DB_USERNAME="vivamama"
$env:DB_PASSWORD="senha"
$env:CORS_ALLOWED_ORIGINS="https://exemplo.com"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=prod"
```

Não versionar credenciais, dados do banco ou arquivos enviados pelos usuários.

## Estrutura principal

```text
src/main/java/vivamama/       Código do backend
src/main/resources/           Configurações e arquivos estáticos
frontend/src/                 Código da aplicação React
frontend/package.json         Scripts e dependências do frontend
data/                         Banco H2 local
uploads/                      Arquivos enviados durante a execução
```

## Solução de problemas

### Porta 8080 ocupada

Encerre o processo que está usando a porta ou altere a porta do Spring Boot com:

```powershell
$env:SERVER_PORT="8081"
```

Se alterar a porta do backend, ajuste também o proxy em `frontend/vite.config.ts` ou configure `VITE_API_URL`.

### Login temporariamente bloqueado

O backend limita tentativas consecutivas de login. Aguarde alguns minutos antes de tentar novamente.

### Banco local com dados inconsistentes

Pare o backend e faça uma cópia de segurança antes de remover os arquivos do banco em `data/`. Na próxima execução, o banco será recriado conforme a configuração do perfil `dev`.
