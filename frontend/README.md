# Frontend React — Viva Mama

SPA em React, TypeScript estrito, Vite, React Router, TanStack Query, React Hook Form, Zod e Axios.

## Desenvolvimento

Requisitos: Node.js 20+ e o backend Spring Boot na porta `8080`.

```bash
npm install
npm run dev
```

O Vite abre `http://localhost:5173` e encaminha chamadas da API para `http://localhost:8080`. Para outra origem, copie `.env.example` para `.env.local` e defina `VITE_API_URL`.

O backend deve ser iniciado explicitamente com o perfil `dev` e um segredo JWT local:

```powershell
$env:JWT_SECRET='uma-chave-local-com-32-ou-mais-caracteres'
./mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=dev'
```

## Verificação

```bash
npm run lint
npm test
npm run build
```

## Produção integrada ao Spring Boot

1. Execute `npm run build`. O Vite grava `index.html` e os assets em `src/main/resources/static/`.
2. Empacote/inicie o Spring normalmente. `SpaController` encaminha as rotas React para `index.html`.
3. Inicie o Spring com o perfil `prod` e defina obrigatoriamente `JWT_SECRET`, `DB_URL`, `DB_DRIVER`, `DB_USERNAME`, `DB_PASSWORD` e `CORS_ALLOWED_ORIGINS`.

Em uma implantação na mesma origem, `VITE_API_URL` deve ficar vazio. Dados runtime não devem ser versionados: `data/` e `uploads/` ficam ignorados pelo Git.

## Sessão e segurança

O JWT é mantido em `sessionStorage` e removido apenas ao receber `401`; respostas `403` preservam a sessão. Downloads usam requisição autenticada e URL temporária de `Blob`. Uma evolução recomendada é o backend emitir cookie HttpOnly/Secure/SameSite.

Contas médicas exigem código de convite configurado no backend por `DOCTOR_REGISTRATION_CODE`.

Uploads são validados no frontend e no backend: PDF, JPEG, PNG ou WEBP, com no máximo 10 MB e assinatura de conteúdo compatível.

## Organização

- `auth`: estado da sessão e proteção por perfil;
- `pages`: telas por domínio;
- `shared`: componentes reutilizáveis, feedback e upload;
- `lib`: cliente HTTP, sessão e formatação;
- `types`: contratos TypeScript da API.

O cadastro cria o perfil no endpoint `/auth/register`; por isso a SPA não chama `/profiles/*` novamente.
