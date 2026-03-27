# Implementation Plan: Project Improvement

## Overview

Incremental improvement of the Gooder Budget application across security, backend architecture, frontend resilience, and DevOps. Tasks are ordered so foundational changes (BigDecimal, exception handling, service decoupling) land first, followed by dependent features (pagination, ownership, REST fixes), then frontend, and finally DevOps/docs.

## Tasks

- [x] 1. BigDecimal migration for monetary fields
  - [x] 1.1 Migrate entity fields from double to BigDecimal
    - Change `Envelope.balance` and `Envelope.maxLimit` from `double` to `BigDecimal` with `@Column(precision = 19, scale = 2)`
    - Change `Transaction.transactionAmount` from `double` to `BigDecimal` with `@Column(precision = 19, scale = 2)`
    - Change `EnvelopeHistory.envelopeAmount` from `double` to `BigDecimal` with `@Column(precision = 19, scale = 2)`
    - Update constructors, getters, setters, and `toString()` methods accordingly
    - _Requirements: 5.1, 5.2, 5.3_

  - [x] 1.2 Migrate DTOs to BigDecimal
    - Update `EnvelopeDTO` record fields `balance` and `maxLimit` from `Double` to `BigDecimal`
    - Update `TransferFundDTO` record field `amount` from `Double` to `BigDecimal`
    - Update `TransactionDTO` field `transactionAmount` from `double` to `BigDecimal`
    - Update `EnvelopeHistoryDTO` field `envelopeAmount` from `double` to `BigDecimal`
    - _Requirements: 5.4_

  - [x] 1.3 Refactor service arithmetic to use BigDecimal methods
    - Replace all `+`, `-`, `<`, `>` operators on monetary values in `EnvelopeService` with `BigDecimal.add()`, `BigDecimal.subtract()`, `BigDecimal.compareTo()`
    - Apply same changes in `TransactionService` and any other service touching monetary values
    - Use `RoundingMode.HALF_UP` for any division operations
    - _Requirements: 5.5, 5.6_

  - [ ]\* 1.4 Write property test for BigDecimal scale invariant
    - **Property 9: BigDecimal Scale Invariant**
    - Add jqwik dependency to `pom.xml`
    - Generate random monetary values, persist Envelope/Transaction/EnvelopeHistory entities, verify all monetary fields have scale == 2
    - **Validates: Requirements 5.1, 5.2, 5.3**

  - [ ]\* 1.5 Write property test for balance-transaction sum invariant
    - **Property 10: Balance-Transaction Sum Invariant**
    - Generate a sequence of allocate/spend operations on an Envelope, verify `currentBalance == initialBalance + sum(transactionAmounts)`
    - **Validates: Requirements 5.6**

- [x] 2. Standardize exception handling
  - [x] 2.1 Replace RuntimeException with BusinessException in services
    - Replace all `throw new RuntimeException(...)` in `EnvelopeService` with `throw new BusinessException(code, message)` using appropriate error codes
    - Replace all `throw new RuntimeException(...)` in `TransactionService` with `throw new BusinessException(code, message)`
    - _Requirements: 8.1, 8.2_

  - [x] 2.2 Expand GlobalExceptionHandler
    - Add handler for `BusinessException` returning structured JSON `{ "code": ..., "message": ... }`
    - Add handler for `MethodArgumentNotValidException` returning field-level errors
    - Add handler for `AccessDeniedException` returning HTTP 403
    - Add handler for `UsernameNotFoundException` returning HTTP 401
    - Add catch-all `Exception` handler returning HTTP 500 with generic message, logging full stack trace
    - _Requirements: 8.3, 8.4, 8.5, 8.6, 8.7_

  - [ ]\* 2.3 Write property test for BusinessException structured response
    - **Property 11: BusinessException Structured Response**
    - Trigger various BusinessExceptions via service methods, verify the response contains `code` and `message` fields with correct HTTP status
    - **Validates: Requirements 8.3**

  - [ ]\* 2.4 Write property test for validation error response structure
    - **Property 6: Validation Error Response Structure**
    - Submit invalid DTOs to `@Valid` endpoints, verify HTTP 400 with field-name-to-error-message map
    - **Validates: Requirements 3.7, 8.6**

