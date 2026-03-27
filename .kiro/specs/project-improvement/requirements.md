# Requirements Document

## Introduction

This document defines the requirements for a comprehensive improvement of the Gooder Budget envelope budgeting application. The application is a full-stack system with a Spring Boot 3.4.1 / Java 17 backend and a React 18 / TypeScript frontend. The improvements span critical security fixes, architectural refactoring, test coverage expansion, frontend resilience, deployment orchestration, and documentation generation.

## Glossary

- **Backend**: The Spring Boot 3.4.1 REST API server written in Java 17, using Spring Security, Spring Data JPA, and PostgreSQL
- **Frontend**: The React 18.3 single-page application written in TypeScript, using Vite, MUI 6.3, Zustand, and React Router DOM
- **Envelope**: A budget category entity that holds a balance and a maximum spending limit, owned by a single User
- **Transaction**: A financial event (allocation, spend, or transfer) recorded against an Envelope
- **EnvelopeHistory**: An audit record capturing the Envelope balance after each Transaction
- **User**: An authenticated person who owns Envelopes and performs Transactions
- **JWT**: JSON Web Token used for stateless authentication between Frontend and Backend
- **TokenProcessor**: The Backend component responsible for generating and validating JWTs
- **SecurityConfig**: The Backend Spring Security configuration class defining authentication, authorization, and CORS rules
- **GlobalExceptionHandler**: The Backend @RestControllerAdvice class that maps exceptions to HTTP responses
- **EnvelopeService**: The Backend service class containing business logic for Envelope CRUD, transfers, allocations, and spending
- **TransactionService**: The Backend service class containing business logic for Transaction CRUD
- **EnvelopeController**: The Backend REST controller exposing Envelope endpoints
- **UserController**: The Backend REST controller exposing User authentication and management endpoints
- **DTO**: Data Transfer Object — a record or class used to carry data between layers without exposing internal entities
- **BigDecimal**: A Java class for arbitrary-precision decimal arithmetic, suitable for financial calculations
- **ErrorBoundary**: A React component that catches JavaScript errors in its child component tree and displays a fallback UI
- **OpenAPI**: The OpenAPI 3.0 specification format for documenting REST APIs (formerly Swagger)
- **JaCoCo**: A Java code coverage library that integrates with Maven to measure test coverage
- **Docker_Compose**: A tool for defining and running multi-container Docker applications via a YAML configuration file
- **MUI Theme**: A Material-UI ThemeProvider configuration that centralizes colors, typography, and component style overrides for the entire Frontend application

## Requirements

### Requirement 1: Externalize Secrets and Credentials

**User Story:** As a developer, I want all secrets and credentials read from environment variables, so that no sensitive data is committed to source control.

#### Acceptance Criteria

1. THE Backend SHALL read the JWT signing key from the environment variable `JWT_PRIVATE_KEY` instead of a hardcoded value in application.properties
2. THE Backend SHALL read the database URL, username, and password from the environment variables `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` respectively
3. THE Backend SHALL read the CORS allowed origins from the environment variable `CORS_ALLOWED_ORIGINS`
4. IF an environment variable for a required secret is missing, THEN THE Backend SHALL fail to start and log a descriptive error message identifying the missing variable
5. THE Frontend SHALL read the Backend API base URL from the Vite environment variable `VITE_API_BASE_URL` instead of a hardcoded IP address in App.tsx
6. THE application.properties file SHALL contain only placeholder references (e.g., `${DB_URL}`) and no literal credential values

### Requirement 2: Restrict CORS Policy

**User Story:** As a security engineer, I want CORS restricted to explicit allowed origins, so that cross-origin requests from untrusted domains are rejected.

#### Acceptance Criteria

1. THE SecurityConfig SHALL configure CORS allowedOrigins using a list of explicit origin URLs loaded from the `CORS_ALLOWED_ORIGINS` environment variable
2. THE SecurityConfig SHALL reject CORS preflight requests from origins not present in the allowed origins list
3. THE SecurityConfig SHALL allow credentials only when the requesting origin matches an entry in the allowed origins list

### Requirement 3: Validate All Incoming Request Data

**User Story:** As a developer, I want all incoming DTOs validated at the controller layer, so that malformed or malicious input is rejected before reaching business logic.

