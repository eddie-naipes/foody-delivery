# Foody Delivery — Design do Back-end

**Data:** 2026-10-16
**Prazo de entrega do desafio:** 2026-10-21 23:59
**Escopo deste documento:** API REST (Spring Boot). O front-end (React) terá spec próprio.

---

## 1. Objetivo

Versão simplificada de um sistema de rastreamento de pedidos de delivery. Usuários autenticados (operadores do sistema) cadastram pedidos de clientes, listam/buscam pedidos e atualizam o status ao longo do fluxo de entrega.

Prioridade: clareza e organização para o avaliador. Primeiro entregamos o núcleo exigido pelo enunciado; extras entram depois, em commits isolados.

### Requisitos do enunciado

- Cadastro de usuário (nome, e-mail, senha) e login (e-mail + senha); apenas usuários autenticados acessam o sistema.
- Criar pedido (cliente, itens, endereço de entrega).
- Atualizar status: `RECEBIDO`, `EM_PREPARO`, `SAIU_PARA_ENTREGA`, `ENTREGUE`, `CANCELADO`.
- Listar todos os pedidos e buscar por ID.
- Persistência em SQLite (ou similar).
- Repositório Git com histórico de commits.

## 2. Decisões técnicas

| Item | Decisão |
|---|---|
| Repositório | Monorepo `foody-delivery-test/` com `backend/`, `frontend/`, `README.md` |
| Build | Maven + Maven Wrapper (`mvnw`) |
| Java | Target 21 (LTS) |
| Framework | Spring Boot 3.5.x |
| Banco | SQLite (arquivo `foody.db`) via JPA/Hibernate + `hibernate-community-dialects` |
| Esquema | `spring.jpa.hibernate.ddl-auto=update` (sem Flyway) |
| Autenticação | JWT stateless (jjwt, HS256, expiração 8h), sem refresh token; senha com BCrypt |
| Arquitetura | MVC por camada |
| Idioma do código | Inglês (classes, campos, rotas) |
| Valores de status | Exatamente como no enunciado (em português) |
| Mensagens de erro da API | Português |
| Commits | Conventional Commits, mensagens em português, um commit por etapa |

## 3. Estrutura de pacotes

Pacote base: `com.foody.delivery`

```
com.foody.delivery
├── FoodyDeliveryApplication
├── config/        SecurityConfig, CorsConfig
├── controller/    AuthController, OrderController
├── service/       AuthService, OrderService, JwtService
├── repository/    UserRepository, OrderRepository
├── model/         User, Order, OrderItem, Address, OrderStatus
├── dto/
│   ├── request/   RegisterRequest, LoginRequest, CreateOrderRequest,
│   │              OrderItemRequest, AddressRequest, UpdateStatusRequest
│   └── response/  AuthResponse, UserResponse, OrderResponse,
│                  OrderItemResponse, AddressResponse, ErrorResponse
├── security/      JwtAuthenticationFilter, JwtAuthenticationEntryPoint
└── exception/     GlobalExceptionHandler, ResourceNotFoundException,
                   EmailAlreadyInUseException, InvalidStatusTransitionException
```

DTOs são `record`s. Entidades JPA nunca são expostas diretamente pela API.

## 4. Modelo de dados

### `User` → tabela `users`

| Campo | Tipo | Regra |
|---|---|---|
| `id` | `Long` | PK, autoincremento |
| `name` | `String` | obrigatório, até 100 caracteres |
| `email` | `String` | obrigatório, único, normalizado para minúsculas |
| `password` | `String` | hash BCrypt |
| `createdAt` | `Instant` | automático |

Implementa `UserDetails` (username = e-mail).

### `Order` → tabela `orders`

| Campo | Tipo | Regra |
|---|---|---|
| `id` | `Long` | PK |
| `customerName` | `String` | obrigatório |
| `customerPhone` | `String` | obrigatório |
| `deliveryAddress` | `Address` | `@Embedded` |
| `items` | `List<OrderItem>` | `@OneToMany(mappedBy="order", cascade=ALL, orphanRemoval=true)` |
| `status` | `OrderStatus` | `@Enumerated(STRING)`, inicia em `RECEBIDO` (definido pelo servidor) |
| `createdBy` | `User` | `@ManyToOne`, obrigatório |
| `createdAt` / `updatedAt` | `Instant` | `@CreationTimestamp` / `@UpdateTimestamp` |