- [x] 3. Decouple services from HTTP layer
  - [x] 3.1 Refactor EnvelopeService to return domain objects/DTOs
    - Change all public methods to return `Envelope`, `List<Envelope>`, `Transaction`, or `void` instead of `ResponseEntity<?>`
    - Move all `ResponseEntity` construction to `EnvelopeController`
    - Use typed `ResponseEntity<>` generics in the controller
    - _Requirements: 6.1, 6.4, 6.5_

  - [x] 3.2 Refactor EnvelopeHistoryService to return domain objects/DTOs
    - Change all public methods to return `EnvelopeHistory`, `List<EnvelopeHistory>`, or `void` instead of `ResponseEntity<?>`
    - Move `ResponseEntity` construction to `EnvelopHistoryController`
    - _Requirements: 6.2_

  - [x] 3.3 Refactor TransactionService to return domain objects/DTOs
    - Change `getTransactionsByEnvelopeId` to return `List<Transaction>` instead of `ResponseEntity<?>`
    - Move `ResponseEntity` construction to `TransactionController`
    - _Requirements: 6.3_

- [x] 4. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 5. Lazy fetching and N+1 resolution
  - [x] 5.1 Switch all JPA associations to FetchType.LAZY
    - Change `User.envelopes` from `FetchType.EAGER` to `FetchType.LAZY`
    - Change `Envelope.user`, `Envelope.transactions`, `Envelope.envelopeHistories` from `FetchType.EAGER` to `FetchType.LAZY`
    - Change `Transaction.envelope`, `Transaction.envelopeHistories` from `FetchType.EAGER` to `FetchType.LAZY`
    - Change `EnvelopeHistory.envelope`, `EnvelopeHistory.transaction` from `FetchType.EAGER` to `FetchType.LAZY`
    - _Requirements: 7.1, 7.2, 7.3, 7.4_

  - [x] 5.2 Add @EntityGraph / JOIN FETCH queries to repositories
    - Add `@EntityGraph(attributePaths = {"user"})` to `EnvelopeRepository.findById` and `findByUser_UserId`
    - Add appropriate `@EntityGraph` or `@Query` with `JOIN FETCH` to other repository methods that need associated data
    - _Requirements: 7.5_

- [x] 6. Resolve circular dependency in SecurityConfig
  - [x] 6.1 Refactor JWTAuthFilter to use ObjectProvider
    - Change `JWTAuthFilter` constructor to accept `ObjectProvider<AuthenticationManager>` instead of direct `AuthenticationManager`
    - Call `authManagerProvider.getObject()` inside `doFilterInternal`
    - Remove `@Lazy` annotation from `SecurityConfig` constructor
    - _Requirements: 9.1, 9.2, 9.3_

- [x] 7. Externalize secrets and restrict CORS
  - [x] 7.1 Externalize backend secrets to environment variables
    - Replace hardcoded DB URL, username, password in `application.properties` with `${DB_URL}`, `${DB_USERNAME}`, `${DB_PASSWORD}`
    - Replace hardcoded JWT key with `${JWT_PRIVATE_KEY}`
    - Add `cors.allowed-origins=${CORS_ALLOWED_ORIGINS}` property
    - _Requirements: 1.1, 1.2, 1.3, 1.6_

  - [x] 7.2 Restrict CORS to explicit allowed origins
    - Inject `@Value("${cors.allowed-origins}")` as `String[]` in `SecurityConfig`
    - Replace `allowedOriginPatterns("*")` with `allowedOrigins(allowedOrigins)` in the CORS configurer
    - _Requirements: 2.1, 2.2, 2.3_

  - [x] 7.3 Externalize frontend API base URL
    - Update `backendHost.tsx` to read from `import.meta.env.VITE_API_BASE_URL` with localhost fallback
    - Remove hardcoded `axios.defaults.baseURL` from `App.tsx`
    - _Requirements: 1.5_

  - [ ]\* 7.4 Write property test for CORS origin enforcement
    - **Property 1: CORS Origin Enforcement**
    - Generate random origin strings, verify CORS preflight accepted iff origin is in configured allowed list
    - **Validates: Requirements 2.2, 2.3**

