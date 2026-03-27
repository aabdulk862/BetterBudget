# Gooder Budget — Project Improvement Overview

## What Is This Project?

Gooder Budget is an envelope budgeting application. Users create budget envelopes (e.g., "Groceries", "Entertainment"), allocate money into them, track spending via transactions, and transfer funds between envelopes. There are two roles: employees (regular users) who manage their own envelopes, and managers who can view and operate on any user's data.

The stack is Spring Boot 3.4.1 / Java 17 on the backend, React 18 / TypeScript / Vite on the frontend, with PostgreSQL for persistence and JWT-based stateless authentication.

## Application Flows

### Authentication Flow

1. User submits username + password to `POST /api/v1/users`
2. Backend validates credentials against BCrypt-hashed password in the database
3. On success, a JWT token is generated via `TokenProcessor` and returned alongside user info
4. Frontend stores the token in localStorage and attaches it as a `Bearer` header on all subsequent requests
5. `JWTAuthFilter` intercepts every request, extracts the token, authenticates via `JWTAuthProvider`, and populates the `SecurityContext`
6. New users register via `POST /api/v1/users/register` — password is validated (min 8 chars), BCrypt-encoded, and the user is persisted with `ROLE_EMPLOYEE` by default

### Envelope Management Flow

1. User creates an envelope via `POST /api/v1/envelopes` with a description, initial balance, and max limit
2. Envelopes are listed via `GET /api/v1/envelopes` (paginated) — employees see only their own (filtered by `userId` query param), managers see all
3. Individual envelope detail is fetched via `GET /api/v1/envelopes/{id}` — ownership is verified before returning data
4. Deleting an envelope via `DELETE /api/v1/envelopes/{id}` cascades to remove associated transactions and history

### Transaction Flow (Allocate / Spend / Transfer)

**Allocate money** (`POST /api/v1/envelopes/allocate/{envelopeId}`):

- Adds funds to an envelope
- Validates: amount > 0, new balance doesn't exceed maxLimit
- Creates a positive transaction record and an envelope history snapshot

**Spend money** (`POST /api/v1/envelopes/spend/{envelopeId}`):

- Deducts funds from an envelope
- Validates: amount > 0, sufficient balance
- Creates a negative transaction record (amount is negated for frontend display) and a history snapshot

**Transfer funds** (`POST /api/v1/envelopes/transfer`):

- Moves money between two envelopes owned by the same user
- Validates: both envelopes exist, user owns both, sufficient balance in source, destination won't exceed maxLimit
- Creates two transaction records (negative on source, positive on destination) and two history entries

### Authorization Flow

- Every envelope/transaction operation passes the authenticated username (from `SecurityContextHolder`) to the service layer
- `verifyOwnership()` compares the envelope's owner username against the authenticated user
- If they don't match, `AccessDeniedException` is thrown → `GlobalExceptionHandler` returns HTTP 403
- Users with `ROLE_MANAGER` bypass ownership checks entirely

### Frontend Data Flow

- Zustand store holds the logged-in user state (userId, username, role, token)
- Components fetch data using the token from the store, hitting the backend via `backendHost` (configured from `VITE_API_BASE_URL` env var)
- Each data-fetching component manages `isLoading` and `error` state — shows a spinner during loading, an alert with retry on failure
- `ErrorBoundary` wraps every route to catch rendering crashes gracefully

---

## Changes Made in the Project Improvement Spec

### 1. BigDecimal Migration for Financial Accuracy

**Problem:** All monetary fields (`balance`, `maxLimit`, `transactionAmount`, `envelopeAmount`) used `double`, which introduces floating-point rounding errors in financial calculations.

**Changes:**

- Migrated all monetary fields in `Envelope`, `Transaction`, and `EnvelopeHistory` entities from `double` to `BigDecimal` with `@Column(precision = 19, scale = 2)`
- Updated all DTOs (`EnvelopeDTO`, `TransferFundDTO`, `TransactionDTO`, `EnvelopeHistoryDTO`) to use `BigDecimal`
- Replaced all arithmetic operators (`+`, `-`, `<`, `>`) in `EnvelopeService` with `BigDecimal.add()`, `.subtract()`, `.compareTo()`, `.negate()`
- Updated all existing unit tests to use `BigDecimal` constructors and comparisons

### 2. Standardized Exception Handling

