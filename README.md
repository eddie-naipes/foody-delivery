# Foody Delivery

Sistema simplificado de rastreamento de pedidos de delivery: usuários autenticados cadastram pedidos, acompanham a lista com o status atual e avançam cada pedido pelo fluxo de entrega.

| Parte | Stack | Pasta |
|---|---|---|
| API REST | Java 21 · Spring Boot 4.0 · Spring Security (JWT) · JPA/Hibernate · SQLite · MapStruct · Lombok | [`backend/`](backend) |
| Interface web | React | `frontend/` *(em construção)* |

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
# com a API rodando em http://localhost:8080
npx newman run postman/foody-delivery.postman_collection.json -e postman/local.postman_environment.json
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
- **Lombok com moderação**: `@Getter` e construtor protegido nas entidades (sem `@Data`/setters — o status só muda por `changeStatus`), `@RequiredArgsConstructor` na injeção e `@Slf4j`. O código gerado é marcado para não contar na cobertura.
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