#### Acceptance Criteria

1. THE Backend SHALL annotate all controller method DTO parameters with `@Valid`
2. THE EnvelopeDTO SHALL enforce that envelopeDescription is not blank and has a maximum length of 255 characters
3. THE EnvelopeDTO SHALL enforce that balance and maxLimit are not null and are zero or positive
4. THE TransferFundDTO SHALL enforce that fromId and toId are not null, amount is positive, and transactionTitle is not blank
5. THE IncomingLogin DTO SHALL enforce that username and password are not blank
6. THE User registration payload SHALL enforce that username, password, email, firstName, and lastName are not blank, email matches a valid email pattern, and password has a minimum length of 8 characters
7. WHEN validation fails, THE GlobalExceptionHandler SHALL return an HTTP 400 response containing a structured JSON body with field-level error messages

### Requirement 4: Enforce Resource Ownership Authorization

**User Story:** As a user, I want to access only my own envelopes and transactions, so that other users cannot view or modify my financial data.

#### Acceptance Criteria

1. WHEN an authenticated User requests an Envelope by ID, THE EnvelopeService SHALL verify that the Envelope belongs to the authenticated User before returning the data
2. WHEN an authenticated User requests to delete an Envelope, THE EnvelopeService SHALL verify that the Envelope belongs to the authenticated User before performing the deletion
3. WHEN an authenticated User requests to allocate, spend, or transfer funds, THE EnvelopeService SHALL verify that all referenced Envelopes belong to the authenticated User
4. WHEN an authenticated User requests Transactions by Envelope ID, THE TransactionService SHALL verify that the Envelope belongs to the authenticated User
5. IF a User attempts to access a resource owned by a different User, THEN THE Backend SHALL return an HTTP 403 Forbidden response
6. WHILE a User has the ROLE_MANAGER role, THE Backend SHALL allow access to any User's Envelopes and Transactions

### Requirement 5: Use BigDecimal for Financial Amounts

**User Story:** As a developer, I want all monetary values stored and computed using BigDecimal, so that floating-point rounding errors do not corrupt financial data.

#### Acceptance Criteria

1. THE Envelope entity SHALL store balance and maxLimit as BigDecimal with a scale of 2
2. THE Transaction entity SHALL store transactionAmount as BigDecimal with a scale of 2
3. THE EnvelopeHistory entity SHALL store envelopeAmount as BigDecimal with a scale of 2
4. THE EnvelopeDTO, TransferFundDTO, and TransactionDTO SHALL use BigDecimal for all monetary fields
5. THE EnvelopeService SHALL perform all arithmetic (addition, subtraction, comparison) using BigDecimal methods instead of primitive double operators
6. FOR ALL valid Envelope balance operations, the sum of all Transaction amounts for an Envelope SHALL equal the current Envelope balance minus the initial balance (invariant property)

### Requirement 6: Decouple Services from HTTP Layer

**User Story:** As a developer, I want service methods to return domain objects or DTOs, so that controllers are solely responsible for HTTP concerns.

#### Acceptance Criteria

1. THE EnvelopeService SHALL return domain objects or DTOs from all public methods instead of ResponseEntity
2. THE EnvelopeHistoryService SHALL return domain objects or DTOs from all public methods instead of ResponseEntity
3. THE TransactionService SHALL return domain objects or DTOs from all public methods instead of ResponseEntity
4. THE EnvelopeController SHALL construct ResponseEntity objects using the values returned by EnvelopeService
5. THE controllers SHALL use typed ResponseEntity (e.g., `ResponseEntity<EnvelopeDTO>`) instead of wildcard `ResponseEntity<?>`

### Requirement 7: Use Lazy Fetching and Resolve N+1 Queries

**User Story:** As a developer, I want JPA associations to use lazy fetching with explicit join-fetch queries, so that the application does not load unnecessary data or produce N+1 query problems.

#### Acceptance Criteria

1. THE User entity SHALL declare the envelopes association with `FetchType.LAZY`
2. THE Envelope entity SHALL declare the transactions and envelopeHistories associations with `FetchType.LAZY`
3. THE Transaction entity SHALL declare the envelope and envelopeHistories associations with `FetchType.LAZY`
4. THE EnvelopeHistory entity SHALL declare the envelope and transaction associations with `FetchType.LAZY`
5. WHEN a service method requires associated data, THE corresponding Repository SHALL provide a query using `@EntityGraph` or `JOIN FETCH` to load the required associations in a single query

