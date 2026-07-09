# Spring Security Tutorial

A complete, hands-on walkthrough of building an authentication and authorization system with **Spring Boot 3.5.x** (Spring Framework 6, Java 17) and **Spring Security 6**, organized into Git branches that progressively cover most of the key concepts related to security in the Spring ecosystem.

The data model follows this EER schema: `users` ↔ `roles` (via `role_user`) ↔ `permissions` (via `role_permission`), plus token management (`activation_tokens`, `blacklisted_tokens`, `password_reset_tokens`).

This document is the **complete specification** of the project: it is meant to be followed step by step to implement each branch.

Repository: https://github.com/EdgarEldy/spring-security-tutorial

## Table of contents

- [Tech stack](#tech-stack)
- [Data model](#data-model)
- [Branching strategy](#branching-strategy)
- [Project structure](#project-structure)
- [Standard response format](#standard-response-format)
- [Spring AOP](#spring-aop)
- [feature/core-architecture](#featurecore-architecture)
- [feature/users](#featureusers)
- [feature/roles-permissions](#featureroles-permissions)
- [feature/tokens](#featuretokens)
- [feature/auth](#featureauth)
- [Order of work](#order-of-work)
- [Code conventions](#code-conventions)
- [Concepts covered](#concepts-covered)
- [How to follow this tutorial](#how-to-follow-this-tutorial)

## Tech stack

| Component | Choice |
|---|---|
| Framework | Spring Boot 3.5.x (Spring Framework 6) |
| Language | Java 17 (LTS) |
| Build | Maven |
| Database | PostgreSQL 16 (via Docker Compose) |
| Migrations | Flyway |
| ORM | Spring Data JPA / Hibernate |
| DTO mapping | MapStruct + Lombok |
| Validation | Jakarta Bean Validation |
| API documentation | springdoc-openapi (Swagger UI) |
| Monitoring | Spring Boot Actuator |
| Security | Spring Security 6 + JWT (jjwt) |
| Aspect-oriented programming | Spring AOP |
| Scheduled / async tasks | Spring Scheduling (`@Scheduled`), Spring Async (`@Async`) |
| Tests | JUnit 5, Mockito, Testcontainers, AssertJ, spring-security-test |
| CI/CD | GitHub Actions |
| Containerization | Docker, docker-compose |

## Data model

```
                        ┌──────────────┐
                        │    users     │
                        └──────┬───────┘
              ┌────────────────┼────────────────────┬──────────────────┐
              │                │                     │                  │
     activation_tokens  password_reset_tokens  blacklisted_tokens   role_user
                                                                          │
                                                                       roles
                                                                          │
                                                                    role_permission
                                                                          │
                                                                    permissions
```

- `users` N-N `roles` via the join table `role_user`
- `roles` N-N `permissions` via the join table `role_permission`
- `users` 1-N `activation_tokens`, `password_reset_tokens`, `blacklisted_tokens`

### Column details

**users**
| Column | Type | Constraints |
|---|---|---|
| id | BIGINT | PK, auto-increment |
| first_name | VARCHAR(50) | NOT NULL |
| last_name | VARCHAR(100) | NOT NULL |
| email | VARCHAR(100) | NOT NULL, UNIQUE |
| password | VARCHAR(255) | NOT NULL, hashed (BCrypt) |
| enabled | BOOLEAN | NOT NULL, defaults to `false` (enabled once the account is validated) |
| account_locked | BOOLEAN | NOT NULL, defaults to `false` |

**roles**
| Column | Type | Constraints |
|---|---|---|
| id | BIGINT | PK, auto-increment |
| role_name | VARCHAR(50) | NOT NULL, UNIQUE (e.g. `ADMIN`, `USER`) |

**permissions**
| Column | Type | Constraints |
|---|---|---|
| id | BIGINT | PK, auto-increment |
| resource | VARCHAR(50) | NOT NULL (e.g. `PRODUCT`, `USER`) |
| action | VARCHAR(50) | NOT NULL (e.g. `READ`, `WRITE`, `DELETE`) |

**role_user** (join table)
| Column | Type | Constraints |
|---|---|---|
| user_id | BIGINT | Composite PK, FK → users.id |
| role_id | BIGINT | Composite PK, FK → roles.id |

**role_permission** (join table)
| Column | Type | Constraints |
|---|---|---|
| role_id | BIGINT | Composite PK, FK → roles.id |
| permission_id | BIGINT | Composite PK, FK → permissions.id |

**activation_tokens**
| Column | Type | Constraints |
|---|---|---|
| id | BIGINT | PK, auto-increment |
| user_id | BIGINT | FK → users.id |
| token | VARCHAR(255) | NOT NULL |
| created_at | DATETIME | NOT NULL |
| expires_at | DATETIME | |
| validated_at | DATETIME | set when the account is activated |

**blacklisted_tokens**
| Column | Type | Constraints |
|---|---|---|
| id | BIGINT | PK, auto-increment |
| user_id | BIGINT | FK → users.id |
| token | VARCHAR(768) | NOT NULL, full JWT |
| jti | VARCHAR(255) | UNIQUE, JWT identifier |
| blacklisted_at | DATETIME | |
| created_at | DATETIME | NOT NULL |
| expires_at | DATETIME | JWT expiration date (for purging) |
| validated_at | DATETIME | |

**password_reset_tokens**
| Column | Type | Constraints |
|---|---|---|
| id | BIGINT | PK, auto-increment |
| user_id | BIGINT | FK → users.id |
| token | VARCHAR(255) | NOT NULL |
| type | VARCHAR(255) | NOT NULL (e.g. `PASSWORD_RESET`) |
| expiry_date | DATETIME | NOT NULL |

## Branching strategy

| Branch | Role |
|---|---|
| `master` | Stable, production-ready code. No direct commits, only merges from `develop`. |
| `develop` | Integration branch. All `feature/*` branches are merged here before `master`. |
| `feature/core-architecture` | Technical foundation: project structure, configuration, Docker, CI. |
| `feature/users` | `User` entity, CRUD and administrative account management. |
| `feature/roles-permissions` | RBAC: `Role`, `Permission`, user ↔ role ↔ permission assignment. |
| `feature/tokens` | Token management: account activation, password reset, JWT blacklist. |
| `feature/auth` | Full authentication: registration, login, JWT, logout, activation, forgotten password, integrates all previous branches. |

Each feature is developed on its own branch, then merged into `develop` via a documented **Pull Request** (even solo), to keep a clear, educational trace of each step.

## Project structure

```
spring-security-tutorial/
├── src/
│   ├── main/
│   │   ├── java/edgareldy/springsecuritytutorial/
│   │   │   ├── SpringSecurityTutorialApplication.java
│   │   │   ├── config/
│   │   │   │   ├── OpenApiConfig.java
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── JacksonConfig.java
│   │   │   │   ├── CorsConfig.java
│   │   │   │   ├── SchedulingConfig.java
│   │   │   │   └── AsyncConfig.java
│   │   │   ├── entity/
│   │   │   │   ├── User.java
│   │   │   │   ├── Role.java
│   │   │   │   ├── Permission.java
│   │   │   │   ├── ActivationToken.java
│   │   │   │   ├── BlacklistedToken.java
│   │   │   │   └── PasswordResetToken.java
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── RoleRepository.java
│   │   │   │   ├── PermissionRepository.java
│   │   │   │   ├── ActivationTokenRepository.java
│   │   │   │   ├── BlacklistedTokenRepository.java
│   │   │   │   └── PasswordResetTokenRepository.java
│   │   │   ├── dto/
│   │   │   │   ├── common/
│   │   │   │   │   ├── ApiResponse.java
│   │   │   │   │   └── PageResponse.java
│   │   │   │   ├── user/ (UserRequest, UserResponse, UpdateProfileRequest)
│   │   │   │   ├── role/ (RoleRequest, RoleResponse)
│   │   │   │   ├── permission/ (PermissionRequest, PermissionResponse)
│   │   │   │   └── auth/ (RegisterRequest, LoginRequest, AuthResponse,
│   │   │   │             ActivateAccountRequest, ForgotPasswordRequest, ResetPasswordRequest)
│   │   │   ├── mapper/
│   │   │   │   ├── UserMapper.java
│   │   │   │   ├── RoleMapper.java
│   │   │   │   └── PermissionMapper.java
│   │   │   ├── service/
│   │   │   │   ├── UserService.java
│   │   │   │   ├── RoleService.java
│   │   │   │   ├── PermissionService.java
│   │   │   │   ├── ActivationTokenService.java
│   │   │   │   ├── PasswordResetTokenService.java
│   │   │   │   ├── BlacklistedTokenService.java
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── EmailService.java
│   │   │   │   └── impl/
│   │   │   │       ├── UserServiceImpl.java
│   │   │   │       ├── RoleServiceImpl.java
│   │   │   │       ├── PermissionServiceImpl.java
│   │   │   │       ├── ActivationTokenServiceImpl.java
│   │   │   │       ├── PasswordResetTokenServiceImpl.java
│   │   │   │       ├── BlacklistedTokenServiceImpl.java
│   │   │   │       ├── AuthServiceImpl.java
│   │   │   │       └── EmailServiceImpl.java
│   │   │   ├── controller/
│   │   │   │   ├── UserController.java
│   │   │   │   ├── RoleController.java
│   │   │   │   ├── PermissionController.java
│   │   │   │   └── AuthController.java
│   │   │   ├── exception/
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── BusinessRuleException.java
│   │   │   │   ├── InvalidTokenException.java
│   │   │   │   ├── ErrorResponse.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── security/
│   │   │   │   ├── JwtService.java
│   │   │   │   ├── JwtAuthFilter.java
│   │   │   │   ├── UserDetailsServiceImpl.java
│   │   │   │   ├── CustomPermissionEvaluator.java
│   │   │   │   ├── CustomAuthenticationEntryPoint.java
│   │   │   │   └── CustomAccessDeniedHandler.java
│   │   │   ├── aspect/
│   │   │   │   ├── LoggingAspect.java
│   │   │   │   └── ExecutionTimeAspect.java
│   │   │   └── scheduler/
│   │   │       └── TokenCleanupScheduler.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-test.yml
│   │       ├── application-prod.yml
│   │       └── db/migration/
│   │           └── V1__init_schema.sql
│   └── test/
│       └── java/edgareldy/springsecuritytutorial/
│           ├── controller/ (MockMvc + spring-security-test tests)
│           ├── service/ (Mockito unit tests)
│           ├── security/ (JWT filter and PermissionEvaluator tests)
│           └── repository/ (@DataJpaTest / Testcontainers tests)
├── docker-compose.yml
├── Dockerfile
├── .github/workflows/ci.yml
├── pom.xml
└── README.md
```

## Standard response format

Every API response (success and error alike) is wrapped in a generic `ApiResponse<T>` DTO, defined in `dto/common/ApiResponse.java`, following the same principle as the `spring-boot-tutorial` project.

```java
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        Instant timestamp
) {
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, Instant.now());
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, Instant.now());
    }
}
```

- On list endpoints, `data` holds a `PageResponse<T>` (paginated content: `content`, `page`, `size`, `totalElements`, `totalPages`).
- On error, `GlobalExceptionHandler` returns an `ApiResponse<Void>` with `success=false` and an explicit `message` (invalid/expired token, wrong credentials, locked account, etc.).

## Spring AOP

- Dependency: `spring-boot-starter-aop`
- `LoggingAspect` (`aspect/LoggingAspect.java`): `@Around` advice on all `@Service` beans, logs method entry/exit, arguments (masking sensitive fields such as `password` and `token`) and exceptions
- `ExecutionTimeAspect` (`aspect/ExecutionTimeAspect.java`): `@Around` advice on controllers, measures the processing time of each request
- Also used to illustrate an application-security-flavored aspect: logging every failed login attempt without duplicating code in `AuthServiceImpl`

## feature/core-architecture

Technical foundation shared by the whole project, to be merged first into `develop`.

### Tasks

- [x] Initialize the project via Spring Initializr (Maven, Java 17, Spring Boot 3.5.16)
- [x] Dependencies: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `spring-boot-starter-actuator`, `spring-boot-starter-security`, `spring-boot-starter-aop`, `spring-boot-starter-mail`, `flyway-core`, `flyway-database-postgresql`, `postgresql` driver, `lombok`, `mapstruct` + `mapstruct-processor`, `springdoc-openapi-starter-webmvc-ui`, `jjwt-api`/`jjwt-impl`/`jjwt-jackson`
- [x] Test dependencies: `spring-boot-starter-test`, `spring-security-test`, `testcontainers` (junit-jupiter, postgresql)
- [x] Create the package tree shown above
- [x] `application.yml`: shared configuration (app name, port, JSON date format)
- [x] `application-dev.yml`: local datasource, `spring.jpa.show-sql=true`, Flyway enabled, dummy SMTP config (e.g. MailHog/Mailtrap)
- [x] `application-test.yml`: Testcontainers datasource
- [x] `application-prod.yml`: datasource and JWT secrets via environment variables
- [x] Flyway script `V1__init_schema.sql` (users, roles, permissions, role_user, role_permission, activation_tokens, blacklisted_tokens, password_reset_tokens tables)
- [x] `GlobalExceptionHandler` (`@RestControllerAdvice`): `ResourceNotFoundException` (404), `MethodArgumentNotValidException` (400), `BusinessRuleException` (422), `InvalidTokenException` (400), `BadCredentialsException`/`LockedException`/`DisabledException` (401), generic `Exception` (500)
- [x] Standard `ErrorResponse` DTO: `timestamp`, `status`, `error`, `message`, `path`, `fieldErrors`
- [x] Generic `ApiResponse<T>` and `PageResponse<T>` DTOs (`dto/common/`)
- [x] `LoggingAspect` and `ExecutionTimeAspect` (`aspect/`)
- [x] `OpenApiConfig`: "Authorize" button (Bearer JWT) in Swagger UI
- [x] Actuator: `health`, `info`, `metrics` in dev; `health` only in prod
- [x] Structured logging (`logback-spring.xml`)
- [x] `CorsConfig`
- [x] `AsyncConfig` (`@EnableAsync`, dedicated `ThreadPoolTaskExecutor` for sending emails)
- [x] `SchedulingConfig` (`@EnableScheduling`)
- [x] Multi-stage `Dockerfile` + `docker-compose.yml` (app + PostgreSQL + MailHog to test emails locally)
- [x] `.github/workflows/ci.yml`: Maven build + tests
- [x] Branch `README` explaining the configuration choices (see below)

### Configuration notes

- **`hibernate.ddl-auto` is `validate` in every profile**, never `update` or `create`. Flyway's
  `V1__init_schema.sql` is the single source of truth for the schema; Hibernate only checks
  that entity mappings agree with it once `feature/users`/`feature/roles-permissions` add the
  entities.
- **Structured logging combines two mechanisms on purpose.** `logback-spring.xml` includes
  Spring Boot's own default appenders instead of redefining them, and adds a prod-only
  `file-appender.xml` include via `<springProfile name="prod">`. The actual JSON formatting
  comes from Spring Boot's native `logging.structured.format.console`/`.file` properties (set
  to `ecs` in `application-prod.yml`), so no extra dependency or hand-written JSON encoder was
  needed.
- **`CorsConfig` is annotated `@Profile("dev")`.** Only a local frontend dev server
  (`localhost:4200`) gets a CORS exemption; prod is expected to declare its own, narrower
  policy once a real frontend origin exists.
- **Testcontainers only, no H2.** `application-test.yml` declares no datasource at all:
  `TestcontainersConfiguration`'s `@ServiceConnection` `PostgreSQLContainer` bean wires the
  datasource automatically. Real PostgreSQL in tests avoids behavioral differences between the
  two engines (types, constraints, SQL dialect) that would otherwise only surface in
  production.
- **`application-prod.yml`'s SMTP auth/STARTTLS are overridable via `MAIL_SMTP_AUTH`/
  `MAIL_SMTP_STARTTLS`, defaulting to `true`.** `docker-compose.yml` sets both to `false` so
  the `app` service can talk to the bundled MailHog container, which supports neither; a real
  deployment leaves the defaults or points at its actual SMTP provider's requirements.
- **`LoggingAspect` redacts record components named "password" or "token" (case insensitive)
  via reflection**, rather than requiring every future DTO to implement a masking interface.
  Any record-based request/response added in later branches is covered automatically.
- **`GlobalExceptionHandler` already handles `BadCredentialsException`/`LockedException`/
  `DisabledException`** even though `SecurityConfig` and the login flow only arrive in
  `feature/auth`: `spring-boot-starter-security` is already a dependency, and centralizing the
  401 mapping here avoids revisiting this class later.

## feature/users

### Endpoints

| Method | URL | Description | Access |
|---|---|---|---|
| GET | `/api/users` | Paginated list of users | ADMIN |
| GET | `/api/users/{id}` | User detail | ADMIN or owner |
| PUT | `/api/users/{id}` | Update profile (first name, last name) | ADMIN or owner |
| DELETE | `/api/users/{id}` | Delete an account | ADMIN |
| PATCH | `/api/users/{id}/lock` | Lock an account (`account_locked=true`) | ADMIN |
| PATCH | `/api/users/{id}/unlock` | Unlock an account | ADMIN |

### Tasks

- [x] `User` entity implementing `UserDetails` (or wrapped via `UserDetailsServiceImpl` in `feature/auth`)
- [x] `UserRepository` (`findByEmail`, derived queries)
- [x] DTOs `UserRequest`/`UserResponse`/`UpdateProfileRequest` (the password never appears in responses)
- [x] `UserMapper` (MapStruct, explicit exclusion of the `password` field)
- [x] `UserService` interface + `UserServiceImpl` implementation
- [x] Business rule: an ADMIN cannot lock their own account
- [x] `UserController` with access checks
- [x] Unit and integration tests (with `@WithMockUser`)

### Configuration notes and deviations

- **`PasswordEncoderConfig` and `MethodSecurityConfig` were added ahead of `feature/auth`'s full
  `SecurityConfig`.** `UserServiceImpl.createUser` needs a `PasswordEncoder` to hash passwords,
  and `UserController`'s ADMIN-only endpoints need `@EnableMethodSecurity` to make
  `@PreAuthorize` effective, both well before a login endpoint or filter chain exist. Each stays
  a small, single-purpose `@Configuration` class; `feature/auth` still owns the
  `SecurityFilterChain`, `JwtAuthFilter`, and the rest of `SecurityConfig`.
- **`GlobalExceptionHandler` now also maps `AccessDeniedException` to 403.** Both
  `@PreAuthorize` failures and `UserController`'s manual owner check throw it; without
  `SecurityConfig`'s `ExceptionTranslationFilter` (added only in `feature/auth`), nothing else
  would translate it, so it would otherwise fall through to the generic 500 handler.
- **Deviation from the illustrative `@PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")`
  expression:** `GET /api/users/{id}` and `PUT /api/users/{id}` check ownership manually against
  `authentication.getName()` (the caller's email) resolved through `UserService.findByEmail`,
  instead of `authentication.principal.id`. `User` only becomes the actual Spring Security
  principal type once `feature/auth` wires `UserDetailsServiceImpl`; until then,
  `authentication.principal` is a generic Spring Security `UserDetails` with no `id` property.
  The email-based check is enforceable and testable today with plain `@WithMockUser` and keeps
  working unchanged once `feature/auth` lands.
- **The current `Authentication` is read from `SecurityContextHolder` rather than taken as a
  controller method parameter.** Resolving it as a parameter relies on
  `HttpServletRequest.getUserPrincipal()`, which is only populated by the security filter chain;
  reading `SecurityContextHolder` directly matches what `@PreAuthorize` itself checks and what
  `@WithMockUser` populates in tests, filter chain or not.
- **`UserRequest` has no matching `POST /api/users` endpoint in this branch.** It backs
  `UserService.createUser`, called for the first time by `feature/auth`'s
  `AuthServiceImpl.register`; there is deliberately no public user-creation endpoint outside
  registration.

## feature/roles-permissions

### Endpoints

| Method | URL | Description | Access |
|---|---|---|---|
| GET | `/api/roles` | List of roles | ADMIN |
| POST | `/api/roles` | Create a role | ADMIN |
| PUT | `/api/roles/{id}` | Update a role | ADMIN |
| DELETE | `/api/roles/{id}` | Delete a role | ADMIN |
| GET | `/api/permissions` | List of permissions | ADMIN |
| POST | `/api/permissions` | Create a permission | ADMIN |
| DELETE | `/api/permissions/{id}` | Delete a permission | ADMIN |
| POST | `/api/roles/{roleId}/permissions/{permissionId}` | Assign a permission to a role | ADMIN |
| DELETE | `/api/roles/{roleId}/permissions/{permissionId}` | Remove a permission from a role | ADMIN |
| POST | `/api/users/{userId}/roles/{roleId}` | Assign a role to a user | ADMIN |
| DELETE | `/api/users/{userId}/roles/{roleId}` | Remove a role from a user | ADMIN |

### Tasks

- [x] `Role`, `Permission` entities with `@ManyToMany` relations (`role_user`, `role_permission` as explicit join tables or via `@JoinTable`)
- [x] `RoleRepository`, `PermissionRepository`
- [x] Corresponding DTOs and mappers
- [x] `RoleService`, `PermissionService` interfaces + implementations in `service/impl`
- [x] `RoleController`, `PermissionController`
- [x] `CustomPermissionEvaluator` (`security/CustomPermissionEvaluator.java`): implements `PermissionEvaluator` to evaluate `@PreAuthorize("hasPermission('PRODUCT','WRITE')")` expressions based on `resource`/`action`
- [x] Unit tests (assigning/removing roles and permissions) and integration tests

### Configuration notes and deviations

- **Permission naming convention: `RESOURCE:ACTION`** (colon-separated, both upper-case), e.g.
  `USER:CREATE`, `USER:READ`, `USER:UPDATE`, `USER:DELETE`. `resource`/`action` stay separate
  columns on `Permission` (schema already fixed in `feature/core-architecture`'s V1 migration);
  the colon only appears where the pair is combined into a single string, i.e. the derived
  Spring Security authority.
- **`User.getAuthorities()` now derives real authorities**: one `ROLE_<ROLENAME>` per assigned
  role, one `PERMISSION_<RESOURCE>:<ACTION>` per permission granted through those roles, both
  upper-cased regardless of how `roleName`/`resource`/`action` were stored (neither
  `RoleRequest`/`PermissionRequest` nor the database enforce a canonical case, and `hasRole`/
  `hasPermission` compare against upper-case strings; storing mixed-case data without this
  normalization silently breaks every authorization check for that role/permission). Replaces
  the empty list `feature/users` used as a placeholder.
- **`CustomPermissionEvaluator` checks `PERMISSION_<RESOURCE>:<ACTION>` authorities directly**,
  not a loaded target entity. `hasPermission('USER', 'CREATE')` resolves to checking for a
  `PERMISSION_USER:CREATE` authority on the current `Authentication`. This keeps permission
  checks resource/action based without needing `UserDetailsServiceImpl` (not built until
  `feature/auth`) to supply a fully hydrated `User` principal; it also makes the evaluator
  trivially unit-testable with a plain `Authentication` built from
  `SimpleGrantedAuthority`, no Spring context required.
- **`MethodSecurityConfig` now wires a `MethodSecurityExpressionHandler` bean** with
  `CustomPermissionEvaluator` set as its `PermissionEvaluator`, using a `static` `@Bean` factory
  method per Spring Security's documented pattern for method-security infrastructure beans.
  Any `@WebMvcTest` slice that imports `MethodSecurityConfig` must also import
  `CustomPermissionEvaluator` (a plain `@Component`, not picked up by `@WebMvcTest` scanning) or
  context loading fails.
- **Role and permission deletion refuse to remove a still-referenced row**
  (`RoleServiceImpl.delete` checks `existsByRoles_Id`, `PermissionServiceImpl.delete` checks
  `existsByPermissions_Id`), mirroring the non-empty-category rule from `spring-boot-tutorial`.
  Not explicitly required by this section of the README, but avoids a raw foreign key
  violation surfacing as an unhelpful 500.
- **`GET /api/roles` and `GET /api/permissions` return a plain `List<T>`, not
  `PageResponse<T>`**, unlike `GET /api/users`: the README describes these as a plain "list",
  not a paginated one, and both tables are expected to stay small (admin-managed reference
  data).
- **Role/permission assignment reuses `Set.add`/`Set.remove` return values** (`Permission`,
  `Role`, and `User` all have id-based `equals`/`hashCode`) to detect "already assigned" and
  "not assigned" without an extra existence query.
- **`UserResponse` now includes `roles`** (role names only, not their permissions). Every
  existing construction site from `feature/users` (mapper, tests) was updated accordingly.

## feature/tokens

This branch does not expose public endpoints: it provides the generation/validation/cleanup building blocks for tokens, consumed by `feature/auth`.

### Tasks

- [ ] `ActivationToken`, `PasswordResetToken`, `BlacklistedToken` entities
- [ ] Associated repositories (`findByToken`, `findByJti`, `findByUserIdAndValidatedAtIsNull`, etc.)
- [ ] `ActivationTokenService` interface + implementation: secure random token generation, expiration (e.g. 24h), validation, `validated_at` marking
- [ ] `PasswordResetTokenService` interface + implementation: generation, expiration (e.g. 1h), invalidation after use
- [ ] `BlacklistedTokenService` interface + implementation: adding a JWT to the blacklist on logout, presence check (`existsByJti`) used by `JwtAuthFilter`
- [ ] `EmailService` interface + `EmailServiceImpl` implementation (`spring-boot-starter-mail`), `sendActivationEmail`, `sendPasswordResetEmail` methods, executed asynchronously (`@Async`)
- [ ] `TokenCleanupScheduler` (`scheduler/TokenCleanupScheduler.java`): `@Scheduled(cron = "...")` job that purges expired tokens daily (activation, reset, blacklist)
- [ ] Unit tests (token generation, expiration, validation) and scheduler tests

## feature/auth

Final integration branch: full authentication, depends on `users`, `roles-permissions` and `tokens`.

### Endpoints

| Method | URL | Description | Access |
|---|---|---|---|
| POST | `/api/auth/register` | Registration (account created disabled + activation email sent) | Public |
| GET | `/api/auth/activate-account?token=...` | Activates the account via the token received by email | Public |
| POST | `/api/auth/login` | Authenticates and returns a JWT | Public |
| POST | `/api/auth/logout` | Adds the current JWT to the blacklist | Authenticated |
| GET | `/api/auth/me` | Profile of the logged-in user | Authenticated |
| POST | `/api/auth/forgot-password` | Generates a reset token and sends an email | Public |
| POST | `/api/auth/reset-password` | Resets the password via the received token | Public |

### Tasks

- [ ] `JwtService` (`security/JwtService.java`): JWT generation (claims: `sub`, `jti`, `roles`, `permissions`, `iat`, `exp`), signature/expiration validation, claims extraction
- [ ] `JwtAuthFilter` (`OncePerRequestFilter`): extracts the JWT from the `Authorization` header, checks it is not in `blacklisted_tokens` (via `BlacklistedTokenService`), populates the `SecurityContext`
- [ ] `UserDetailsServiceImpl`: loads a `User` with its roles/permissions for Spring Security
- [ ] `SecurityConfig` (`SecurityFilterChain`): BCrypt `PasswordEncoder`, `STATELESS` session, CSRF disabled, per-endpoint authorization rules, registration of `JwtAuthFilter`, `CustomPermissionEvaluator`, `CustomAuthenticationEntryPoint` (401), `CustomAccessDeniedHandler` (403)
- [ ] `AuthService` interface + `AuthServiceImpl` implementation orchestrating `UserService`, `ActivationTokenService`, `PasswordResetTokenService`, `BlacklistedTokenService`, `EmailService`, `JwtService`
- [ ] Business rule: refuse login if `enabled=false` (`DisabledException`) or `account_locked=true` (`LockedException`)
- [ ] `AuthController`
- [ ] OpenAPI documentation with a Bearer JWT security scheme
- [ ] End-to-end integration tests: register → activate → login → access a protected resource → logout → token rejected after logout
- [ ] Error case tests: unactivated account, locked account, expired token, already-used token, wrong credentials

## Order of work

1. `feature/core-architecture` → Pull Request to `develop`
2. `feature/users` (depends on `core-architecture`) → Pull Request to `develop`
3. `feature/roles-permissions` (depends on `users`) → Pull Request to `develop`
4. `feature/tokens` (depends on `users`) → Pull Request to `develop`
5. `feature/auth` (depends on `users`, `roles-permissions`, `tokens`) → Pull Request to `develop`
6. `develop` → `master` once everything is tested and validated

## Code conventions

- Root package: `edgareldy.springsecuritytutorial`
- DTOs: Java `record` rather than classes (immutability, less boilerplate)
- No business logic in controllers: delegate to the service layer only
- **Contract/implementation services**: the interface (`XxxService`) lives at the root of `service/`, its implementation (`XxxServiceImpl`) lives in `service/impl/`. Controllers and tests only depend on the interface.
- Any service method that writes to the database is annotated `@Transactional` (on the implementation)
- Every controller returns an `ApiResponse<T>` (see [Standard response format](#standard-response-format))
- The password and raw tokens are never returned in an HTTP response nor logged in plain text
- Tokens (activation, reset, JWT) are generated with a cryptographically secure random generator (`SecureRandom` or equivalent)

## Concepts covered

- Layered architecture (controller / service / repository)
- Spring Data JPA: `@ManyToMany` relations with join tables, derived queries, `@Query`
- DTOs and mapping (MapStruct)
- Validation (Bean Validation)
- Centralized exception handling
- Transactions (`@Transactional`)
- Schema migrations (Flyway)
- API documentation (OpenAPI / Swagger UI) with a JWT security scheme
- Observability (Actuator, structured logging)
- JWT authentication (generation, validation, security filter)
- Fine-grained RBAC authorization (roles, permissions, custom `PermissionEvaluator`, `@PreAuthorize`)
- Account lifecycle (registration, email activation, locking)
- Password reset via token
- Logout via token blacklist (JWT blacklisting)
- Aspect-oriented programming (Spring AOP)
- Scheduled tasks (`@Scheduled`) for cleaning up expired tokens
- Asynchronous processing (`@Async`) for sending emails
- Security testing (`spring-security-test`, `@WithMockUser`)
- Containerization (Docker, docker-compose)
- Continuous integration (GitHub Actions)

## How to follow this tutorial

1. Clone the repository and check out `develop`
2. Create/checkout the `feature/core-architecture` branch and follow its task checklist
3. Continue with `feature/users`, `feature/roles-permissions`, `feature/tokens`, then `feature/auth` in that order
4. Open a Pull Request to `develop` at the end of each branch
5. Run the project with `docker-compose up`, then open Swagger UI at `http://localhost:8080/swagger-ui.html`