- [x] 8. Input validation with Jakarta Bean Validation
  - [x] 8.1 Add spring-boot-starter-validation dependency
    - Add `spring-boot-starter-validation` to `pom.xml`
    - _Requirements: 3.1_

  - [x] 8.2 Add validation annotations to DTOs
    - Add `@NotNull`, `@NotBlank`, `@Size`, `@DecimalMin` annotations to `EnvelopeDTO`
    - Add `@NotNull`, `@NotBlank`, `@DecimalMin` annotations to `TransferFundDTO`
    - Add `@NotBlank` annotations to `IncomingLogin`
    - Add `@NotBlank`, `@Email`, `@Size` annotations to `User` entity for registration validation
    - _Requirements: 3.2, 3.3, 3.4, 3.5, 3.6_

  - [x] 8.3 Add @Valid to all controller method parameters
    - Annotate all DTO parameters in `EnvelopeController`, `TransactionController`, `UserController`, `EnvelopHistoryController` with `@Valid`
    - _Requirements: 3.1_

  - [ ]\* 8.4 Write property test for EnvelopeDTO validation
    - **Property 2: EnvelopeDTO Validation Rejects Invalid Input**
    - Generate EnvelopeDTOs with blank/oversized descriptions, null/negative balance/maxLimit, verify HTTP 400
    - **Validates: Requirements 3.2, 3.3**

  - [ ]\* 8.5 Write property test for TransferFundDTO validation
    - **Property 3: TransferFundDTO Validation Rejects Invalid Input**
    - Generate TransferFundDTOs with null IDs, zero/negative amounts, blank titles, verify HTTP 400
    - **Validates: Requirements 3.4**

  - [ ]\* 8.6 Write property test for IncomingLogin validation
    - **Property 4: IncomingLogin Validation Rejects Blank Credentials**
    - Generate IncomingLogin with blank/whitespace username or password, verify HTTP 400
    - **Validates: Requirements 3.5**

  - [ ]\* 8.7 Write property test for User registration validation
    - **Property 5: User Registration Validation Rejects Invalid Payloads**
    - Generate invalid registration payloads (blank fields, invalid email, short password), verify rejection
    - **Validates: Requirements 3.6**

- [x] 9. Enforce resource ownership authorization
  - [x] 9.1 Implement ownership verification in EnvelopeService
    - Add `verifyOwnership(Envelope, String authenticatedUsername)` method that throws `AccessDeniedException` if user doesn't own the envelope
    - Add manager role bypass check (`ROLE_MANAGER` skips ownership verification)
    - Call ownership check in `getEnvelopeById`, `deleteEnvelope`, `allocateMoney`, `spendMoney`, `transferEnvelope`
    - Pass authenticated username from controllers to service methods
    - _Requirements: 4.1, 4.2, 4.3, 4.6_

  - [x] 9.2 Implement ownership verification in TransactionService
    - Verify envelope ownership when fetching transactions by envelope ID
    - _Requirements: 4.4, 4.5_

  - [ ]\* 9.3 Write property test for resource ownership enforcement
    - **Property 7: Resource Ownership Enforcement**
    - Generate user-envelope pairs where authenticated user != owner, verify HTTP 403 and resource unmodified
    - **Validates: Requirements 4.1, 4.2, 4.3, 4.4, 4.5**

  - [ ]\* 9.4 Write property test for manager role bypass
    - **Property 8: Manager Role Bypasses Ownership Checks**
    - Generate manager user + any resource, verify access granted regardless of ownership
    - **Validates: Requirements 4.6**

