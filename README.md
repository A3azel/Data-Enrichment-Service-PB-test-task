# Data Enrichment Service

Асинхронно обробляє повідомлення з RabbitMQ: збагачує їх через зовнішній REST API,
зберігає результат у PostgreSQL і публікує підсумок у вихідний exchange.

## Технологічний стек

- **Java 21**, **Spring Boot 4.1** (Web MVC, Data JPA, AMQP) + Lombok
- **PostgreSQL** — сховище результатів
- **RabbitMQ** — вхідна черга та вихідний exchange
- **Lombok** — скорочення boilerplate
- **JUnit 5 + Mockito** — unit-тести; **Testcontainers** — інтеграційний тест

## Потік обробки

```
inbound queue -> @RabbitListener -> перевірка дубля -> збагачення (POST, поза транзакцією)
              -> збереження (@Transactional) -> публікація в exchange (після коміту)
```

## Запуск

```bash
docker compose up -d      # PostgreSQL + RabbitMQ (таблиця створюється з db/init.sql)
mvn spring-boot:run
```

Застосунок стартує зі stub-сервісом (`app.enrichment-api.stub=true`), за умовами ТЗ замість рального сервісу використовується сервіс-заглушка

## Проядок роботи з застосунком

Надіслати тестове повідомлення: RabbitMQ UI (`localhost:15672`, guest/guest) -> черга
`enrichment.inbound.queue` -> Publish message, з властивістю `content_type: application/json`:

## Приклад JSON

```
{
  "messageId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "userId": 42,
  "action": "LOGIN",
  "timestamp": "2026-07-01 10:00:00.0"
}
```

В результаті запис з'явиться в таблиці `result` і буде опублікований у `enrichment.outbound.exchange`.

## Тести

- **Unit** (`EnrichmentServiceTest`, Mockito) — чотири сценарії `process`:
- новий messageId → збережено, опубліковано, `logId` = id рядка;
- дубль виявлено перевіркою → API, БД і publisher не викликаються;
- дубль виявлено unique-обмеженням → без винятку, без публікації;
- збій API → виняток пропагується, нічого не збережено й не опубліковано.
- **Smoke** (`contextLoads`) - піднімає контекст, ловить помилки конфігурації.
- **Integration** (`EnrichmentFlowIT`, Testcontainers) - весь флоу на реальних PostgreSQL і
  RabbitMQ: від вхідного повідомлення до рядка в БД та вихідного повідомлення.

  ## Архітектурні рішення

  ### Транзакції
  
  - Виклик зовнішнього API - поза транзакцією: HTTP-виклик усередині транзакції — антипатерн:
  з'єднання з пулу утримується на весь час мережевого запиту, тож при недоступному
  зовнішньому сервісі кожне повідомлення тримає з'єднання до таймауту — і під навантаженням
  connection pool впаде.
  - Транзакційний лише запис у БД (`ResultPersistenceService`).
  - Запис винесено в **окремий бін** - `@Transactional` працює через проксі, тож
    self-invocation усередині `EnrichmentService` тихо вимкнув би транзакцію.

  ### Ідемпотентність

  1. `UNIQUE` на `message_id` — гарантія на рівні БД.
  2. Попередня перевірка — відсікає більшість дублів.
 
  ### Обробка помилок

  - Бите повідомлення (`MessageConversionException`) -> одразу в DLQ
  - 4xx (`NonRetryableEnrichmentException`) -> одразу в DLQ (зроблено сепеціально щоб всі 4xx помилки одразу проскакували етап 3 спроби з backoff) 
  - 5xx / таймаут (`EnrichmentApiException`) -> 3 спроби з backoff -> DLQ
 
  ### Публікація після коміту

  `publish(...)` викликається після `save(...)` в результаті сервіс ніколи не відправить повідомлення, якие відкотилося

  ### Client

  Інтерфейс `EnrichmentClient` із двома реалізаціями, які перемикаються через `app.enrichment-api.stub`

  - `EnrichmentClientImpl` — псевдо-робочий код. HTTP POST через `RestClient`, класифікація 4xx/5xx.
  - `StubEnrichmentClient` — заглушка, за умовами ТЗ.
   
  >Author: Ihor Losinskyi
