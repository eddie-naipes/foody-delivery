# Foody Delivery

Sistema simplificado de rastreamento de pedidos de delivery: usuários autenticados cadastram pedidos, acompanham a lista com o status atual e avançam cada pedido pelo fluxo de entrega.

| Parte | Stack | Pasta |
|---|---|---|
| API REST | Java 21 · Spring Boot 4.0 · Spring Security (JWT) · JPA/Hibernate · SQLite · MapStruct · Lombok | [`backend/`](backend) |
| Interface web | React 19 · TypeScript · Vite · TanStack Query · React Hook Form + Zod · Tailwind CSS | [`frontend/`](frontend) |

---

## Rodando tudo com Docker

O jeito mais rápido de ver o sistema completo. Sobem dois containers: `web` (Nginx servindo o front e repassando `/api`) e `api` (Spring Boot). Só o `web` fica exposto; a API é acessível apenas pela rede interna, e o front e a API compartilham a mesma origem, sem CORS.

```bash
cp .env.example .env              # defina JWT_SECRET (openssl rand -base64 48)
docker compose up -d --build      # aplicação em http://localhost
docker compose logs -f api        # acompanhar os logs da API
```

- O compose **não sobe sem `JWT_SECRET`** no `.env`, para evitar rodar em produção com a chave de desenvolvimento.
- O banco SQLite fica no volume `foody-data` e sobrevive a restarts e rebuilds.
- O `web` só inicia depois que a API está aceitando conexões (healthcheck), então ninguém vê erro 502 durante a subida.
- As duas imagens são construídas em dois estágios (só o necessário vai para produção); a API roda com usuário sem privilégios.
- Para usar outra porta: `WEB_PORT=8081` no `.env`.

### Deploy em uma VPS (ex.: Hostinger, Ubuntu)

1. Instale o Docker: `curl -fsSL https://get.docker.com | sh`
2. Clone o repositório e entre na pasta.
3. `cp .env.example .env` e preencha `JWT_SECRET`.
4. `docker compose up -d --build`
5. Para atualizar: `git pull && docker compose up -d --build` — os dados permanecem no volume.

