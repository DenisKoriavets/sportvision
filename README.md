# 🏆 SportVision API

> RESTful бекенд-застосунок для управління дитячими спортивними секціями на базі Spring Boot 4

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.4-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?logo=postgresql)](https://www.postgresql.org/)
[![CI](https://github.com/DenisKoriavets/sportvision/actions/workflows/pipeline.yml/badge.svg)](https://github.com/DenisKoriavets/sportvision/actions)

---

## 📖 Про проєкт

**SportVision API** — це повноцінний RESTful бекенд для автоматизації роботи дитячих спортивних секцій. Система працює через батьків: батько реєструє акаунт, додає дітей і керує їхньою участю в тренуваннях — записує на заняття, оплачує абонементи та отримує нотифікації.

Бізнес-сторона системи контролює хто реально відвідує тренування і хто за них платить. Тренери відмічають відвідування через API, і система автоматично списує заняття з абонементу. Якщо абонемент закінчився — дитина не може бути записана на нові заняття. Сервіс бере на себе всю комунікацію з батьками: нагадування про тренування, повідомлення про зміни розкладу, попередження про закінчення абонементу.

---

## ✨ Функціональність

### 🔐 Автентифікація та безпека
- Реєстрація з підтвердженням email через SendGrid
- Stateless JWT автентифікація з **Access (15 хв) + Refresh (7 днів) токенами**
- Ротація refresh токенів при кожному оновленні
- Кастомна валідація паролю через `@Password` анотацію
- Деактивація акаунтів адміністратором — заблокований користувач не може використовувати навіть валідний токен
- Rate limit на повторне надсилання листа верифікації (раз на 2 хвилини)

### 👨‍👩‍👧 Управління дітьми та групами
- Батько може мати необмежену кількість дітей, кожна — окремий учасник
- Запис дитини в групу перевіряє: вік, місткість групи та наявність активного абонементу
- Динамічна фільтрація груп через **JPA Specification + Criteria API**: секція, тренер, вік, наявність місць (реалізовано через subquery)
- `currentOccupancy` обчислюється через `@Formula` без окремого поля в БД

### 📋 Відвідування та списання занять
Ключовий бізнес-процес реалізований через **Event-Driven архітектуру**:

```
Тренер відмічає відвідування
    → AttendanceMarkedEvent
        → SubscriptionDeductionListener: списує заняття
            → SubscriptionExpiredEvent / SubscriptionLowEvent
                → NotificationDispatchListener: нотифікація батьку
```

- Масове відмічання відвідування одним запитом
- Списання тільки при статусі `PRESENT`, ABSENT і EXCUSED не списують
- **Optimistic locking** через `@Version` на Subscription запобігає race condition
- При скасуванні заняття — заняття повертаються на абонемент через `SessionCancellationListener`

### 💳 Платіжна система (Stripe)
- Stripe Checkout redirect-based флоу
- Верифікація підпису webhook через `Webhook.constructEvent()` — захист від підроблених запитів
- Idempotency обробки webhook: повторний `checkout.session.completed` ігнорується
- Scheduled job кожні 30 хвилин скасовує PENDING платежі старші 1 години

### 🔔 Нотифікації (Strategy Pattern)
Три канали реалізовані через патерн **Strategy** з асинхронною відправкою:
- **Email** — HTML шаблони через Thymeleaf + SendGrid REST API
- **Telegram** — відправка через Telegram Bot API без зайвих бібліотек

Батько вибирає канали у профілі. Якщо preferences порожні — fallback на Email.

### 📅 Заплановані задачі
- `SessionReminderJob` — щодня о 18:00 нагадує про завтрашні тренування
- `SubscriptionExpiryJob` — щодня о 9:00 попереджає про абонементи з ≤2 заняттями
- `PaymentExpiryJob` — кожні 30 хвилин скасовує прострочені платежі

---

## 🛠️ Технологічний стек

| Категорія | Технологія |
|---|---|
| Мова | Java 21 |
| Фреймворк | Spring Boot 4.0.4 |
| Безпека | Spring Security + JJWT 0.12.6 |
| ORM | Spring Data JPA / Hibernate |
| БД (prod) | PostgreSQL 15 |
| БД (test) | Testcontainers + PostgreSQL |
| Міграції | Liquibase |
| Валідація | Jakarta Bean Validation + кастомні анотації |
| Маппінг | MapStruct 1.5.5 |
| AOP | Spring AOP (AspectJ) |
| Фільтрація | JPA Specification / Criteria API |
| Платежі | Stripe Java SDK |
| Email | SendGrid Java SDK + Thymeleaf шаблони |
| Telegram | Telegram Bot API (REST) |
| Документація | SpringDoc OpenAPI 3.0 (Swagger UI) |
| Тестування | JUnit 5 + Mockito + Testcontainers |
| Збірка | Gradle |
| Контейнери | Docker + Docker Compose |
| CI/CD | GitHub Actions |

---

## 🏗️ Архітектура

Проєкт побудовано за **Layered Architecture** з чітким розділенням відповідальності:

```
Controller → Service → Repository → Database
               ↕
         Event Publisher
               ↕
           Listeners
               ↕
     Notification Channels
```

Сервіси не викликають один одного напряму — комунікація через `ApplicationEventPublisher`. Це забезпечує loose coupling між модулями відвідування, абонементів і нотифікацій.

### Структура пакетів

```
src/main/java/com/github/deniskoriavets/sportvision/
├── aspect/             # LoggingAspect, AuditAspect, ExceptionLoggingAspect
├── config/             # JWT, Stripe, SendGrid, AsyncConfig, OpenAPI
├── controller/         # REST-контролери
├── dto/                # Request / Response / Criteria records
├── entity/             # JPA-сутності з soft delete (@SQLDelete)
│   └── enums/          # Role, SessionStatus, SubscriptionStatus, ...
├── event/              # Domain events (records)
├── exception/          # GlobalExceptionHandler + кастомні виключення
├── listener/           # @TransactionalEventListener handlers
├── mapper/             # MapStruct compile-time маппери
├── notification/       # NotificationStrategy, NotificationDispatcher
├── repository/         # Spring Data JPA + JpaSpecificationExecutor
│   └── specification/  # Criteria API builders
├── scheduler/          # @Scheduled jobs
├── security/           # JWT filter, SecurityFacade, UserDetailsService
├── service/            # interfaces + impl
└── validation/         # @Password, @ValidAgePeriod, @ValidPhoneNumber, ...
```

---

## 🚀 Запуск проєкту

### Передумови

- [Docker](https://docs.docker.com/get-docker/) та [Docker Compose](https://docs.docker.com/compose/)
- [JDK 21](https://adoptium.net/) — для локального запуску без Docker

### 1. Клонування репозиторію

```bash
git clone https://github.com/DenisKoriavets/sportvision.git
cd sportvision
```

### 2. Налаштування змінних середовища

Створіть файл `.env` у корені проєкту:

```env
JWT_SECRET_KEY=your-very-secret-key-at-least-256-bits-long-base64-encoded

SENDGRID_API_KEY=SG.your_sendgrid_api_key
SENDGRID_FROM_EMAIL=noreply@yourdomain.com

STRIPE_SECRET_KEY=sk_test_your_stripe_secret_key
STRIPE_PUBLISHABLE_KEY=pk_test_your_stripe_publishable_key
STRIPE_WEBHOOK_SECRET=whsec_your_webhook_secret

TELEGRAM_BOT_TOKEN=your_telegram_bot_token
```

> ⚠️ Ніколи не комітьте `.env` у репозиторій. Він вже є у `.gitignore`.

### 3. Запуск через Docker Compose

```bash
# Зібрати Docker-образ застосунку
docker build -t sportvision-api:latest .

# Запустити застосунок та базу даних
docker compose up -d
```

Застосунок буде доступний за адресою: **http://localhost:8080**

### 4. Локальний запуск (без Docker)

Переконайтесь, що PostgreSQL запущено та `.env` заповнено, потім:

```bash
./gradlew bootRun
```

---

## 📋 API Endpoints

### 🔐 Auth — `/api/v1/auth`

| Метод | Endpoint | Опис | Auth |
|---|---|---|---|
| `POST` | `/register` | Реєстрація батька | ❌ |
| `POST` | `/login` | Вхід, отримання JWT пари | ❌ |
| `POST` | `/refresh` | Оновлення access token | ❌ |
| `POST` | `/logout` | Logout, інвалідація refresh token | ✅ |
| `GET` | `/verify?token={token}` | Підтвердження email | ❌ |
| `POST` | `/resend-verification` | Повторна відправка листа (rate limit: 2 хв) | ❌ |

### 👤 Батьки — `/api/v1/parents`

| Метод | Endpoint | Опис | Auth |
|---|---|---|---|
| `GET` | `/me` | Отримати свій профіль | PARENT |
| `PUT` | `/me` | Оновити профіль + notification preferences | PARENT |
| `POST` | `/me/telegram/link` | Прив'язати Telegram чат | PARENT |
| `GET` | `/` | Список всіх батьків | ADMIN |
| `PUT` | `/{id}/deactivate` | Деактивувати акаунт | ADMIN |

### 👶 Діти — `/api/v1/children`

| Метод | Endpoint | Опис | Auth |
|---|---|---|---|
| `GET` | `/` | Список своїх дітей | PARENT |
| `POST` | `/` | Додати дитину | PARENT |
| `GET` | `/{id}` | Деталі дитини | PARENT (owner) |
| `PUT` | `/{id}` | Оновити дані дитини | PARENT (owner) |
| `DELETE` | `/{id}` | Видалити дитину (soft delete) | PARENT (owner) |
| `GET` | `/{id}/attendance` | Історія відвідувань | PARENT (owner) |
| `GET` | `/{id}/subscriptions` | Абонементи дитини | PARENT (owner) |
| `GET` | `/search` | Пошук дітей (фільтри) | Authenticated |

### 📝 Запис у групи — `/api/v1/enrollments`

| Метод | Endpoint | Опис | Auth |
|---|---|---|---|
| `POST` | `/` | Записати дитину в групу | PARENT |
| `DELETE` | `/` | Відрахувати дитину з групи | PARENT |

### 🏟️ Секції — `/api/v1/sections`

| Метод | Endpoint | Опис | Auth |
|---|---|---|---|
| `GET` | `/` | Список секцій з фільтрами | Authenticated |
| `POST` | `/` | Створити секцію | ADMIN |
| `GET` | `/{id}` | Деталі секції | Authenticated |
| `PUT` | `/{id}` | Оновити секцію | ADMIN |
| `DELETE` | `/{id}` | Видалити секцію (soft delete) | ADMIN |

### 👥 Групи — `/api/v1/groups`

| Метод | Endpoint | Опис | Auth |
|---|---|---|---|
| `GET` | `/` | Список груп (фільтри: секція, тренер, вік, місця) | Authenticated |
| `POST` | `/` | Створити групу | ADMIN |
| `GET` | `/{id}` | Деталі групи | Authenticated |
| `PUT` | `/{id}` | Оновити групу | ADMIN |
| `DELETE` | `/{id}` | Видалити групу (soft delete) | ADMIN |
| `GET` | `/{id}/children` | Список дітей у групі | COACH / ADMIN |

### 📅 Розклад — `/api/v1/schedules`

| Метод | Endpoint | Опис | Auth |
|---|---|---|---|
| `POST` | `/` | Додати слот розкладу | ADMIN / COACH |
| `GET` | `/group/{groupId}` | Розклад групи | Authenticated |
| `DELETE` | `/{id}` | Видалити слот | ADMIN / COACH |

### 🎯 Заняття — `/api/v1/sessions`

| Метод | Endpoint | Опис | Auth |
|---|---|---|---|
| `POST` | `/generate` | Згенерувати заняття за розкладом | ADMIN |
| `POST` | `/` | Створити разове заняття | ADMIN / COACH |
| `PUT` | `/{id}/cancel` | Скасувати заняття | ADMIN / COACH |
| `POST` | `/{id}/attendance` | Відмітити відвідування (масово) | COACH |
| `PUT` | `/{sessionId}/attendance/{childId}` | Виправити відмітку (тільки для SCHEDULED) | COACH / ADMIN |
| `GET` | `/group/{groupId}` | Заняття групи за датами | Authenticated |
| `GET` | `/` | Пошук занять з фільтрами | Authenticated |

### 🎫 Абонементи — `/api/v1/subscriptions`

| Метод | Endpoint | Опис | Auth |
|---|---|---|---|
| `POST` | `/admin/buy` | Купити абонемент за готівку | ADMIN |
| `GET` | `/child/{childId}` | Абонементи дитини | PARENT |
| `GET` | `/{id}` | Деталі абонементу | PARENT (owner) |
| `PUT` | `/{id}/cancel` | Скасувати абонемент | PARENT (owner) |

### 💰 Платежі — `/api/v1/payments`

| Метод | Endpoint | Опис | Auth |
|---|---|---|---|
| `POST` | `/checkout` | Створити Stripe Checkout Session | PARENT |
| `GET` | `/` | Список своїх платежів | PARENT |
| `GET` | `/{id}` | Деталі платежу (тільки свої) | PARENT |
| `POST` | `/webhook` | Stripe Webhook з верифікацією підпису | Public (Stripe) |

### 📊 Тарифні плани — `/api/v1/subscription-plans`

| Метод | Endpoint | Опис | Auth |
|---|---|---|---|
| `GET` | `/` | Список планів з фільтрами | Authenticated |
| `GET` | `/{id}` | Деталі плану | Authenticated |
| `POST` | `/` | Створити план | ADMIN |
| `PUT` | `/{id}` | Оновити план | ADMIN |
| `DELETE` | `/{id}` | Видалити план | ADMIN |

### 🏋️ Тренери — `/api/v1/coaches`

| Метод | Endpoint | Опис | Auth |
|---|---|---|---|
| `GET` | `/` | Список тренерів | Authenticated |
| `POST` | `/{id}/assign` | Призначити роль COACH | ADMIN |
| `DELETE` | `/{id}/revoke` | Відкликати роль COACH | ADMIN |
| `GET` | `/{id}/groups` | Групи тренера | COACH / ADMIN |

### ⚙️ Адмін — `/api/v1/admin`

| Метод | Endpoint | Опис | Auth |
|---|---|---|---|
| `GET` | `/stats` | Загальна статистика системи | ADMIN |
| `GET` | `/children` | Всі діти в системі (з фільтрами) | ADMIN |
| `GET` | `/children/{id}` | Дитина по id (без ownership перевірки) | ADMIN |
| `DELETE` | `/children/{id}` | Видалити будь-яку дитину (soft delete) | ADMIN |
| `GET` | `/subscriptions` | Всі абонементи (фільтр по статусу) | ADMIN |
| `PUT` | `/subscriptions/{id}/activate` | Ручна активація абонементу | ADMIN |

---

## 📚 Swagger UI

Після запуску застосунку інтерактивна документація API доступна за адресою:

```
http://localhost:8080/swagger-ui.html
```

Специфікація OpenAPI у JSON:

```
http://localhost:8080/api-docs
```

Swagger UI підтримує JWT авторизацію — натисніть **Authorize** і введіть `Bearer <token>`.

---

## 🧪 Тестування

Проєкт містить юніт- та інтеграційні тести для всіх шарів.

```bash
# Запустити всі тести
./gradlew test

# Запустити з детальним виводом
./gradlew test --info
```

### Покриття тестами

| Шар | Тести |
|---|---|
| Integration (Controllers) | Auth, Child, Group, Enrollment, Parent, Subscription, Payment, Attendance |
| Unit (Services) | Auth, Child (+ admin ops), Group, Session, Enrollment, Subscription (+ payment history, admin activate), SubscriptionPlan, PaymentCleanup, Parent (coach revoke) |
| Unit (Listeners) | SubscriptionDeduction, SessionCancellation |
| Unit (Schedulers) | NotificationScheduler |
| Repository | Child (soft delete), Subscription (optimistic locking) |

> Усі integration тести запускаються проти **реального PostgreSQL контейнеру** через Testcontainers — H2 не використовується. Це гарантує що Liquibase міграції, `@SQLDelete` і складні JPQL запити працюють як у prod.

---

## 🗄️ База даних

Схема БД керується через **Liquibase** і версіонується у вигляді міграцій:

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

Міграції застосовуються автоматично при старті застосунку. Фізичного видалення немає — всі основні entity мають `is_deleted` колонку і `@SQLRestriction` на рівні Hibernate.

---

## ⚙️ Конфігурація

Застосунок підтримує профілі Spring:

| Профіль | Файл | Опис |
|---|---|---|
| `dev` (default) | `application-dev.yaml` | Локальна розробка, PostgreSQL |
| `test` | `application-test.yaml` | Тести, Testcontainers |

### Необхідні змінні середовища

| Змінна | Опис |
|---|---|
| `JWT_SECRET_KEY` | Base64-encoded секрет для підписання JWT (мінімум 256 біт) |
| `SENDGRID_API_KEY` | API ключ SendGrid для відправки email |
| `SENDGRID_FROM_EMAIL` | Email відправника |
| `STRIPE_SECRET_KEY` | Secret key Stripe (`sk_test_...` або `sk_live_...`) |
| `STRIPE_WEBHOOK_SECRET` | Webhook signing secret (`whsec_...`) |
| `TELEGRAM_BOT_TOKEN` | Токен Telegram бота для нотифікацій |

---

## 🔄 CI/CD

GitHub Actions автоматично запускає pipeline при кожному push або pull request у будь-яку гілку:

1. **Checkout** — клонування репозиторію
2. **Setup JDK 21** (Temurin distribution, кешування Gradle)
3. **Build & Test** — `./gradlew clean build` із запуском Testcontainers
4. **Upload Test Report** — артефакт з HTML звітом при падінні тестів
5. **Docker Build** — перевірка що Dockerfile компілюється

Конфігурація: `.github/workflows/pipeline.yml`

---

## 🔒 Безпека

- Паролі зберігаються у хешованому вигляді (BCrypt)
- Stateless JWT автентифікація — сервер не зберігає сесії
- Refresh токени зберігаються у БД і видаляються при logout
- `JwtAuthenticationFilter` перевіряє `isEnabled()` і `isEmailVerified()` при кожному запиті
- Webhook ендпоінт верифікує HMAC підпис Stripe перед обробкою
- Soft delete — дані фізично не видаляються

---

## 👤 Автор

**Denis Koriavets**

[![GitHub](https://img.shields.io/badge/GitHub-DenisKoriavets-181717?logo=github)](https://github.com/DenisKoriavets)