### Requirement 8: Standardize Exception Handling

**User Story:** As a developer, I want all business errors routed through a consistent exception hierarchy handled by GlobalExceptionHandler, so that error responses are uniform and informative.

#### Acceptance Criteria

1. THE EnvelopeService SHALL throw BusinessException instead of RuntimeException for all business rule violations (e.g., insufficient funds, envelope not found, amount exceeds limit)
2. THE TransactionService SHALL throw BusinessException instead of RuntimeException for all business rule violations
3. THE GlobalExceptionHandler SHALL handle BusinessException and return a structured JSON response containing the error code and message with the appropriate HTTP status
4. THE GlobalExceptionHandler SHALL handle UsernameNotFoundException and return an HTTP 401 response
5. THE GlobalExceptionHandler SHALL handle AccessDeniedException and return an HTTP 403 response
6. THE GlobalExceptionHandler SHALL handle MethodArgumentNotValidException (from @Valid) and return an HTTP 400 response with field-level error details
7. THE GlobalExceptionHandler SHALL handle all uncaught exceptions and return an HTTP 500 response with a generic error message, logging the full stack trace

### Requirement 9: Resolve Circular Dependency in SecurityConfig

**User Story:** As a developer, I want the SecurityConfig free of circular dependencies, so that the application context initializes cleanly without @Lazy workarounds.

#### Acceptance Criteria

1. THE SecurityConfig SHALL not use the `@Lazy` annotation on any constructor parameter
2. THE JWTAuthFilter SHALL obtain the AuthenticationManager through a method lookup or provider injection that does not create a circular bean dependency
3. WHEN the application starts, THE Spring context SHALL initialize without circular dependency warnings or errors

### Requirement 10: Add Pagination to List Endpoints

**User Story:** As a user, I want list endpoints to return paginated results, so that large datasets do not degrade performance or overwhelm the frontend.

#### Acceptance Criteria

1. THE EnvelopeController GET /envelopes endpoint SHALL accept optional `page` and `size` query parameters with defaults of 0 and 20 respectively
2. THE TransactionController GET /transactions endpoint SHALL accept optional `page` and `size` query parameters with defaults of 0 and 20 respectively
3. THE EnvelopeHistoryController GET endpoint SHALL accept optional `page` and `size` query parameters with defaults of 0 and 20 respectively
4. THE paginated response SHALL include totalElements, totalPages, currentPage, and pageSize metadata alongside the content list

### Requirement 11: Fix REST API Design Issues

**User Story:** As a developer, I want the REST API to follow standard conventions, so that the API is predictable and easy to consume.

#### Acceptance Criteria

1. THE UserController DELETE endpoint SHALL accept the username as a path variable (`/users/{username}`) instead of a request body
2. THE EnvelopeController GET by user endpoint SHALL use the path `/envelopes?userId={userId}` or `/users/{userId}/envelopes` instead of `/envelopes/user/{userId}`
3. THE Backend SHALL version all API endpoints under a `/api/v1` prefix
4. THE duplicate `/transactions` route in the Frontend Router SHALL be removed

### Requirement 12: Add Frontend Error Boundaries

**User Story:** As a user, I want the application to display a friendly fallback UI when a component crashes, so that a single error does not break the entire page.

#### Acceptance Criteria

1. THE Frontend SHALL implement an ErrorBoundary component that catches rendering errors in its child tree
2. THE ErrorBoundary SHALL display a user-friendly fallback message with an option to retry or navigate home
3. THE Frontend SHALL wrap each top-level route component with an ErrorBoundary

### Requirement 13: Add Frontend Loading and Error States

**User Story:** As a user, I want to see loading indicators during data fetches and clear error messages on failure, so that I understand the application state at all times.

#### Acceptance Criteria

1. WHILE the Frontend is fetching data from the Backend, THE Frontend SHALL display a loading indicator (e.g., spinner or skeleton)
2. WHEN a Backend request fails, THE Frontend SHALL display an error message describing the failure
3. THE Frontend SHALL provide a retry mechanism for failed data fetches