> Para HTTPS, coloque um proxy reverso (Caddy ou Nginx + Let's Encrypt) na frente da porta do `web` e libere no firewall apenas as portas 80/443.

---

## Back-end

### Pré-requisitos

- **JDK 21 ou superior** (`java -version`). Maven não é necessário: o projeto inclui o Maven Wrapper (`mvnw`).

### Como rodar

```bash
cd backend
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run
```

A API sobe em `http://localhost:8080`. O banco SQLite (`backend/foody.db`) e as tabelas são criados automaticamente na primeira execução.

| Variável de ambiente | Padrão | Descrição |
|---|---|---|
| `JWT_SECRET` | chave de desenvolvimento | Chave HS256 em Base64 (mínimo 32 bytes). **Defina em produção.** |

Outras configurações ficam em [`application.yml`](backend/src/main/resources/application.yml): expiração do token (`app.jwt.expiration`, padrão `8h`) e origens CORS liberadas (`app.cors.allowed-origins`, padrão `http://localhost:5173`).

### Testes e cobertura

```bash
cd backend
./mvnw verify
```

Roda os testes unitários e de integração (MockMvc + SQLite em memória) e **falha o build se a cobertura de linhas ou branches ficar abaixo de 100%** (JaCoCo). Relatório: `backend/target/site/jacoco/index.html`.

### Testes de API (Postman/Newman)

A coleção em [`postman/`](postman) percorre o fluxo completo contra a API rodando — cadastro, login, criação do pedido, avanço de status até `ENTREGUE` — e cada cenário de erro (400, 401, 404, 409, 422), com 63 asserções. Pode ser importada no Postman ou executada pela linha de comando (requer Node.js):

```bash
# com a API rodando em http://localhost:8080 (./mvnw spring-boot:run)
npx newman run postman/foody-delivery.postman_collection.json -e postman/local.postman_environment.json

# ou contra o Docker Compose (passando pelo Nginx)
npx newman run postman/foody-delivery.postman_collection.json --env-var baseUrl=http://localhost
```

### Endpoints

Todas as rotas, exceto `/api/auth/**`, exigem o header `Authorization: Bearer <token>`.

| Método | Rota | Descrição | Sucesso |
|---|---|---|---|
| `POST` | `/api/auth/register` | Cadastra usuário (`name`, `email`, `password`) e já devolve o token | `201` |
| `POST` | `/api/auth/login` | Login com `email` e `password` | `200` |
| `POST` | `/api/orders` | Cria pedido (cliente, endereço, itens) | `201` + `Location` |
| `GET` | `/api/orders` | Lista todos os pedidos, mais recentes primeiro | `200` |
| `GET` | `/api/orders/{id}` | Busca pedido por id | `200` |
| `PATCH` | `/api/orders/{id}/status` | Atualiza o status (`{"status": "EM_PREPARO"}`) | `200` |

### Exemplo de uso

```bash
# 1. Cadastro (devolve o token)
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name": "Operador", "email": "op@foody.com", "password": "secret123"}'

# 2. Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "op@foody.com", "password": "secret123"}'

TOKEN="<token da resposta>"

# 3. Criar pedido
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" \
  -d '{
    "customerName": "Maria Silva",
    "customerPhone": "11999998888",
    "deliveryAddress": {
      "street": "Av. Paulista", "number": "1000", "complement": "Apto 12",
      "neighborhood": "Bela Vista", "city": "Sao Paulo", "zipCode": "01310-100"
    },
    "items": [
      { "name": "Pizza Margherita", "quantity": 2, "unitPrice": 45.00 },
      { "name": "Refrigerante", "quantity": 1, "unitPrice": 9.90 }
    ]
  }'

# 4. Avançar o status
curl -X PATCH http://localhost:8080/api/orders/1/status \
  -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" \
  -d '{"status": "EM_PREPARO"}'

# 5. Listar
curl http://localhost:8080/api/orders -H "Authorization: Bearer $TOKEN"
```

> **Windows (Git Bash/PowerShell):** acentos digitados direto no terminal podem não ser enviados em UTF-8, e a API responde `400 Corpo da requisição inválido`. Salve o JSON em um arquivo UTF-8 e envie com `--data-binary @pedido.json`.

### Fluxo de status

```
RECEBIDO → EM_PREPARO → SAIU_PARA_ENTREGA → ENTREGUE
    ↓           ↓               ↓
 CANCELADO   CANCELADO      CANCELADO
```

- Todo pedido nasce `RECEBIDO` (definido pelo servidor).
- Só é possível avançar para o próximo passo ou cancelar.
- `ENTREGUE` e `CANCELADO` são finais. Qualquer outra transição retorna `422`.

### Formato de erro

Todos os erros seguem o mesmo formato:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Dados inválidos",
  "fieldErrors": [{ "field": "items", "message": "O pedido deve ter ao menos um item" }],
  "timestamp": "2026-10-16T20:00:00Z"
}
```

| Situação | HTTP |
|---|---|
| Validação, JSON inválido, status desconhecido (a resposta lista os aceitos), id não numérico | `400` |
| Sem token, token inválido/expirado, credenciais inválidas | `401` |
| Pedido ou rota inexistente | `404` |
| E-mail já cadastrado | `409` |
| Transição de status não permitida | `422` |
| Erro inesperado (sem detalhes internos) | `500` |

### Decisões técnicas

- **Arquitetura MVC por camadas** (`controller → service → repository → model`): simples e familiar para o tamanho do projeto.
- **Regras de negócio no domínio**: a máquina de estados vive no enum `OrderStatus` e o total é calculado pelo `Order` (não é persistido, para nunca ficar inconsistente com os itens). O service só orquestra.
- **DTOs como `record`**: a API nunca expõe entidades JPA; a senha jamais aparece em respostas.
- **MapStruct** para converter entidades ↔ DTOs: código gerado em compilação (sem reflexão) e `unmappedTargetPolicy = ERROR`, então um campo esquecido quebra o build em vez de virar `null`.
- **Lombok com moderação**: `@Getter` e construtor protegido nas entidades (sem `@Data`/setters — o status só muda por `changeStatus`) e `@Slf4j`. Construtores com atribuições são escritos à mão: o Lombok embutido no VS Code (JDT) gera para eles um código que o editor rejeita como recurso do Java 25. O código gerado é marcado para não contar na cobertura.
- **Mensagens centralizadas** na classe `Messages`: todo texto devolvido pela API (inclusive de validação) é uma constante, reutilizada pelos testes.
- **Exceções de negócio** estendem `ApiException`, que carrega o status HTTP — um único handler as trata.
- **Logs de negócio** (cadastro, login, pedido criado, mudança de status, requisições rejeitadas) sem dados sensíveis: nunca senha, token ou dados pessoais do cliente.
- **Normalização na entrada**: e-mail em minúsculas e sem espaços, CEP sem hífen (`01310-100` → `01310100`), textos com trim — feita nos próprios records, antes da validação.
- **JWT stateless** (HS256, 8h, sem refresh token) com senhas em BCrypt.
- **Cliente como dado do pedido**: o usuário logado é o operador; o pedido registra quem o criou (`createdBy`).
- **Endereço `@Embeddable`**: colunas na própria tabela de pedidos, sem tabela extra.
- **Listagem sem N+1**: itens e criador carregados na mesma consulta (`@EntityGraph`).

### Estrutura

```
backend/src/main/java/com/foody/delivery
├── config/       SecurityConfig, ClockConfig
├── controller/   AuthController, OrderController
├── service/      AuthService, OrderService, JwtService
├── repository/   UserRepository, OrderRepository
├── model/        User, Order, OrderItem, Address, OrderStatus
├── dto/          request/ e response/ (records)
├── mapper/       OrderMapper, UserMapper (MapStruct)
├── security/     JwtAuthenticationFilter, JwtAuthenticationEntryPoint
├── exception/    GlobalExceptionHandler, ApiException e subclasses
├── validation/   @MaxBytes (limite de 72 bytes do BCrypt)
└── util/         Messages
```

### Próximos passos

- Histórico de mudanças de status (linha do tempo do pedido).
- Paginação e filtro por status na listagem.
- Refresh token.
- Migrações versionadas (Flyway).

---

## Front-end

Aplicação da expedição: o operador entra, vê a fila de pedidos com o status de cada um, cria pedidos e avança cada um pelas etapas.

### Como rodar

Requer **Node.js 20 ou superior** e a API rodando em `http://localhost:8080`.

