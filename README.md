# Bank Cards API

REST API для управления пользователями, банковскими картами и переводами между ними.

Стек: **Spring Boot**, **JWT**, **Spring Security**, **PostgreSQL**, **Liquibase**.

---

## Базовая информация

- **URL:** `http://localhost:8080`
- **Формат:** JSON
- **Аутентификация:** JWT через Bearer Token
- **Безопасность:** Номера карт хранятся в зашифрованном виде (AES) и возвращаются в API только в виде маски `**** **** **** 1234`
- **Роли:**
    - `ADMIN` — полный доступ к системе
    - `USER` — работа только со своими картами

---
## Запуск

Необходим docker

Запускаем команду в папке проекта
```
docker compose up --build
```
В первом запуске автоматически создатся admin пользователь с данными:
```
username: admin
password: password
```
### Swagger UI/OpenApi
#### url `http://localhost:8080/swagger-ui/index.html#/`

## Аутентификация

### Логин

**POST** `/auth/login`

Получаем токен для дальнейшей работы.

```json
{
  "username": "admin",
  "password": "password"
}
```

**Ответ:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

Дальше этот токен нужно прокидывать в заголовке:

```
Authorization: Bearer <ваш_токен>
```

---

## Пользователи (`/api/v1/users`)

Почти все эндпоинты требуют права администратора.

### Создать клиента

**POST** `/api/v1/users/create/client` (только ADMIN)

```json
{
  "username": "user1",
  "password": "password"
}
```

### Создать админа

**POST** `/api/v1/users/create/admin` (только ADMIN)

```json
{
  "username": "admin2",
  "password": "password"
}
```

### Получить пользователя

**GET** `/api/v1/users/{id}` (только ADMIN)

### Список всех пользователей

**GET** `/api/v1/users?page=0&size=10&sort=username,asc` (только ADMIN)

Параметры:
- `page` — номер страницы (начинается с 0)
- `size` — количество на странице
- `sort` — сортировка, например `username,asc`

**Пример запроса:**
```
GET /api/v1/users?page=0&size=5&sort=id,desc
```

**Пример ответа:**
```json
{
  "content": [
    {
      "id": 3,
      "username": "admin2",
      "role": "ADMIN"
    },
    {
      "id": 2,
      "username": "user1",
      "role": "USER"
    },
    {
      "id": 1,
      "username": "admin",
      "role": "ADMIN"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 5,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    }
  },
  "totalPages": 1,
  "totalElements": 3,
  "last": true,
  "first": true,
  "size": 5,
  "number": 0,
  "numberOfElements": 3,
  "empty": false
}
```

### Сменить username другому пользователю

**PUT** `/api/v1/users/username/{id}` (только ADMIN)

```json
{
  "username": "new_username"
}
```

### Сменить свой username

**PUT** `/api/v1/users`

```json
{
  "username": "my_new_username"
}
```

### Сменить свой пароль

**PATCH** `/api/v1/users`

```json
{
  "oldPassword": "old_pass",
  "newPassword": "new_pass"
}
```

Возвращает `201 CREATED`

### Изменить роль пользователя

**PUT** `/api/v1/users/role/{id}` (только ADMIN)

```json
{
  "role": "ADMIN"
}
```

### Удалить пользователя

**DELETE** `/api/v1/users/{id}` (только ADMIN)

Возвращает `204 NO CONTENT`

---

## Карты (`/api/v1/cards`)

### Создать карту

**POST** `/api/v1/cards` (только ADMIN)

```json
{
  "cardNumber": "1234567890123456",
  "owner": "Ivan Petrov",
  "expirationDate": "2027-12-31",
  "userId": 1
}
```

> ⚠️ Номер карты будет зашифрован и сохранён в базе. В ответах API номер отображается только в виде маски.

### Получить карту по ID

**GET** `/api/v1/cards/{id}` (только ADMIN)

### Карты конкретного пользователя

**GET** `/api/v1/cards/user/{userId}?page=0&size=10&sort=cardNumber,asc` (только ADMIN)

**Пример запроса:**
```
GET /api/v1/cards/user/1?page=0&size=3&sort=expirationDate,desc
```

**Пример ответа:**
```json
{
  "content": [
    {
      "id": 1,
      "maskedNumber": "**** **** **** 3456",
      "owner": "Ivan Petrov",
      "expirationDate": "2027-12-31",
      "status": "ACTIVE",
      "balance": 1500.00
    },
    {
      "id": 2,
      "maskedNumber": "**** **** **** 7654",
      "owner": "Ivan Petrov",
      "expirationDate": "2026-06-31",
      "status": "ACTIVE",
      "balance": 500.00
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 3
  },
  "totalPages": 1,
  "totalElements": 2,
  "last": true,
  "first": true,
  "numberOfElements": 2
}
```

