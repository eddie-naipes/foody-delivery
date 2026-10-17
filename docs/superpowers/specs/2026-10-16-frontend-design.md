# Foody Delivery — Design do Front-end

**Data:** 2026-10-16
**Escopo:** aplicação React que consome a API do back-end (`docs/superpowers/specs/2026-10-16-backend-design.md`).

## 1. Objetivo

O enunciado pede uma aplicação React que **lista os pedidos com seus status atuais e permite criar um novo pedido**. Como a API exige autenticação e expõe a troca de status, o front também cobre cadastro/login e o avanço do pedido pelo fluxo.

**Quem usa:** o operador do restaurante (atendente/expedição). **Trabalho principal:** ver de relance em que etapa cada pedido está e movê-lo para a próxima.

## 2. Decisões técnicas

| Item | Decisão |
|---|---|
| Base | Vite 7 + React 19 + TypeScript 5.9 |
| Rotas | React Router 7 (modo biblioteca) |
| Dados do servidor | TanStack Query 5 — cache, invalidação após mutações, estados de carregamento/erro |
| Formulários | React Hook Form + Zod (mesmas regras do back: CEP, senha 6–72 bytes, ≥1 item, preço com 2 casas) |
| HTTP | Axios com interceptors: injeta `Authorization: Bearer`, normaliza erros para `ApiError` e faz logout em 401 |
| Estilo | Tailwind CSS 4 com tokens próprios (seção 5) |
| Testes | Vitest + Testing Library + MSW (API simulada). Foco em fluxos e regras, sem meta de 100% |
| Mesma origem | Dev: proxy do Vite `/api → :8080`. Produção: Nginx serve o build e repassa `/api` ao container da API. Sem CORS em nenhum dos dois |
| Sessão | Token + usuário no `localStorage`; expirado/recusado (401) limpa a sessão e volta ao login |

Versões fixadas (exatas) na geração estável anterior às majors mais recentes, para evitar APIs recém-alteradas.

## 3. Arquitetura (por funcionalidade)

```
frontend/src
├── app/          App, rotas, providers (QueryClient, Auth), layout autenticado
├── shared/
│   ├── api/      cliente HTTP, tipo ApiError e parser de erros da API
│   ├── ui/       Button, TextField, Alert, Spinner, EmptyState
│   └── lib/      formatação (moeda, data relativa), cn()
├── features/
│   ├── auth/     api, AuthProvider/useAuth, ProtectedRoute, LoginPage, RegisterPage, schemas
│   └── orders/   api, types, status (rótulos, cores, transições), hooks (queries/mutations),
│                 schemas, OrdersPage, NewOrderPage, OrderDetailPage e componentes
└── test/         setup do Vitest, handlers MSW e utilitários de render
```

Regras de dependência: `features/*` usam `shared/*`; `shared` não conhece features; uma feature não importa a outra, exceto `orders` usando `useAuth` para o nome do operador.

**Máquina de estados espelhada** em `features/orders/status.ts` (mesmo fluxo do back) para mostrar só os botões possíveis. O back continua sendo a autoridade: um 422 é exibido como erro.

**Erros da API:** `fieldErrors` do back viram erros nos campos do formulário (`setError`, inclusive caminhos como `items[0].quantity` e `deliveryAddress.zipCode`); demais mensagens aparecem em um `Alert`.

## 4. Telas

| Rota | Tela | Conteúdo |
|---|---|---|
| `/login` | Entrar | e-mail, senha, link para cadastro |
| `/cadastro` | Criar conta | nome, e-mail, senha; entra direto após cadastrar |
| `/pedidos` | Pedidos | contagem por status (que também filtra a lista), comandas com nº, cliente, bairro, itens, total, status e há quanto tempo; estado vazio convida a criar o primeiro pedido |
| `/pedidos/novo` | Novo pedido | formulário (cliente, endereço, itens dinâmicos) ao lado de uma **comanda que se monta ao vivo** com subtotais e total |
| `/pedidos/:id` | Pedido | comanda completa, trilho de etapas do fluxo e ações: "Iniciar preparo", "Saiu para entrega", "Marcar como entregue", "Cancelar pedido" (com confirmação) |

## 5. Direção visual

**Conceito (revisado em 2026-10-17):** a interface segue a identidade visual atual da Foody Delivery (foodydelivery.com e o app de operação mostrado no site), para o sistema parecer parte da plataforma. Os tokens foram extraídos dos estilos do próprio site.

**Cor**

| Token | Hex | Uso |
|---|---|---|
| `bench` | `#FAF5F3` | fundo da aplicação (bege quente das seções do site) |
| `ink` | `#241E1C` | texto principal |
| `muted` | `#5B534F` | texto secundário |
| `line` | `#EFE6E2` | bordas de cartões e divisórias |
| `brand` | `#D30101` | ação principal, número do pedido, aba ativa |
| `coral` | `#FF5C5C` | destaque da marca ("Delivery"), hover |
| `blush` | `#FFF0EF` | fundo de avisos e do total |
| status | `#2563EB` recebido, `#C2410C` em preparo, `#7C3AED` saiu, `#15803D` entregue, `#6B7280` cancelado |

O botão principal usa o vermelho `#D30101` (e não o coral do site) porque texto branco sobre coral não atinge contraste legível.

**Tipografia:** *Sen*, a fonte da marca, em toda a interface; títulos em 800, rótulos em 700, números com algarismos tabulares.

**Componentes:** botões em pílula com sombra avermelhada; cartões brancos com raio de 20px e borda quente; cabeçalho branco com abas sublinhadas em vermelho; pedido como cartão com etiqueta de status, número em vermelho e "Entregar em:" em negrito, como no app da Foody. O logotipo da empresa não é reproduzido; a marca usa um ícone próprio.

**Princípios:** status sempre com cor e texto; vermelho reservado para ação e erro (o foco dos campos é escuro, para não parecer erro); foco de teclado visível; movimento só em resposta a ação e respeitando `prefers-reduced-motion`.

**Entrada de dados:** telefone, CEP, quantidade e preço têm máscara enquanto se digita — o campo nunca aceita caractere inválido — e todos os textos têm limite de tamanho.

## 6. Docker

`frontend/Dockerfile` em dois estágios (Node para o build, Nginx para servir). `nginx.conf` com fallback de SPA (`try_files ... /index.html`) e `location /api/` repassando a `http://api:8080`. O `docker-compose.yml` ganha o serviço `web` (porta 80) dependente de `api`; a API deixa de precisar ser exposta publicamente.

## 7. Validação

1. `npm run test` (Vitest) e `npm run build` (inclui checagem de tipos) passando.
2. Sessão no Chrome percorrendo: cadastro, login, logout, rota protegida sem sessão, lista vazia, criação com erros de validação vindos do front e do back, criação válida, filtro por status, avanço até entregue, cancelamento, transição inválida, token expirado/adulterado, layout no celular.
