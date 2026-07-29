# Frontend React — Viva Mama

SPA em React, TypeScript estrito, Vite, React Router, TanStack Query, React Hook Form, Zod e Axios. O frontend HTML/JS antigo permanece intacto durante a validação da migração.

## Desenvolvimento

Requisitos: Node.js 20+ e o backend Spring Boot na porta `8080`.

```bash
npm install
npm run dev
```

O Vite abre `http://localhost:5173` e encaminha chamadas da API para `http://localhost:8080`. Para outra origem, copie `.env.example` para `.env.local` e defina `VITE_API_URL`.

## Verificação

```bash
npm run lint
npm test
npm run build
```

## Produção integrada ao Spring Boot

1. Execute `npm run build`. O Vite grava `index.html` e os assets diretamente em `src/main/resources/static/`, sem apagar os arquivos legados.
2. Empacote/inicie o Spring normalmente. `SpaController` encaminha as rotas React para o novo `index.html` minúsculo sem interceptar endpoints da API.
3. Inicie o Spring com o perfil `prod` e defina obrigatoriamente `JWT_SECRET` (32+ bytes). Defina `CORS_ALLOWED_ORIGINS` apenas quando frontend e API estiverem em origens diferentes.

Em uma implantação na mesma origem, `VITE_API_URL` deve ficar vazio. O diretório `dist` não é versionado.

## Sessão e segurança

O JWT é mantido em `sessionStorage`, sendo apagado ao receber `401` ou `403`. Downloads usam requisição autenticada e URL temporária de `Blob`. Uma evolução recomendada é o backend emitir cookie HttpOnly/Secure/SameSite, eliminando o acesso do JavaScript ao token.

Uploads são validados no frontend e no backend: PDF, JPEG, PNG ou WEBP, com no máximo 10 MB. O backend normaliza nomes e impede resolução de caminhos fora do diretório configurado.

## Organização

- `auth`: estado da sessão e proteção por perfil;
- `pages`: telas por domínio;
- `shared`: componentes reutilizáveis, feedback e upload;
- `lib`: cliente HTTP, sessão e formatação;
- `types`: contratos TypeScript da API.

O cadastro já cria o perfil no endpoint `/auth/register`; por isso a SPA não chama `/profiles/*` novamente.