Métodos de domínio:
- `getTotalAmount()` — soma dos subtotais dos itens (calculado, não persistido).
- `changeStatus(OrderStatus next)` — valida via `OrderStatus.canTransitionTo` e lança `InvalidStatusTransitionException` se inválida.
- `addItem(OrderItem item)` — mantém os dois lados da associação.

### `OrderItem` → tabela `order_items`

| Campo | Tipo | Regra |
|---|---|---|
| `id` | `Long` | PK |
| `name` | `String` | obrigatório |
| `quantity` | `Integer` | ≥ 1 |
| `unitPrice` | `BigDecimal` | > 0, precisão (10,2) — snapshot do preço no momento do pedido |
| `order` | `Order` | `@ManyToOne`, obrigatório |

Método: `getSubtotal()` = `quantity × unitPrice`.

### `Address` (`@Embeddable`)

`street`, `number`, `complement` (opcional), `neighborhood`, `city`, `zipCode` (8 dígitos, armazenado sem hífen).

### `OrderStatus` (enum)

```
RECEBIDO → EM_PREPARO → SAIU_PARA_ENTREGA → ENTREGUE
    ↓           ↓               ↓
 CANCELADO   CANCELADO      CANCELADO
```

- Avança apenas para o próximo passo, ou para `CANCELADO` enquanto não for final.
- `ENTREGUE` e `CANCELADO` são finais.
- Transição para o mesmo status é inválida.
- Método: `canTransitionTo(OrderStatus next)`.

## 5. API

### Autenticação (pública)

| Método | Rota | Body | Sucesso | Erros |
|---|---|---|---|---|
| POST | `/api/auth/register` | `{ name, email, password }` | `201 { token, user: { id, name, email } }` | 400 validação, 409 e-mail em uso |
| POST | `/api/auth/login` | `{ email, password }` | `200 { token, user }` | 400 validação, 401 credenciais inválidas (mensagem genérica) |

### Pedidos (requer `Authorization: Bearer <token>`)

| Método | Rota | Body | Sucesso | Erros |
|---|---|---|---|---|
| POST | `/api/orders` | `{ customerName, customerPhone, deliveryAddress, items[] }` | `201` + header `Location` + pedido | 400, 401 |
| GET | `/api/orders` | — | `200` lista ordenada por `createdAt` desc | 401 |
| GET | `/api/orders/{id}` | — | `200` pedido | 401, 404 |
| PATCH | `/api/orders/{id}/status` | `{ status }` | `200` pedido atualizado | 400, 401, 404, 422 |

Sem paginação nem filtros (fora do escopo do núcleo).

### Formato do pedido (resposta)

```json
{
  "id": 1,
  "customerName": "Maria Silva",
  "customerPhone": "11999998888",
  "deliveryAddress": {
    "street": "Av. Paulista", "number": "1000", "complement": "Apto 12",
    "neighborhood": "Bela Vista", "city": "São Paulo", "zipCode": "01310100"
  },
  "items": [
    { "id": 1, "name": "Pizza Margherita", "quantity": 2, "unitPrice": 45.00, "subtotal": 90.00 }
  ],
  "totalAmount": 90.00,
  "status": "RECEBIDO",
  "createdBy": { "id": 1, "name": "Operador" },
  "createdAt": "2026-10-16T18:00:00Z",
  "updatedAt": "2026-10-16T18:00:00Z"
}
```

### Validações de entrada (Bean Validation)

- `name`, `customerName`, `customerPhone`, `street`, `number`, `neighborhood`, `city`, item `name`: `@NotBlank`.
- `email`: `@NotBlank @Email`.
- `password`: mínimo 6 caracteres.
- `items`: `@NotEmpty`, `@Valid` em cada item.
- `quantity`: `@NotNull @Min(1)`. `unitPrice`: `@NotNull @Positive`.
- `zipCode`: padrão `\d{5}-?\d{3}`; normalizado para 8 dígitos.
- `status`: `@NotNull`; valor fora do enum → 400.