- [x] 10. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 11. Pagination for list endpoints
  - [x] 11.1 Add pagination to EnvelopeController and EnvelopeService
    - Accept `page` and `size` query parameters (defaults 0 and 20) in `GET /envelopes`
    - Create `PaginatedResponse<T>` record with `content`, `totalElements`, `totalPages`, `currentPage`, `pageSize`
    - Return `PaginatedResponse<Envelope>` from the controller
    - _Requirements: 10.1, 10.4_

  - [x] 11.2 Add pagination to TransactionController and TransactionService
    - Accept `page` and `size` query parameters in `GET /transactions`
    - Return `PaginatedResponse<Transaction>`
    - _Requirements: 10.2, 10.4_

  - [x] 11.3 Add pagination to EnvelopeHistoryController and EnvelopeHistoryService
    - Accept `page` and `size` query parameters in `GET /envelopes/history`
    - Return `PaginatedResponse<EnvelopeHistory>`
    - _Requirements: 10.3, 10.4_

  - [ ]\* 11.4 Write property test for pagination metadata correctness
    - **Property 12: Pagination Metadata Correctness**
    - Generate page/size parameters, verify `currentPage == page`, `pageSize == size`, `totalPages == ceil(totalElements / size)`, `content.length <= size`
    - **Validates: Requirements 10.1, 10.2, 10.3, 10.4**

- [x] 12. REST API design fixes
  - [x] 12.1 Fix UserController DELETE endpoint
    - Change `@DeleteMapping` to `@DeleteMapping("/{username}")` with `@PathVariable String username` instead of `@RequestBody`
    - _Requirements: 11.1_

  - [x] 12.2 Fix EnvelopeController GET by user endpoint
    - Change `/envelopes/user/{userId}` to use query parameter `/envelopes?userId={userId}` or nested resource path
    - _Requirements: 11.2_

  - [x] 12.3 Add /api/v1 prefix to all endpoints
    - Add `server.servlet.context-path=/api/v1` to `application.properties` or add base `@RequestMapping("/api/v1")` to controllers
    - Update frontend API base URL to include `/api/v1` suffix
    - _Requirements: 11.3_

  - [x] 12.4 Remove duplicate /transactions route in frontend
    - Remove the duplicate `<Route path="/transactions" element={<AllTransactions />} />` in `App.tsx`
    - _Requirements: 11.4_

- [x] 13. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 14. Frontend error boundaries
  - [x] 14.1 Create ErrorBoundary component
    - Create a class component `ErrorBoundary` that catches rendering errors via `getDerivedStateFromError` and `componentDidCatch`
    - Display a fallback UI with a retry button and a link to navigate home
    - _Requirements: 12.1, 12.2_

  - [x] 14.2 Wrap route components with ErrorBoundary
    - Wrap each `<Route>` element in `App.tsx` with `<ErrorBoundary>`
    - _Requirements: 12.3_

- [x] 15. Frontend loading and error states
  - [x] 15.1 Add loading and error states to data-fetching components
    - Add `isLoading` and `error` state to `EnvelopeList`, `DetailedEnvelope`, `AllTransactions`, and other data-fetching components
    - Display MUI `CircularProgress` during loading
    - Display MUI `Alert` with error message and retry button on failure
    - _Requirements: 13.1, 13.2, 13.3_

- [x] 16. Unify frontend styling with MUI ThemeProvider
  - [x] 16.1 Create global SCSS variables file
    - Create `_variables.scss` with all CSS custom properties (colors, spacing, border-radius, shadows)
    - Remove duplicate `:root` blocks from `Login.scss`, `Register.scss`, `Personalize.scss`, `DetailedEnvelope.scss`, `Navbar.scss`
    - Import `_variables.scss` in `main.scss`
    - _Requirements: 17.1_

  - [x] 16.2 Convert CSS files to SCSS
    - Convert `AddMoney.css` to `AddMoney.scss`
    - Convert `CreateEnvelope.css` to `CreateEnvelope.scss`
    - Update component imports accordingly
    - _Requirements: 17.2_

  - [x] 16.3 Create MUI ThemeProvider and consolidate button styles
    - Create a MUI theme with primary (green) and secondary (purple) palette, default border-radius, and typography
    - Wrap `<App />` in `<ThemeProvider>` in `main.tsx`
    - Replace duplicated button classes (`button1`, `button2`, `button3`, `envButton1`) with a single `.btn-primary` class or MUI theme button override
    - _Requirements: 17.3, 17.4_

  - [x] 16.4 Extract shared two-column layout component
    - Create a shared layout component or SCSS mixin for the text-left / form-right pattern used in Login, Register, Personalize, and CreateEnvelope
    - Refactor those components to use the shared layout
    - _Requirements: 17.5, 17.6_

  - [x] 16.5 Add focus indicators to interactive elements
    - Add `:focus-visible` outlines to all buttons, links, and form inputs via global styles or MUI theme overrides
    - _Requirements: 17.7_

