# 🏆 SportVision API

> RESTful backend application for managing children's sports sections built with Spring Boot 4

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.4-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?logo=postgresql)](https://www.postgresql.org/)
[![CI](https://github.com/DenisKoriavets/sportvision/actions/workflows/pipeline.yml/badge.svg)](https://github.com/DenisKoriavets/sportvision/actions)

---

## 📖 About the Project

**SportVision API** is a full-featured RESTful backend for automating the operations of children's sports sections. The system works through parents: a parent registers an account, adds children, and manages their participation in training — enrolling them in sessions, purchasing subscriptions, and receiving notifications.

The business side of the system tracks who actually attends training and who pays for it. Coaches mark attendance through the API, and the system automatically deducts sessions from the subscription. If a subscription has expired, the child cannot be enrolled in new sessions. The service handles all communication with parents: training reminders, schedule change notifications, and subscription expiry warnings.

---

## ✨ Features

### 🔐 Authentication & Security
- Registration with email confirmation via SendGrid
- Stateless JWT authentication with **Access (15 min) + Refresh (7 days) tokens**
- Refresh token rotation on every renewal
- Custom password validation via `@Password` annotation
- Account deactivation by administrator — a blocked user cannot use even a valid token
- Rate limit on re-sending the verification email (once every 2 minutes)

### 👨‍👩‍👧 Children & Group Management
- A parent can have an unlimited number of children, each being a separate participant
- Enrolling a child in a group checks: age, group capacity, and presence of an active subscription
- Dynamic group filtering via **JPA Specification + Criteria API**: section, coach, age, available spots (implemented via subquery)
- `currentOccupancy` is computed via `@Formula` without a separate field in the DB

### 📋 Attendance & Session Deduction
The key business process is implemented via **Event-Driven architecture**:

```
Coach marks attendance
    → AttendanceMarkedEvent
        → SubscriptionDeductionListener: deducts a session
            → SubscriptionExpiredEvent / SubscriptionLowEvent
                → NotificationDispatchListener: notifies the parent
```

- Bulk attendance marking in a single request
- Deduction only for `PRESENT` status; ABSENT and EXCUSED do not deduct
- **Optimistic locking** via `@Version` on Subscription prevents race conditions
- When a session is cancelled — sessions are returned to the subscription via `SessionCancellationListener`

### 💳 Payment System (Stripe)
- Stripe Checkout redirect-based flow
- Webhook signature verification via `Webhook.constructEvent()` — protection against forged requests
- Idempotent webhook processing: a duplicate `checkout.session.completed` is ignored
- Scheduled job every 30 minutes cancels PENDING payments older than 1 hour

### 🔔 Notifications (Strategy Pattern)
Three channels implemented via the **Strategy** pattern with asynchronous delivery:
- **Email** — HTML templates via Thymeleaf + SendGrid REST API
- **Telegram** — delivery via Telegram Bot API without extra libraries

The parent selects channels in their profile. If preferences are empty — fallback to Email.

### 📅 Scheduled Jobs
- `SessionReminderJob` — every day at 18:00, reminds about tomorrow's training sessions
- `SubscriptionExpiryJob` — every day at 9:00, warns about subscriptions with ≤2 sessions remaining
- `PaymentExpiryJob` — every 30 minutes, cancels expired payments

---

## 🛠️ Tech Stack

| Category | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.4 |
| Security | Spring Security + JJWT 0.12.6 |
| ORM | Spring Data JPA / Hibernate |
| DB (prod) | PostgreSQL 15 |
| DB (test) | Testcontainers + PostgreSQL |
| Migrations | Liquibase |
| Validation | Jakarta Bean Validation + custom annotations |
| Mapping | MapStruct 1.5.5 |
| AOP | Spring AOP (AspectJ) |
| Filtering | JPA Specification / Criteria API |
| Payments | Stripe Java SDK |
| Email | SendGrid Java SDK + Thymeleaf templates |
| Telegram | Telegram Bot API (REST) |
| Documentation | SpringDoc OpenAPI 3.0 (Swagger UI) |
| Testing | JUnit 5 + Mockito + Testcontainers |
| Build | Gradle |
| Containers | Docker + Docker Compose |
| CI/CD | GitHub Actions |

---

## 🏗️ Architecture

The project is built on **Layered Architecture** with a clear separation of concerns:

```
Controller → Service → Repository → Database
               ↕
         Event Publisher
               ↕
           Listeners
               ↕
     Notification Channels
```

Services do not call each other directly — communication happens through `ApplicationEventPublisher`. This ensures loose coupling between the attendance, subscription, and notification modules.

### Package Structure

```
src/main/java/com/github/deniskoriavets/sportvision/
├── aspect/             # LoggingAspect, AuditAspect, ExceptionLoggingAspect
├── config/             # JWT, Stripe, SendGrid, AsyncConfig, OpenAPI
├── controller/         # REST controllers
├── dto/                # Request / Response / Criteria records
├── entity/             # JPA entities with soft delete (@SQLDelete)
│   └── enums/          # Role, SessionStatus, SubscriptionStatus, ...
├── event/              # Domain events (records)
├── exception/          # GlobalExceptionHandler + custom exceptions
├── listener/           # @TransactionalEventListener handlers
├── mapper/             # MapStruct compile-time mappers
├── notification/       # NotificationStrategy, NotificationDispatcher
├── repository/         # Spring Data JPA + JpaSpecificationExecutor
│   └── specification/  # Criteria API builders
├── scheduler/          # @Scheduled jobs
├── security/           # JWT filter, SecurityFacade, UserDetailsService
├── service/            # interfaces + impl
└── validation/         # @Password, @ValidAgePeriod, @ValidPhoneNumber, ...
```

---

## 🚀 Running the Project

### Prerequisites

- [Docker](https://docs.docker.com/get-docker/) and [Docker Compose](https://docs.docker.com/compose/)
- [JDK 21](https://adoptium.net/) — for local run without Docker

### 1. Clone the Repository

```bash
git clone https://github.com/DenisKoriavets/sportvision.git
cd sportvision
```

### 2. Configure Environment Variables

Create a `.env` file in the project root:

```env
JWT_SECRET_KEY=your-very-secret-key-at-least-256-bits-long-base64-encoded

SENDGRID_API_KEY=SG.your_sendgrid_api_key
SENDGRID_FROM_EMAIL=noreply@yourdomain.com

STRIPE_SECRET_KEY=sk_test_your_stripe_secret_key
STRIPE_PUBLISHABLE_KEY=pk_test_your_stripe_publishable_key
STRIPE_WEBHOOK_SECRET=whsec_your_webhook_secret

TELEGRAM_BOT_TOKEN=your_telegram_bot_token
```

> ⚠️ Never commit `.env` to the repository. It is already in `.gitignore`.

### 3. Run with Docker Compose

```bash
# Build the application Docker image
docker build -t sportvision-api:latest .

# Start the application and the database
docker compose up -d
```

The application will be available at: **http://localhost:8080**

### 4. Local Run (without Docker)

Make sure PostgreSQL is running and `.env` is filled in, then:

```bash
./gradlew bootRun
```

---

## 📋 API Endpoints

### 🔐 Auth — `/api/v1/auth`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/register` | Register a parent | ❌ |
| `POST` | `/login` | Login, receive JWT pair | ❌ |
| `POST` | `/refresh` | Refresh access token | ❌ |
| `POST` | `/logout` | Logout, invalidate refresh token | ✅ |
| `GET` | `/verify?token={token}` | Confirm email | ❌ |
| `POST` | `/resend-verification` | Resend verification email (rate limit: 2 min) | ❌ |

### 👤 Parents — `/api/v1/parents`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `GET` | `/me` | Get own profile | PARENT |
| `PUT` | `/me` | Update profile + notification preferences | PARENT |
| `POST` | `/me/telegram/link` | Link Telegram chat | PARENT |
| `GET` | `/` | List all parents | ADMIN |
| `PUT` | `/{id}/deactivate` | Deactivate account | ADMIN |

### 👶 Children — `/api/v1/children`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `GET` | `/` | List own children | PARENT |
| `POST` | `/` | Add a child | PARENT |
| `GET` | `/{id}` | Child details | PARENT (owner) |
| `PUT` | `/{id}` | Update child data | PARENT (owner) |
| `DELETE` | `/{id}` | Delete child (soft delete) | PARENT (owner) |
| `GET` | `/{id}/attendance` | Attendance history | PARENT (owner) |
| `GET` | `/{id}/subscriptions` | Child subscriptions | PARENT (owner) |
| `GET` | `/search` | Search children (filters) | Authenticated |

### 📝 Enrollments — `/api/v1/enrollments`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/` | Enroll a child in a group | PARENT |
| `DELETE` | `/` | Remove a child from a group | PARENT |

### 🏟️ Sections — `/api/v1/sections`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `GET` | `/` | List sections with filters | Authenticated |
| `POST` | `/` | Create a section | ADMIN |
| `GET` | `/{id}` | Section details | Authenticated |
| `PUT` | `/{id}` | Update a section | ADMIN |
| `DELETE` | `/{id}` | Delete a section (soft delete) | ADMIN |

### 👥 Groups — `/api/v1/groups`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `GET` | `/` | List groups (filters: section, coach, age, spots) | Authenticated |
| `POST` | `/` | Create a group | ADMIN |
| `GET` | `/{id}` | Group details | Authenticated |
| `PUT` | `/{id}` | Update a group | ADMIN |
| `DELETE` | `/{id}` | Delete a group (soft delete) | ADMIN |
| `GET` | `/{id}/children` | List children in a group | COACH / ADMIN |

### 📅 Schedules — `/api/v1/schedules`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/` | Add a schedule slot | ADMIN / COACH |
| `GET` | `/group/{groupId}` | Group schedule | Authenticated |
| `DELETE` | `/{id}` | Delete a slot | ADMIN / COACH |

### 🎯 Sessions — `/api/v1/sessions`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/generate` | Generate sessions from schedule | ADMIN |
| `POST` | `/` | Create a one-time session | ADMIN / COACH |
| `PUT` | `/{id}/cancel` | Cancel a session | ADMIN / COACH |
| `POST` | `/{id}/attendance` | Mark attendance (bulk) | COACH |
| `PUT` | `/{sessionId}/attendance/{childId}` | Correct a mark (only for SCHEDULED) | COACH / ADMIN |
| `GET` | `/group/{groupId}` | Group sessions by date | Authenticated |
| `GET` | `/` | Search sessions with filters | Authenticated |

### 🎫 Subscriptions — `/api/v1/subscriptions`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/admin/buy` | Purchase a subscription for cash | ADMIN |
| `GET` | `/child/{childId}` | Child subscriptions | PARENT |
| `GET` | `/{id}` | Subscription details | PARENT (owner) |
| `PUT` | `/{id}/cancel` | Cancel a subscription | PARENT (owner) |

### 💰 Payments — `/api/v1/payments`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/checkout` | Create a Stripe Checkout Session | PARENT |
| `GET` | `/` | List own payments | PARENT |
| `GET` | `/{id}` | Payment details (own only) | PARENT |
| `POST` | `/webhook` | Stripe Webhook with signature verification | Public (Stripe) |

### 📊 Subscription Plans — `/api/v1/subscription-plans`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `GET` | `/` | List plans with filters | Authenticated |
| `GET` | `/{id}` | Plan details | Authenticated |
| `POST` | `/` | Create a plan | ADMIN |
| `PUT` | `/{id}` | Update a plan | ADMIN |
| `DELETE` | `/{id}` | Delete a plan | ADMIN |

### 🏋️ Coaches — `/api/v1/coaches`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `GET` | `/` | List coaches | Authenticated |
| `POST` | `/{id}/assign` | Assign COACH role | ADMIN |
| `DELETE` | `/{id}/revoke` | Revoke COACH role | ADMIN |
| `GET` | `/{id}/groups` | Coach's groups | COACH / ADMIN |

### ⚙️ Admin — `/api/v1/admin`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `GET` | `/stats` | Overall system statistics | ADMIN |
| `GET` | `/children` | All children in the system (with filters) | ADMIN |
| `GET` | `/children/{id}` | Child by id (without ownership check) | ADMIN |
| `DELETE` | `/children/{id}` | Delete any child (soft delete) | ADMIN |
| `GET` | `/subscriptions` | All subscriptions (filter by status) | ADMIN |
| `PUT` | `/subscriptions/{id}/activate` | Manual subscription activation | ADMIN |

---

## 📚 Swagger UI

After starting the application, the interactive API documentation is available at:

```
http://localhost:8080/swagger-ui.html
```

OpenAPI specification in JSON:

```
http://localhost:8080/api-docs
```

Swagger UI supports JWT authorization — click **Authorize** and enter `Bearer <token>`.

---

## 🧪 Testing

The project contains unit and integration tests for all layers.

```bash
# Run all tests
./gradlew test

# Run with detailed output
./gradlew test --info
```

### Test Coverage

| Layer | Tests |
|---|---|
| Integration (Controllers) | Auth, Child, Group, Enrollment, Parent, Subscription, Payment, Attendance |
| Unit (Services) | Auth, Child (+ admin ops), Group, Session, Enrollment, Subscription (+ payment history, admin activate), SubscriptionPlan, PaymentCleanup, Parent (coach revoke) |
| Unit (Listeners) | SubscriptionDeduction, SessionCancellation |
| Unit (Schedulers) | NotificationScheduler |
| Repository | Child (soft delete), Subscription (optimistic locking) |

> All integration tests run against a **real PostgreSQL container** via Testcontainers — H2 is not used. This guarantees that Liquibase migrations, `@SQLDelete`, and complex JPQL queries work as they do in production.

---

## 🗄️ Database

The DB schema is managed via **Liquibase** and versioned as migrations:

```
src/main/resources/db/changelog/migrations/
├── 001-create-parents-table.xml
├── 002-create-parent-notifications-table.xml
├── 003-create-refresh-tokens-table.xml
├── 004-create-verification-tokens-table.xml
├── 005-create-sections-table.xml
├── 006-create-groups-table.xml
├── 007-create-children-table.xml
├── 008-create-schedules-table.xml
├── 009-create-sessions-table.xml
├── 010-create-attendance-table.xml
├── 011-create-subscription-plans-table.xml
├── 012-create-subscriptions-table.xml
└── 013-create-payments-table.xml
```

Migrations are applied automatically on application startup. There is no physical deletion — all main entities have an `is_deleted` column and `@SQLRestriction` at the Hibernate level.

---

## ⚙️ Configuration

The application supports Spring profiles:

| Profile | File | Description |
|---|---|---|
| `dev` (default) | `application-dev.yaml` | Local development, PostgreSQL |
| `test` | `application-test.yaml` | Tests, Testcontainers |

### Required Environment Variables

| Variable | Description |
|---|---|
| `JWT_SECRET_KEY` | Base64-encoded secret for signing JWT (minimum 256 bits) |
| `SENDGRID_API_KEY` | SendGrid API key for sending emails |
| `SENDGRID_FROM_EMAIL` | Sender email address |
| `STRIPE_SECRET_KEY` | Stripe secret key (`sk_test_...` or `sk_live_...`) |
| `STRIPE_WEBHOOK_SECRET` | Webhook signing secret (`whsec_...`) |
| `TELEGRAM_BOT_TOKEN` | Telegram bot token for notifications |

---

## 🔄 CI/CD

GitHub Actions automatically runs the pipeline on every push or pull request to any branch:

1. **Checkout** — clone the repository
2. **Setup JDK 21** (Temurin distribution, Gradle caching)
3. **Build & Test** — `./gradlew clean build` with Testcontainers
4. **Upload Test Report** — artifact with HTML report on test failure
5. **Docker Build** — verify that the Dockerfile compiles

Configuration: `.github/workflows/pipeline.yml`

---

## 🔒 Security

- Passwords are stored hashed (BCrypt)
- Stateless JWT authentication — the server does not store sessions
- Refresh tokens are stored in the DB and deleted on logout
- `JwtAuthenticationFilter` checks `isEnabled()` and `isEmailVerified()` on every request
- Webhook endpoint verifies the Stripe HMAC signature before processing
- Soft delete — data is not physically deleted

---

## 👤 Author

**Denis Koriavets**

[![GitHub](https://img.shields.io/badge/GitHub-DenisKoriavets-181717?logo=github)](https://github.com/DenisKoriavets)