## 6. Segurança

1. **Registro:** `AuthService` verifica e-mail duplicado → BCrypt na senha → salva `User` → gera token.
2. **Login:** `AuthenticationManager` valida credenciais → `JwtService` gera token.
3. **Requisições:** `JwtAuthenticationFilter` (`OncePerRequestFilter`) lê `Authorization: Bearer`, valida assinatura/expiração, carrega o usuário e popula o `SecurityContext`.
4. **Controller:** `@AuthenticationPrincipal User` fornece o usuário logado (usado em `createdBy`).

- JWT: HS256, `subject` = e-mail, `iat`, `exp` = 8h.
- Segredo: propriedade `app.jwt.secret`, lida da variável de ambiente `JWT_SECRET`, com valor padrão de desenvolvimento.
- `SecurityConfig`: sessão `STATELESS`, CSRF desabilitado, `/api/auth/**` liberado, demais rotas autenticadas.
- CORS: origem `http://localhost:5173` (configurável via `app.cors.allowed-origins`).
- `JwtAuthenticationEntryPoint`: 401 em JSON no formato padrão de erro.

## 7. Tratamento de erros

Formato único (`ErrorResponse`):

```json
{
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Transição de ENTREGUE para EM_PREPARO não permitida",
  "fieldErrors": [],
  "timestamp": "2026-10-16T18:00:00Z"
}
```

`fieldErrors`: lista de `{ field, message }`, preenchida apenas em erros de validação.

| Exceção | HTTP |
|---|---|
| `MethodArgumentNotValidException` | 400 (com `fieldErrors`) |
| `HttpMessageNotReadableException` (JSON malformado / enum inválido) | 400 |
| `BadCredentialsException` | 401 |
| Requisição sem token / token inválido (entry point) | 401 |
| `ResourceNotFoundException` | 404 |
| `EmailAlreadyInUseException` | 409 |
| `InvalidStatusTransitionException` | 422 |
| `Exception` (genérica) | 500, mensagem genérica, sem stack trace |

## 8. Testes

- Ferramentas: JUnit 5, Mockito, AssertJ, Spring Boot Test, MockMvc, Spring Security Test.
- **Meta: 100% de cobertura de linhas e branches**, medida e exigida pelo **JaCoCo** (`jacoco:check` no `mvn verify` — o build falha abaixo de 100%).
- Testes de integração usam perfil `test` com SQLite em memória (`application-test.yml`), sem tocar em `foody.db`.

| Teste | Tipo | Cobre |
|---|---|---|
| `OrderStatusTest` | unitário | Todas as combinações de transição |
| `OrderTest` / `OrderItemTest` | unitário | Total, subtotal, `changeStatus`, `addItem` |
| `JwtServiceTest` | unitário | Geração, validação, token expirado/adulterado |
| `AuthServiceTest` | unitário (Mockito) | Registro, e-mail duplicado, normalização, login |
| `OrderServiceTest` | unitário (Mockito) | Criação, listagem, busca, 404, atualização de status |
| `JwtAuthenticationFilterTest` | unitário | Sem header, header inválido, token válido |
| `GlobalExceptionHandlerTest` | unitário | Mapeamento de cada exceção |
| `AuthControllerIT` | integração | Registro → login → token, 400, 401, 409 |
| `OrderControllerIT` | integração | CRUD com token, 401, 400, 404, 422 |
| `FoodyDeliveryApplicationTests` | integração | Contexto sobe |

## 9. Versionamento

- Conventional Commits em português; um commit por etapa concluída, sempre compilando e com testes passando.
- Commits sem qualquer atribuição de autoria/coautoria de ferramentas de IA.
- Push para o GitHub feito pelo autor.

## 10. Fora do escopo (melhorias futuras)

- Histórico de mudanças de status (`StatusHistory`).
- Paginação e filtro por status na listagem.
- Refresh token.
- Catálogo de produtos / entidade de cliente.
- Migrações versionadas (Flyway).