**Problem:** Services threw generic `RuntimeException` for business errors, and the `GlobalExceptionHandler` didn't cover all exception types consistently.

**Changes:**

- Replaced all `RuntimeException` throws in `EnvelopeService` and `TransactionService` with `BusinessException(code, message)` using descriptive error codes (1001 = envelope not found, 1002 = insufficient funds, 1003 = exceeds limit, 1004 = invalid amount, etc.)
- Expanded `GlobalExceptionHandler` with handlers for `BusinessException`, `MethodArgumentNotValidException`, `AccessDeniedException`, `UsernameNotFoundException`, and a catch-all `Exception` handler
- All error responses return structured JSON with consistent shapes

### 3. Service-Controller Decoupling

**Problem:** Service methods returned `ResponseEntity<?>`, mixing HTTP concerns into business logic.

**Changes:**

- Refactored all `EnvelopeService` methods to return domain objects (`Envelope`, `List<Envelope>`, `Transaction`, `void`)
- Moved all `ResponseEntity` construction into `EnvelopeController` with typed generics
- Applied the same pattern to `TransactionService` and `EnvelopeHistoryService`
- Converted inline validation checks that returned `ResponseEntity.badRequest()` into `BusinessException` throws

### 4. Lazy Fetching and N+1 Resolution

**Problem:** All JPA associations used `FetchType.EAGER`, loading entire object graphs on every query.

**Changes:**

- Switched all 8 JPA associations across `User`, `Envelope`, `Transaction`, and `EnvelopeHistory` from `EAGER` to `LAZY`
- Added `@EntityGraph(attributePaths = {"user"})` to `EnvelopeRepository.findById`, `findByUser_UserId`, and `findAll`
- Added `@EntityGraph(attributePaths = {"envelope"})` to `TransactionRepository.findByEnvelope_EnvelopeId`
- Added `@EntityGraph(attributePaths = {"envelope", "transaction"})` to `EnvelopeHistoryRepository.findByEnvelope_EnvelopeId`

### 5. Circular Dependency Resolution

**Problem:** `SecurityConfig` → `JWTAuthFilter` → `AuthenticationManager` → `SecurityConfig` created a circular dependency requiring `@Lazy`.

**Changes:**

- Changed `JWTAuthFilter` to accept `ObjectProvider<AuthenticationManager>` instead of direct injection
- Calls `authManagerProvider.getObject()` lazily inside `doFilterInternal`
- Removed `@Lazy` from `SecurityConfig` constructor

### 6. Secret Externalization and CORS Restriction

**Problem:** Database credentials, JWT key, and CORS origins were hardcoded in `application.properties` and source code.

**Changes:**

- Replaced all hardcoded values with environment variable placeholders: `${DB_URL}`, `${DB_USERNAME}`, `${DB_PASSWORD}`, `${JWT_PRIVATE_KEY}`, `${CORS_ALLOWED_ORIGINS}`
- Injected `cors.allowed-origins` as `String[]` in `SecurityConfig` and replaced `allowedOriginPatterns("*")` with `allowedOrigins(allowedOrigins)`
- Updated frontend `backendHost.tsx` to read from `import.meta.env.VITE_API_BASE_URL` with localhost fallback

### 7. Input Validation with Jakarta Bean Validation

**Problem:** No server-side validation on incoming DTOs — malformed data could reach business logic.

**Changes:**

- Added `spring-boot-starter-validation` dependency
- Added `@NotNull`, `@NotBlank`, `@Size`, `@DecimalMin`, `@Email` annotations to `EnvelopeDTO`, `TransferFundDTO`, `IncomingLogin`, and `User` entity
- Added `@Valid` to all controller method DTO parameters
- `MethodArgumentNotValidException` handler returns field-level error messages

### 8. Resource Ownership Authorization

**Problem:** Any authenticated user could access any other user's envelopes and transactions.

**Changes:**

- Added `verifyOwnership(Envelope, String authenticatedUsername)` to `EnvelopeService` — throws `AccessDeniedException` if the user doesn't own the envelope
- Manager role (`ROLE_MANAGER`) bypasses ownership checks via `SecurityContextHolder` authority inspection
- Applied ownership checks to `getEnvelopeById`, `deleteEnvelope`, `allocateMoney`, `spendMoney`, `transferEnvelope`
- Added ownership verification to `TransactionService.getTransactionsByEnvelopeId` with `EnvelopeRepository` injection
- Controllers pass `Authentication.getName()` to service methods

