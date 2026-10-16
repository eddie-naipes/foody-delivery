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

**Conceito:** a expedição de um restaurante funciona com **comandas** — tiras de papel térmico penduradas em ordem. A interface trata cada pedido como uma comanda: papel branco com borda serrilhada, número grande impresso, itens em colunas. Esse é o único elemento ousado; o resto da interface fica quieto e disciplinado.

**Cor**

| Token | Hex | Uso |
|---|---|---|
| `bench` | `#E4E8EC` | fundo da aplicação (bancada de inox) |
| `ink` | `#17212B` | texto principal e barra superior |
| `paper` | `#FFFFFF` | comandas e formulários |
| `muted` | `#5B6875` | texto secundário |
| `ticket` | `#FFC233` | ação principal (amarelo de bag de entrega), sempre com texto `ink` |
| status | `#2563EB` recebido · `#C2410C` em preparo · `#7C3AED` saiu · `#15803D` entregue · `#6B7280` cancelado |

**Tipografia:** *Instrument Sans* para a interface. *IBM Plex Mono* **apenas dentro da comanda** (número, itens, valores) — é a voz da impressora térmica, não um rótulo decorativo. Escala: 13 / 15 / 18 / 24 / 36; número da comanda em 36 mono.

**Layout:** alinhado à esquerda, largura máxima 1200px, barra superior `ink` fina. Lista em grade de comandas (1 coluna no celular, 2–3 no desktop). Novo pedido em duas colunas: formulário à esquerda, comanda viva à direita (empilha no celular).

```
┌──────────────────────────────────────────────────────┐
│ Foody Expedição                 Operador  [Sair]     │  barra ink
├──────────────────────────────────────────────────────┤
│ Pedidos                               [Novo pedido]  │
│ [Todos 12] [Recebido 3] [Em preparo 4] [Saiu 2] ...  │  contagem = filtro
│ ┌╌╌╌╌╌╌╌╌╌╌┐ ┌╌╌╌╌╌╌╌╌╌╌┐ ┌╌╌╌╌╌╌╌╌╌╌┐               │
│ │ #0042    │ │ #0041    │ │ #0040    │  comandas     │
│ │ Maria S. │ │ ...      │ │ ...      │               │
│ │ 3 itens  │ │          │ │          │               │
│ │ R$ 99,90 │ │          │ │          │               │
│ │ ●Preparo │ │          │ │          │               │
│ └╌╌╌╌╌╌╌╌╌╌┘ └╌╌╌╌╌╌╌╌╌╌┘ └╌╌╌╌╌╌╌╌╌╌┘               │
└──────────────────────────────────────────────────────┘
```

**Princípios:** a comanda é o produto; status sempre com cor **e** texto (nunca só cor); sem sombras genéricas nem gradientes; foco de teclado visível; movimento só como resposta a uma ação (a comanda viva "imprime" uma linha nova ao adicionar item; respeita `prefers-reduced-motion`).

**Textos:** verbos diretos e a mesma palavra do botão ao aviso ("Criar pedido" → "Pedido criado"). Erros dizem o que aconteceu e o que fazer.

## 6. Docker

`frontend/Dockerfile` em dois estágios (Node para o build, Nginx para servir). `nginx.conf` com fallback de SPA (`try_files ... /index.html`) e `location /api/` repassando a `http://api:8080`. O `docker-compose.yml` ganha o serviço `web` (porta 80) dependente de `api`; a API deixa de precisar ser exposta publicamente.

## 7. Validação

1. `npm run test` (Vitest) e `npm run build` (inclui checagem de tipos) passando.
2. Sessão no Chrome percorrendo: cadastro, login, logout, rota protegida sem sessão, lista vazia, criação com erros de validação vindos do front e do back, criação válida, filtro por status, avanço até entregue, cancelamento, transição inválida, token expirado/adulterado, layout no celular.
