# AutoSpec Intelligence — Backend

> API RESTful de inteligência competitiva automotiva desenvolvida em **Java 17 + Spring Boot 3.4.1**, com segurança enterprise, cache distribuído, geração de specs via IA e exportação em PDF.

🌐 **API em produção:** `https://18.216.83.72:8443`  
📖 **Swagger UI:** [https://18.216.83.72:8443/swagger-ui/index.html](https://18.216.83.72:8443/swagger-ui/index.html)  
🖥️ **Frontend:** [autospec-mobile.vercel.app](https://autospec-mobile.vercel.app)  
📦 **Repositório:** [github.com/Brunootavioliveira/autospec-intelligence-platform](https://github.com/Brunootavioliveira/autospec-intelligence-platform)  
☁️ **Infraestrutura:** AWS EC2 t3.micro (Ubuntu 26.04) + Docker Compose

---

## Índice

- [Visão Geral](#visão-geral)
- [Arquitetura](#arquitetura)
- [Stack Tecnológica](#stack-tecnológica)
- [Estrutura de Pacotes](#estrutura-de-pacotes)
- [Módulos](#módulos)
- [Segurança](#segurança)
- [Cache e Performance](#cache-e-performance)
- [Banco de Dados](#banco-de-dados)
- [Infraestrutura Docker](#infraestrutura-docker)
- [Variáveis de Ambiente](#variáveis-de-ambiente)
- [Como Rodar Localmente](#como-rodar-localmente)
- [Deploy na AWS EC2](#deploy-na-aws-ec2)
- [Endpoints da API](#endpoints-da-api)
- [Papéis e Permissões (RBAC)](#papéis-e-permissões-rbac)
- [Padrões e Decisões Técnicas](#padrões-e-decisões-técnicas)

---

## Visão Geral

O backend do AutoSpec Intelligence expõe uma API RESTful que orquestra:

- **Geração de specs via IA** — delega para um microserviço Python/FastAPI que usa o modelo Gemini para extrair especificações técnicas de qualquer veículo (marca, modelo, versão, ano)
- **Cache inteligente** — Redis evita chamadas desnecessárias à IA para veículos já consultados
- **Comparação técnica** — score automático por atributo com vencedor calculado matematicamente
- **Análise avançada** — power-to-weight ratio, Track Handling Score e percentis populacionais
- **Relatórios em PDF** — Comparison Report e Vehicle Dossier gerados com OpenPDF
- **Garage pessoal** — frota do analista com insights automáticos e soft delete
- **Auditoria completa** — rastreamento de todas as ações críticas com usuário e timestamp

---

## Arquitetura

```
                    ┌─────────────────────────────────────┐
                    │           AWS EC2 (Docker)           │
                    │                                      │
  HTTPS ─────────▶  │  ┌─────────┐                        │
  :8443             │  │  Nginx  │ TLS 1.2/1.3            │
                    │  │ Reverse │ HTTP→HTTPS redirect     │
                    │  │  Proxy  │                        │
                    │  └────┬────┘                        │
                    │       │ :8080 (interno)              │
                    │  ┌────▼──────────────────────────┐  │
                    │  │     Spring Boot Backend        │  │
                    │  │                               │  │
                    │  │  ┌──────────────────────────┐ │  │
                    │  │  │  Filter Chain            │ │  │
                    │  │  │  HmacFilter              │ │  │
                    │  │  │  RateLimitFilter         │ │  │
                    │  │  │  JwtFilter               │ │  │
                    │  │  └──────────────────────────┘ │  │
                    │  │                               │  │
                    │  │  Controllers → Services       │  │
                    │  │  MapStruct → DTOs             │  │
                    │  │  JPA Auditing                 │  │
                    │  └──┬──────────────┬─────────────┘  │
                    │     │              │                 │
                    │  ┌──▼──┐      ┌───▼───┐             │
                    │  │Redis│      │  PG   │             │
                    │  │Cache│      │  :5432│             │
                    │  └─────┘      └───────┘             │
                    │                    │                 │
                    │  ┌─────────────────▼───────────┐    │
                    │  │  AI Service (FastAPI/Python) │    │
                    │  │  Gemini API integration      │    │
                    │  │  :5000 (interno)             │    │
                    │  └─────────────────────────────┘    │
                    └─────────────────────────────────────┘
```

### Fluxo de uma requisição autenticada

```
Cliente → Nginx (TLS) → HmacFilter → RateLimitFilter → JwtFilter
       → SecurityConfig (RBAC) → Controller → Service → Repository
       → PostgreSQL / Redis / AI Service
```

---

## Stack Tecnológica

| Camada | Tecnologia | Versão |
|---|---|---|
| Linguagem | Java | 17 |
| Framework | Spring Boot | 3.4.1 |
| Segurança | Spring Security | 6.4.2 |
| Persistência | Spring Data JPA + Hibernate | 6.6 |
| Banco | PostgreSQL | 15 |
| Cache | Redis + Spring Cache | 7-alpine |
| Migrations | Flyway | 10.x |
| Mapeamento | MapStruct | 1.5.5 |
| JWT | jjwt | 0.12.6 |
| Rate Limiting | Bucket4j | 8.10.1 |
| PDF | OpenPDF | 2.0.3 |
| HTTP Client | WebClient (WebFlux) | — |
| Documentação | springdoc-openapi | 2.8.6 |
| Boilerplate | Lombok | — |
| Build | Maven | 3.x |
| Runtime | Docker + Nginx | — |
| Cloud | AWS EC2 t3.micro | Ubuntu 26.04 |

---

## Estrutura de Pacotes

```
br.com.autospec.backend
│
├── BackendApplication.java           (@SpringBootApplication + @EnableCaching
│                                      + @EnableJpaAuditing + @EnableScheduling)
│
├── config/
│   ├── AppConfig.java                (WebClient Bean com timeout de 10s)
│   ├── AuditorAwareImpl.java         (popula createdBy/modifiedBy via SecurityContext)
│   ├── CorsConfig.java               (origens permitidas, sem wildcard)
│   └── OpenApiConfig.java            (Swagger bearerAuth scheme)
│
├── core/
│   ├── common/
│   │   ├── Auditable.java            (@MappedSuperclass com 4 campos de auditoria)
│   │   ├── DataRetentionJob.java     (@Scheduled semanal — limpa specs antigas)
│   │   └── ErrorResponseDTO.java     (resposta padronizada de erro)
│   ├── exception/
│   │   ├── BusinessException.java    (400 — regra de negócio violada)
│   │   ├── CryptoException.java      (500 — falha de criptografia)
│   │   └── ResourceNotFoundException (404 — recurso não encontrado)
│   ├── handler/
│   │   └── GlobalExceptionHandler   (@RestControllerAdvice — 7 handlers)
│   ├── hmac/
│   │   ├── HmacFilter.java          (valida X-Signature + anti-replay 5min)
│   │   └── HmacUtil.java            (geração HMAC-SHA256)
│   ├── http/
│   │   └── CachedBodyRequestWrapper (permite reler o body do request)
│   └── security/
│       ├── AuthConfig.java           (DaoAuthenticationProvider + AuthManager)
│       ├── CryptoConverter.java      (AES/GCM/NoPadding + IV aleatório)
│       ├── PasswordConfig.java       (BCryptPasswordEncoder bean)
│       └── SecurityConfig.java       (FilterChain + RBAC + STATELESS)
│
├── infrastructure/
│   └── HealthCheckController.java
│
└── modules/
    ├── analysis/                     (VehicleAnalysisService — métricas derivadas)
    ├── auth/                         (JWT, refresh token, rate limit, sessões)
    ├── comparison/                   (comparações salvas pelo usuário)
    ├── garage/                       (frota pessoal/profissional)
    ├── history/                      (auditoria de ações do usuário)
    ├── report/                       (geração de PDFs)
    ├── user/                         (perfil, senha, sessões, admin)
    └── vehicle/                      (CRUD + geração via IA + comparação técnica)
```

---

## Módulos

### Auth
Gerencia todo o ciclo de autenticação:

- **Registro** → cria usuário com `Role.VIEWER` por padrão, retorna `accessToken` + `refreshToken`
- **Login** → valida credenciais via `AuthenticationManager`, gera novo par de tokens
- **Refresh** → valida o refresh token, faz rotação (token antigo é deletado, novo é criado), retorna novo par
- **Logout** → invalida o refresh token no banco — acesso com o access token expirado é automaticamente rejeitado

**Entidades:** `User`, `RefreshToken`, `UserSession`

**Segurança extra:**
- Refresh token com expiração de 7 dias
- Rotação automática a cada refresh
- `UserSession` rastreia IP, browser, dispositivo e `lastActive`

### Vehicle
Core do sistema:

- **Geração via IA** — verifica cache Redis → verifica banco → chama AI Service → persiste → cacheia
- **Busca textual** com filtros opcionais: brand, minYear, maxYear, minHp, maxHp
- **Comparação por ID** — score automático por atributo com vencedor
- **Comparação por spec** — dois objetos `VehicleRequestDTO` comparados diretamente

**Entidade `VehicleSpec`:** brand, model, version, year, engine, horsepower, torque, drivetrain, topSpeed, acceleration, length, width, height, weight, electricRange, price + campos de auditoria herdados de `Auditable`

### Analysis
Calcula métricas derivadas para um veículo:

- **Power-to-Weight Ratio** (kg/kW)
- **Track Handling Score** (combinação de PTW e aceleração)
- **Percentis populacionais** — onde o veículo está em relação a todos os outros no banco (HP, top speed, aceleração)

### Garage
Frota pessoal/profissional do analista:

- Tipos de frota: `PERSONAL` e `WORK`
- Apelido (`nickname`) customizável por veículo
- Insights automáticos: total de veículos, veículo mais potente
- **Soft delete** — veículo removido fica com `active = false`, preservando o histórico
- Registro automático em `UserHistory` ao adicionar veículo

### History
Auditoria de ações do usuário:

- Tipos: `ANALYSIS`, `COMPARISON`, `SERVICE_RECORD`
- Filtro por tipo via query param
- Soft delete individual ou limpeza total
- Paginação configurável

### Comparison
Comparações salvas pelo usuário com título customizado. Vinculadas ao usuário via `@AuthenticationPrincipal`.

### Report
Geração de PDFs com OpenPDF:

- **Comparison Report** — comparação completa entre dois veículos com parâmetros selecionáveis (`ENGINE`, `PERFORMANCE`, `PRICE`, `SAFETY`, `DIMENSIONS`)
- **Vehicle Dossier** — dossiê completo de um veículo
- PDFs armazenados em `ConcurrentHashMap` em memória; expiram após o primeiro download
- Whitelist de parâmetros com validação explícita

### User
Perfil e gerenciamento de segurança:

- Atualização de nome e senha
- Listagem de sessões ativas com IP, browser e device
- Revogação de sessão individual ou de todas as outras
- **Admin:** listagem de todos os usuários e alteração de roles em tempo real

---

## Segurança

### Filter Chain

```
Request → HmacFilter → RateLimitFilter → JwtFilter → Controller
```

#### HmacFilter
Protege endpoints críticos com assinatura HMAC-SHA256:

```java
// Endpoints protegidos
POST /api/v1/auth/refresh

// Headers obrigatórios
X-Signature: HMAC-SHA256(normalizedBody + timestamp, secret)
X-Timestamp: System.currentTimeMillis()
```

Proteção anti-replay: rejeita requisições com timestamp fora de uma janela de **5 minutos**.

#### RateLimitFilter (Bucket4j)

| Contexto | Chave | Limite |
|---|---|---|
| `POST /auth/login` | IP do cliente | 5 req/min |
| `POST /auth/refresh` | IP do cliente | 10 req/min |
| `/api/v1/vehicles/spec/**` | Username autenticado | 10 req/min |

Retorna `429 Too Many Requests` com JSON padronizado ao ultrapassar o limite.

#### JwtFilter
- Extrai o token do header `Authorization: Bearer <token>`
- Valida assinatura e expiração via `JwtService`
- Carrega o usuário do banco e registra no `SecurityContextHolder`

### RBAC

```java
// SecurityConfig
.requestMatchers(HttpMethod.DELETE, "/api/v1/**").hasRole("ADMIN")
.requestMatchers(HttpMethod.POST, "/api/v1/vehicles/**").hasAnyRole("ANALYST", "ADMIN")
.anyRequest().authenticated()
```

| Ação | VIEWER | ANALYST | ADMIN |
|---|:---:|:---:|:---:|
| Consultar / buscar specs | ✅ | ✅ | ✅ |
| Comparar e analisar | ✅ | ✅ | ✅ |
| Gerar spec com IA | ❌ | ✅ | ✅ |
| Gerar relatórios PDF | ❌ | ✅ | ✅ |
| Deletar specs | ❌ | ❌ | ✅ |
| Gerenciar usuários | ❌ | ❌ | ✅ |

### Criptografia em Repouso
Campos sensíveis do `User` (name, email) são criptografados com **AES/GCM/NoPadding**:

```java
// CryptoConverter.java
private static final String ALGORITHM  = "AES/GCM/NoPadding";
private static final int    IV_SIZE    = 12;   // bytes
private static final int    TAG_LENGTH = 128;  // bits GCM tag

// IV aleatório gerado por SecureRandom para cada valor
// IV é prefixado no Base64 armazenado: [IV (12 bytes)] + [ciphertext]
```

### GlobalExceptionHandler
7 handlers que garantem que nenhuma informação interna vaze para o cliente:

| Exceção | HTTP | Mensagem ao cliente |
|---|---|---|
| `ResourceNotFoundException` | 404 | Mensagem do domínio |
| `BusinessException` | 400 | Mensagem do domínio |
| `MethodArgumentNotValidException` | 400 | `campo:mensagem` por campo |
| `IllegalArgumentException` | 400 | Mensagem do domínio |
| `AccessDeniedException` | 403 | "Acesso negado" |
| `AuthenticationException` | 401 | "Invalid email or password" |
| `HttpMessageNotReadableException` | 400 | "JSON malformado" |
| `CryptoException` | 500 | "Erro interno de processamento" |
| `Exception` | 500 | "Erro interno." (sem detalhes) |

---

## Cache e Performance

### Estratégia Redis
```java
// VehicleService.java
@Cacheable(value = "vehicle-specs-by-key",
    key = "#request.brand() + '-' + #request.model() + '-' + #request.version() + '-' + #request.year()")
public VehicleResponseDTO generateVehicleSpec(VehicleRequestDTO request) { ... }

@Cacheable(value = "vehicle-specs-by-id", key = "#id")
public VehicleResponseDTO findById(Long id) { ... }

@CacheEvict(value = "vehicle-specs-by-id", key = "#id")
public void delete(Long id) { ... }
```

**TTL configurado:** 1 hora (3.600.000 ms)

### Fluxo de geração com cache

```
POST /vehicles/spec
  ├─▶ Redis (vehicle-specs-by-key)? → retorna imediatamente
  ├─▶ PostgreSQL (findByBrandAndModelAndVersionAndYear)? → salva no Redis + retorna
  └─▶ AI Service → salva no PG → salva no Redis → retorna
```

### Retenção de Dados
`DataRetentionJob` executa todo domingo à meia-noite:

```java
@Scheduled(cron = "0 0 0 * * SUN")
@Transactional
public void runDataCleanup() {
    // Deleta specs com mais de retentionMonths (padrão: 6)
    int deleted = vehicleSpecRepository.deleteSpecsOlderThan(cutoffDate);
    log.info("Total de especificações removidas: {}", deleted);
}
```

---

## Banco de Dados

### Entidades principais

```
users
├── id, name (AES-GCM), email (unique), password (BCrypt), role
├── created_by, created_at, last_modified_by, last_modified_date
└── [Auditable]

vehicle_specs
├── id, brand, model, version, year (unique constraint: brand+model+version+year)
├── engine, horsepower, torque, drivetrain, topSpeed, acceleration
├── length, width, height, weight, electricRange, price
└── [Auditable]

refresh_tokens
└── id, token (UUID), user_id, expiresAt

user_sessions
└── id, user_id, sessionToken, deviceInfo, ipAddress, browserApp, lastActive, active

garage_vehicles
├── id, user_id, vehicle_spec_id, fleetType (PERSONAL|WORK), nickname, active
└── [Auditable]

user_history
└── id, user_id, actionType (ANALYSIS|COMPARISON|SERVICE_RECORD), title, description, referenceId, deleted

saved_comparisons
├── id, user_id, vehicle_a_id, vehicle_b_id, title
└── [Auditable]
```

### JPA Auditing
```java
// AuditorAwareImpl.java
@Override
public Optional<String> getCurrentAuditor() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || "anonymousUser".equals(auth.getPrincipal()))
        return Optional.of("SYSTEM");
    return Optional.of(auth.getName()); // email do usuário autenticado
}
```

---

## Infraestrutura Docker

```yaml
# docker-compose.yml
services:
  nginx:      # TLS 1.2/1.3, redirect HTTP→HTTPS, reverse proxy
  backend:    # Spring Boot, 512MB max, logs rotacionados (10MB x 3)
  ai-service: # FastAPI/Python, 512MB max, Gemini API
  postgres:   # PostgreSQL 15, volume persistente
  redis:      # Redis 7 Alpine, cache TTL 1h
```

### Configurações de produção notáveis

```yaml
# Backend
environment:
  JAVA_OPTS: "-Xmx384m -Xms256m"  # otimizado para t3.micro (1GB RAM)
  SPRING_PROFILES_ACTIVE: prod

logging:
  driver: "json-file"
  options:
    max-size: "10m"
    max-file: "3"      # máx 30MB de logs por container
```

### Nginx

```nginx
ssl_protocols TLSv1.2 TLSv1.3;          # apenas protocolos modernos
proxy_pass http://backend:8080;           # rede interna Docker
# HTTP → HTTPS redirect 301 automático
```

O backend **não expõe porta externamente** — acessível apenas via Nginx na rede interna Docker.

---

## Variáveis de Ambiente

### `infra/docker/.env`

```env
# PostgreSQL
POSTGRES_DB=autospec_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=senha-forte-aqui

# JWT
JWT_SECRET=chave-minimo-32-caracteres-aqui
JWT_EXPIRATION=86400000        # 24 horas em ms

# HMAC (deve ser igual ao VITE_HMAC_SECRET do frontend)
HMAC_SECRET=chave-hmac-aqui

# Criptografia AES-GCM (exatamente 32 bytes)
DB_CRYPTO_KEY=chave-32-bytes-exata-aqui

# CORS
FRONTEND_URL=https://autospec-mobile.vercel.app
```

### `application-prod.yaml`

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate    # Flyway controla o schema em produção
    show-sql: false         # sem queries nos logs de produção
  flyway:
    enabled: true
    baseline-on-migrate: true
  cache:
    type: redis
    redis:
      time-to-live: 3600000  # 1 hora

app:
  retention:
    specs-months: 6          # specs com mais de 6 meses são deletadas
    history-months: 12
    sessions-days: 30
```

---

## Como Rodar Localmente

### Pré-requisitos
- Java 17+
- Maven 3.8+
- Docker e Docker Compose

### 1. Clone o repositório
```bash
git clone https://github.com/Brunootavioliveira/autospec-intelligence-platform.git
cd autospec-intelligence-platform
```

### 2. Configure as variáveis de ambiente
```bash
cp infra/docker/.env.example infra/docker/.env
# edite com seus valores
```

### 3. Crie o `application-dev.yaml`
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
  flyway:
    enabled: false
```

### 4. Gere os certificados SSL
```bash
mkdir -p infra/docker/nginx/certs
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout infra/docker/nginx/certs/key.pem \
  -out infra/docker/nginx/certs/cert.pem \
  -subj "/CN=localhost"
```

### 5. Suba a infraestrutura
```bash
cd infra/docker
docker compose up -d postgres redis ai-service nginx
```

### 6. Rode o backend na IDE
```
Perfil: dev
Porta: 8080
```

### 7. Acesse o Swagger
```
https://localhost:8443/swagger-ui/index.html
```

---

## Deploy na AWS EC2

```bash
# 1. Conectar na EC2
ssh -i ~/.ssh/autospec-key.pem ubuntu@SEU_IP

# 2. Instalar Docker
sudo apt update && sudo apt install -y docker.io docker-compose-plugin
sudo usermod -aG docker ubuntu
# reconectar SSH para aplicar o grupo

# 3. Clonar o projeto
git clone https://github.com/Brunootavioliveira/autospec-intelligence-platform.git
cd autospec-intelligence-platform/infra/docker

# 4. Configurar variáveis
nano .env

# 5. Gerar certificados SSL
mkdir -p nginx/certs
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout nginx/certs/key.pem \
  -out nginx/certs/cert.pem \
  -subj "/CN=SEU_IP"

# 6. Subir todos os containers
sudo docker compose up -d --build

# 7. Verificar
sudo docker ps
curl -k https://localhost:8443/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"seu@email.com","password":"suasenha"}'
```

### Comandos úteis em produção

```bash
# Ver logs do backend em tempo real
sudo docker logs autospec-backend -f

# Reiniciar só o backend após mudança
sudo docker compose up -d --build backend

# Acessar o banco de dados
sudo docker exec -it autospec-postgres psql -U postgres -d autospec_db

# Ver uso de memória dos containers
sudo docker stats --no-stream
```

---

## Endpoints da API

Documentação interativa completa disponível no Swagger:  
`https://18.216.83.72:8443/swagger-ui/index.html`

### Authentication — `/api/v1/auth`

| Método | Rota | Auth | HMAC | Descrição |
|---|---|:---:|:---:|---|
| POST | `/register` | ❌ | ❌ | Criar conta (role padrão: VIEWER) |
| POST | `/login` | ❌ | ❌ | Login — retorna accessToken + refreshToken |
| POST | `/refresh` | ❌ | ✅ | Renovar tokens (rotação automática) |
| POST | `/logout` | ❌ | ❌ | Invalidar refresh token |

### Vehicle Specs — `/api/v1/vehicles/spec`

| Método | Rota | Role | HMAC | Descrição |
|---|---|:---:|:---:|---|
| POST | `/` | ANALYST, ADMIN | ✅ | Gerar spec com IA |
| GET | `/` | Todos | ❌ | Listar paginado |
| GET | `/{id}` | Todos | ❌ | Buscar por ID |
| GET | `/search` | Todos | ❌ | Busca textual + filtros |
| GET | `/compare` | Todos | ❌ | Comparar por IDs |
| POST | `/compare` | Todos | ❌ | Comparar por spec |
| DELETE | `/{id}` | ADMIN | ❌ | Deletar |

### Analysis — `/api/v1/analysis`

| Método | Rota | Descrição |
|---|---|---|
| GET | `/{vehicleId}` | Power-to-weight, Track Handling Score, percentis |

### Garage — `/api/v1/garage`

| Método | Rota | Descrição |
|---|---|---|
| POST | `/` | Adicionar veículo à frota |
| GET | `/` | Listar frota ativa |
| GET | `/fleet/{fleetType}` | Filtrar por PERSONAL ou WORK |
| GET | `/insights` | Total, mais potente |
| PATCH | `/{id}` | Atualizar fleet type ou nickname |
| DELETE | `/{id}` | Soft delete |

### History — `/api/v1/history`

| Método | Rota | Descrição |
|---|---|---|
| GET | `/` | Histórico paginado com filtro por tipo |
| DELETE | `/{id}` | Soft delete de entrada |
| DELETE | `/` | Limpar tudo |

### Saved Comparisons — `/api/v1/comparisons/saved`

| Método | Rota | Descrição |
|---|---|---|
| POST | `/` | Salvar comparação |
| GET | `/` | Listar paginado |
| DELETE | `/{id}` | Remover |

### Reports — `/api/v1/reports`

| Método | Rota | Descrição |
|---|---|---|
| POST | `/comparison` | Gerar Comparison Report PDF |
| POST | `/dossier` | Gerar Vehicle Dossier PDF |
| GET | `/{reportId}/download` | Download do PDF (expira após 1 uso) |

### Users — `/api/v1/users`

| Método | Rota | Descrição |
|---|---|---|
| GET | `/me` | Perfil do usuário |
| PATCH | `/me` | Atualizar nome |
| PATCH | `/me/password` | Alterar senha |
| GET | `/me/sessions` | Sessões ativas |
| DELETE | `/me/sessions/{id}` | Revogar sessão |
| DELETE | `/me/sessions` | Revogar todas as outras |
| GET | `/` | Listar todos (ADMIN) |
| PATCH | `/{id}/role` | Alterar role (ADMIN) |

---

## Papéis e Permissões (RBAC)

```
VIEWER  → leitura de todas as specs, análises e comparações
ANALYST → VIEWER + geração de specs via IA + relatórios PDF
ADMIN   → ANALYST + delete de specs + gestão de usuários
```

Novos usuários são registrados automaticamente como `VIEWER`. Um ADMIN pode promover qualquer usuário via `PATCH /api/v1/users/{id}/role`.

---

## Padrões e Decisões Técnicas

### Arquitetura Modular
O projeto usa uma arquitetura modular por domínio (`modules/auth`, `modules/vehicle`, etc.) em vez da separação clássica por camada (`controllers/`, `services/`, `repositories/`). Isso melhora a coesão e facilita a evolução independente de cada módulo.

### Sessão STATELESS
Nenhum estado é armazenado no servidor. Cada requisição é autenticada pelo JWT — o servidor nunca guarda sessões em memória. Isso permite escalar horizontalmente sem sincronização.

### WebClient em vez de RestTemplate
A comunicação com o AI Service usa `WebClient` (Spring WebFlux), mais moderno e com suporte a timeout configurável:
```java
// AppConfig.java
WebClient.builder()
    .baseUrl(aiServiceUrl)
    .clientConnector(new ReactorClientHttpConnector(
        HttpClient.create().responseTimeout(Duration.ofSeconds(10))
    ))
    .build();
```

### MapStruct para mapeamento
Zero reflexão em runtime. O MapStruct gera o código de mapeamento em tempo de compilação, sendo mais performático que frameworks como ModelMapper.

### Soft Delete na Garage e History
Registros removidos ficam com `active = false` ou `deleted = true`. Isso preserva o histórico e permite auditoria futura sem perder dados.

### Whitelist de parâmetros no Report
```java
private static final Set<String> ALLOWED_PARAMS =
    Set.of("ENGINE", "PERFORMANCE", "PRICE", "SAFETY", "DIMENSIONS");
```
Proteção contra injeção de parâmetros inválidos no gerador de PDF.

---

## Autores

Desenvolvido como projeto acadêmico para o **Projeto FORD** — Engenharia de Software.

Bruno Otavio Silva De Oliveira RM556196

Guilherme Flores Pereira de Almeida RM554948

Luiz Fernando de Aragão Souza RM555561

Bruno Otavio Silva De Oliveira RM556196

Marcello de Freitas Moreira RM557531

Leonardo Gonçalves Novaes RM554807

---

*AutoSpec Intelligence Backend — Java 17 + Spring Boot 3.4.1 + AWS EC2*