- [x] 17. Checkpoint - Ensure frontend builds and all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 18. Expand backend test coverage
  - [x] 18.1 Add controller integration tests with MockMvc
    - Write MockMvc integration tests for `UserController` (login success/failure, register, get users)
    - Write MockMvc integration tests for `EnvelopeController` (CRUD, transfer, allocate, spend with auth)
    - Write MockMvc integration tests for `TransactionController` (CRUD, get by envelope)
    - Write MockMvc integration tests for `EnvelopHistoryController` (get all, get by envelope)
    - _Requirements: 14.1_

  - [x] 18.2 Add security integration tests
    - Write tests verifying unauthenticated requests to protected endpoints return HTTP 401
    - Write tests verifying a User cannot access another User's Envelopes (HTTP 403)
    - Write tests verifying manager role can access any resource
    - _Requirements: 14.2, 14.3_

  - [ ]\* 18.3 Write property test for unauthenticated access returns 401
    - **Property 13: Unauthenticated Access Returns 401**
    - Generate requests to protected endpoints without JWT, verify HTTP 401
    - **Validates: Requirements 14.2**

  - [x] 18.4 Add unit tests for EnvelopeService financial operations
    - Write unit tests for `transferEnvelope` covering success, insufficient funds, and exceeds-limit
    - Write unit tests for `allocateMoney` covering success and exceeds-limit
    - Write unit tests for `spendMoney` covering success and insufficient funds
    - _Requirements: 14.4_

  - [x] 18.5 Clean up existing test files and add JaCoCo
    - Remove unused imports and variables from existing test files
    - Add JaCoCo Maven plugin to `pom.xml` configured to generate coverage report on `mvn verify`
    - _Requirements: 14.5, 14.6_

- [x] 19. OpenAPI documentation
  - [x] 19.1 Add springdoc-openapi dependency and configuration
    - Add `springdoc-openapi-starter-webmvc-ui` dependency to `pom.xml`
    - Configure `springdoc.api-docs.path=/api/v1/api-docs` and `springdoc.swagger-ui.path=/api/v1/swagger-ui` in `application.properties`
    - Permit unauthenticated access to OpenAPI and Swagger UI paths in `SecurityConfig`
    - _Requirements: 16.1, 16.2, 16.3, 16.4_

- [x] 20. Docker Compose orchestration
  - [x] 20.1 Create docker-compose.yml
    - Define `db` service (PostgreSQL 15 with named volume)
    - Define `backend` service with environment variables (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_PRIVATE_KEY`, `CORS_ALLOWED_ORIGINS`)
    - Define `frontend` service serving static assets via Nginx
    - _Requirements: 15.1, 15.2, 15.4, 15.5_

  - [x] 20.2 Update backend Dockerfile to multi-stage build
    - Add Maven build stage and JDK 17 runtime stage
    - _Requirements: 15.3_

- [x] 21. Project README
  - [x] 21.1 Create README.md at project root
    - Include project description and tech stack (Spring Boot 3.4.1, Java 17, React 18, TypeScript, PostgreSQL, MUI)
    - Include prerequisites (Java 17, Node 18+, Docker)
    - Include local setup instructions via `docker-compose up`
    - Include instructions for running tests (`mvn verify`, `npm run lint`)
    - Include high-level architecture diagram (Mermaid or text)
    - _Requirements: 18.1, 18.2, 18.3_

- [x] 22. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests use jqwik with minimum 100 iterations per property
- Unit tests use JUnit 5 + Mockito + MockMvc
- Foundational changes (BigDecimal, exceptions, service decoupling) are ordered before dependent features