### 9. Pagination for List Endpoints

**Problem:** List endpoints returned all records at once, which doesn't scale.

**Changes:**

- Created `PaginatedResponse<T>` record with `content`, `totalElements`, `totalPages`, `currentPage`, `pageSize` and a `from(Page<T>)` factory method
- Updated `GET /envelopes`, `GET /transactions`, and `GET /envelopes/history` to accept `page` (default 0) and `size` (default 20) query parameters
- Service methods now accept `Pageable` and return `Page<T>`

### 10. REST API Design Fixes

**Problem:** Several endpoints didn't follow REST conventions.

**Changes:**

- Changed `DELETE /users` from `@RequestBody` to `DELETE /users/{username}` with `@PathVariable`
- Merged `GET /envelopes/user/{userId}` into `GET /envelopes?userId={userId}` as an optional query parameter
- Added `server.servlet.context-path=/api/v1` to prefix all endpoints
- Updated frontend base URL to include `/api/v1`
- Removed duplicate `/transactions` route in `App.tsx`

### 11. Frontend Error Boundaries

**Changes:**

- Created `ErrorBoundary` class component with `getDerivedStateFromError` and `componentDidCatch`
- Fallback UI shows an MUI `Alert` with retry button and "Go Home" link
- Wrapped all 9 route elements in `App.tsx` with `<ErrorBoundary>`

### 12. Frontend Loading and Error States

**Changes:**

- Added `isLoading` and `error` state to `EnvelopeList`, `DetailedEnvelope`, `AllTransactions`, and `SeeUsers`
- Shows MUI `CircularProgress` during loading
- Shows MUI `Alert` with error message and retry button on failure
- Extracted fetch functions for retry capability

### 13. Unified Frontend Styling

**Changes:**

- Created `_variables.scss` with all CSS custom properties (colors, spacing, border-radius, shadows)
- Removed duplicate `:root` blocks from 5 component SCSS files
- Converted `AddMoney.css` and `CreateEnvelope.css` to SCSS
- Created MUI `ThemeProvider` in `main.tsx` with green primary / purple secondary palette, 8px border-radius, Roboto typography, and button style overrides
- Extracted `_two-column-layout.scss` mixin used by Login, Register, Personalize, and CreateEnvelope
- Consolidated `button1`/`button2`/`button3`/`envButton1` into a single `.btn-primary` class
- Added `:focus-visible` outlines globally and via MUI theme overrides

### 14. Expanded Backend Test Coverage

**Changes:**

- Added `SecurityIntegrationTests` with 16 tests across 3 groups: unauthenticated access, wrong-user access, and manager bypass
- Verified existing `EnvelopeServiceTests` already covered transfer/allocate/spend success and failure scenarios (21 tests)
- Cleaned up unused imports and variables across all test files
- Added JaCoCo Maven plugin (v0.8.12) for coverage reports on `mvn verify`

### 15. OpenAPI Documentation

**Changes:**

- Added `springdoc-openapi-starter-webmvc-ui` 2.7.0 dependency
- Configured `springdoc.api-docs.path=/api-docs` and `springdoc.swagger-ui.path=/swagger-ui` (context path auto-prepends `/api/v1`)
- Permitted unauthenticated access to OpenAPI and Swagger UI paths in `SecurityConfig`

### 16. Docker Compose Orchestration

**Changes:**

- Created `docker-compose.yml` with three services: `db` (PostgreSQL 15 with named volume), `backend` (with all env vars), `frontend` (Nginx on port 3000)
- Updated `backend/Dockerfile` to multi-stage build (Maven build stage → JDK 17 runtime stage)
- Created `frontend/Dockerfile` (Node 18 build → Nginx alpine)
- Created `frontend/nginx.conf` with SPA-friendly `try_files` routing

### 17. Default User Accounts

**Changes:**

- Created `DataSeeder` component (`ApplicationRunner`) that seeds two default accounts on startup if they don't exist:
  - `admin` / `password` — `ROLE_MANAGER`
  - `user` / `password` — `ROLE_EMPLOYEE`
- Passwords are BCrypt-encoded; seeder is idempotent

### 18. Project README

**Changes:**

- Created `README.md` with project description, tech stack, prerequisites, quick start via `docker-compose up`, manual setup instructions, test commands, default accounts, API docs links, Mermaid architecture diagram, and project structure overview