### Мои карты

**GET** `/api/v1/cards/my?page=0&size=10&sort=balance,desc`

Вернёт все карты текущего пользователя.

**Пример запроса:**
```
GET /api/v1/cards/my?page=0&size=5&sort=balance,desc
```

**Пример ответа:**
```json
{
  "content": [
    {
      "id": 1,
      "maskedNumber": "**** **** **** 3456",
      "owner": "Ivan Petrov",
      "expirationDate": "2027-12-31",
      "status": "ACTIVE",
      "balance": 1500.00
    },
    {
      "id": 2,
      "maskedNumber": "**** **** **** 7654",
      "owner": "Ivan Petrov",
      "expirationDate": "2026-06-31",
      "status": "ACTIVE",
      "balance": 500.00
    }
  ],
  "totalPages": 1,
  "totalElements": 2,
  "first": true,
  "last": true
}
```

### Все карты в системе

**GET** `/api/v1/cards?page=0&size=10&sort=id,asc` (только ADMIN)

**Пример запроса:**
```
GET /api/v1/cards?page=0&size=10&sort=owner,asc
```

**Пример ответа:**
```json
{
  "content": [
    {
      "id": 1,
      "maskedNumber": "**** **** **** 3456",
      "owner": "Ivan Petrov",
      "expirationDate": "2027-12-31",
      "status": "ACTIVE",
      "balance": 1500.00
    },
    {
      "id": 3,
      "maskedNumber": "**** **** **** 4444",
      "owner": "Maria Ivanova",
      "expirationDate": "2028-03-31",
      "status": "ACTIVE",
      "balance": 2000.00
    },
    {
      "id": 4,
      "maskedNumber": "**** **** **** 8888",
      "owner": "Petr Sidorov",
      "expirationDate": "2025-01-31",
      "status": "BLOCKED",
      "balance": 0.00
    }
  ],
  "totalPages": 1,
  "totalElements": 3
}
```

### Карты по статусу

**GET** `/api/v1/cards/status?status=ACTIVE&page=0&size=10` (только ADMIN)

Доступные статусы:
- `ACTIVE`
- `BLOCKED`
- `EXPIRED`

**Пример запроса:**
```
GET /api/v1/cards/status?status=BLOCKED&page=0&size=5&sort=expirationDate,asc
```

**Пример ответа:**
```json
{
  "content": [
    {
      "id": 4,
      "maskedNumber": "**** **** **** 8888",
      "owner": "Petr Sidorov",
      "expirationDate": "2025-01-31",
      "status": "BLOCKED",
      "balance": 0.00
    },
    {
      "id": 7,
      "maskedNumber": "**** **** **** 2222",
      "owner": "Anna Smirnova",
      "expirationDate": "2026-08-31",
      "status": "BLOCKED",
      "balance": 300.00
    }
  ],
  "totalPages": 1,
  "totalElements": 2,
  "first": true,
  "last": true
}
```

### Баланс конкретной карты

**GET** `/api/v1/cards/my/{cardId}/balance`

```json
{
  "cardId": 1,
  "balance": 500.00
}
```

### Общий баланс по всем картам

**GET** `/api/v1/cards/my/balance`

```json
{
  "cardId": null,
  "balance": 1500.00
}
```

### Пополнить баланс
**PUT** `/api/v1/cards/1/balance?amount=100`

### Снять с баланса
**PUT** `/api/v1/cards/1/balance?amount=-50`

### Перевод между картами

**POST** `/api/v1/cards/transfer`

```json
{
  "fromCardId": 1,
  "toCardId": 2,
  "amount": 100.00
}
```

### Изменить статус карты

**PUT** `/api/v1/cards/{cardId}/status` (только ADMIN)

```json
{
  "status": "BLOCKED"
}
```

### Удалить карту

**DELETE** `/api/v1/cards/{cardId}` (только ADMIN)

Возвращает `204 NO CONTENT`

---

## Частые ошибки

- `400` — что-то не так с данными в запросе
- `401` — токен не передан или неверный
- `403` — не хватает прав для этой операции
- `404` — карта/пользователь не найдены

---

## Как начать работать

1. Залогиньтесь через `/auth/login` и получите токен
2. Добавьте токен в заголовок `Authorization: Bearer <токен>`
3. Проверьте свою роль — от неё зависит доступ к эндпоинтам
4. Не забывайте про пагинацию при запросе списков

---

## Технологии

- Java 21
- Spring Boot
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL
- Liquibase
- Swagger