### Requirement 14: Expand Backend Test Coverage

**User Story:** As a developer, I want comprehensive unit and integration tests, so that regressions are caught before deployment.

#### Acceptance Criteria

1. THE Backend SHALL include controller integration tests using MockMvc for UserController, EnvelopeController, TransactionController, and EnvelopeHistoryController
2. THE Backend SHALL include security integration tests verifying that unauthenticated requests to protected endpoints return HTTP 401
3. THE Backend SHALL include security integration tests verifying that a User cannot access another User's Envelopes (HTTP 403)
4. THE Backend SHALL include unit tests for the transfer, allocate, and spend operations in EnvelopeService covering success, insufficient funds, and exceeds-limit scenarios
5. THE Backend SHALL remove all unused variables and imports from existing test files
6. THE Backend pom.xml SHALL include the JaCoCo Maven plugin configured to generate a coverage report on `mvn verify`

### Requirement 15: Add Docker Compose Orchestration

**User Story:** As a developer, I want a single docker-compose command to start the entire application stack locally, so that onboarding and local development are simple.

#### Acceptance Criteria

1. THE project root SHALL contain a docker-compose.yml file defining services for the Backend, Frontend, and PostgreSQL database
2. THE docker-compose.yml SHALL pass all required environment variables (DB_URL, DB_USERNAME, DB_PASSWORD, JWT_PRIVATE_KEY, CORS_ALLOWED_ORIGINS) to the Backend service
3. THE Backend Dockerfile SHALL use a multi-stage build: a Maven build stage and a runtime stage based on a JDK 17 image
4. THE Frontend service SHALL serve the built static assets via a production-ready web server (e.g., Nginx)
5. THE PostgreSQL service SHALL use a named volume for data persistence

### Requirement 16: Add OpenAPI Documentation

**User Story:** As a developer, I want auto-generated API documentation accessible via a browser, so that frontend developers and external consumers can understand the API contract.

#### Acceptance Criteria

1. THE Backend pom.xml SHALL include the springdoc-openapi dependency
2. WHEN the Backend is running, THE OpenAPI JSON specification SHALL be accessible at `/api/v1/api-docs`
3. WHEN the Backend is running, THE Swagger UI SHALL be accessible at `/api/v1/swagger-ui`
4. THE SecurityConfig SHALL permit unauthenticated access to the OpenAPI and Swagger UI endpoints

### Requirement 17: Unify Frontend Styling and Visual Consistency

**User Story:** As a user, I want the application to have a consistent, polished look and feel across all pages, so that the experience feels cohesive and professional.

#### Acceptance Criteria

1. THE Frontend SHALL define all CSS custom properties (color palette, spacing, border-radius, shadow) in a single global location instead of duplicating `:root` declarations across Login.scss, Register.scss, Personalize.scss, DetailedEnvelope.scss, and Navbar.scss
2. THE Frontend SHALL use SCSS exclusively for all component stylesheets, converting the existing plain CSS files (AddMoney.css, CreateEnvelope.css) to SCSS
3. THE Frontend SHALL replace the duplicated button classes (`button1`, `button2`, `button3`, `envButton1`) with a single reusable button style class or MUI theme override
4. THE Frontend SHALL configure a MUI ThemeProvider at the application root that defines the primary color (green), secondary color (purple), default border-radius, and typography to match the existing design intent
5. THE Frontend SHALL eliminate copy-pasted layout patterns (e.g., the two-column text-left / form-right layout repeated in Login, Register, Personalize, and CreateEnvelope) by extracting a shared layout component or shared SCSS mixin
6. THE Frontend SHALL ensure all pages use consistent spacing, card shadows, and border-radius values sourced from the global theme or shared variables
7. THE Frontend SHALL ensure all interactive elements (buttons, links, form inputs) have visible focus indicators for keyboard navigation

### Requirement 18: Add Project README

**User Story:** As a new developer, I want a README with setup instructions and architecture overview, so that I can onboard quickly.

#### Acceptance Criteria

1. THE project root SHALL contain a README.md file
2. THE README SHALL include a project description, technology stack summary, prerequisites, local setup instructions using Docker Compose, and instructions for running tests
3. THE README SHALL include a high-level architecture diagram or description showing the Frontend, Backend, and Database components and their interactions