```bash
cd frontend
npm install
npm run dev        # http://localhost:5173
```

O Vite repassa `/api` para a API (mesma origem também em desenvolvimento, sem CORS).

| Comando | O que faz |
|---|---|
| `npm run test` | testes (Vitest + Testing Library, com a API simulada pelo MSW) |
| `npm run build` | checagem de tipos + build de produção em `dist/` |
| `npm run coverage` | relatório de cobertura |

### Telas

| Rota | Tela |
|---|---|
| `/login`, `/cadastro` | Entrar e criar conta (entra direto após o cadastro) |
| `/pedidos` | Fila de pedidos em comandas, com contagem por status que também filtra; atualiza sozinha a cada 15 s |
| `/pedidos/novo` | Formulário com itens dinâmicos ao lado de uma comanda que se monta enquanto se digita |
| `/pedidos/:id` | Comanda completa, etapas do pedido e as ações possíveis (cancelar pede confirmação) |

### Decisões técnicas

- **Organização por funcionalidade** (`features/auth`, `features/orders`) com uma camada `shared` (cliente HTTP, componentes de interface, formatação) que não conhece nenhuma funcionalidade.
- **Dados do servidor com TanStack Query**: cache, estados de carregamento e erro, e invalidação após criar pedido ou mudar status. Chaves de cache centralizadas em `orderKeys`.
- **Mesmas regras do back-end no formulário** (Zod): CEP, senha de 6 a 72 bytes, preço com até 2 casas, ao menos um item. Os erros de campo que a API devolve (`items[0].quantity`, `deliveryAddress.zipCode`) caem no campo certo do formulário.
- **Fluxo de status espelhado** em `features/orders/status.ts`, testado contra a mesma tabela de transições do back. A tela mostra só as ações possíveis; se outra pessoa mudou o pedido antes (422), a tela recarrega o pedido e explica o que aconteceu.
- **Sessão**: token no `localStorage`; qualquer 401 em rota protegida encerra a sessão e volta ao login com aviso. Ao sair, o cache é limpo para que dados de um usuário não apareçam para o próximo.
- **Identidade da Foody Delivery**: fonte Sen, vermelho da marca, fundo bege quente, cartões arredondados e botões em pílula, seguindo o site e o app da empresa. Status sempre com cor e texto, foco de teclado visível e animação desligada para quem prefere movimento reduzido.
- **Entrada protegida**: telefone, CEP, quantidade e preço têm máscara enquanto se digita (nenhuma letra entra nesses campos), e a API aplica as mesmas regras.

