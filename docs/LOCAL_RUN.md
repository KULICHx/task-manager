# Локальный запуск (Windows)

Это инструкция для **локального запуска** двух сервисов. Автоматический сквозной запуск уже подтверждён в [GitHub Actions](https://github.com/KULICHx/task-manager/actions/workflows/smoke.yml); запуск на Windows пока необходимо проверить отдельно.

## Требования

- JDK 21 (`java -version` должен показывать `21`).
- Docker Desktop с Docker Compose v2 (`docker compose version`). Запустить Docker Desktop до выполнения команд.
- Доступ в интернет при первом запуске: Docker загружает образы, Maven Wrapper — Maven 3.9.12 и зависимости.
- Отдельно устанавливать Maven **не требуется**: каждый сервис содержит `mvnw.cmd`.

Команды ниже предназначены для PowerShell, запущенного в корне репозитория.

## 1. Запуск инфраструктуры

```powershell
Copy-Item .env.example .env
# При необходимости изменить учётные данные в .env и DB_* переменных приложений.
docker compose up -d

docker compose ps
```

Должны быть запущены `postgres`, `audit-postgres`, `redis`, `kafka`; сервис `kafka-init` должен завершиться с кодом 0 после создания топика `task-events`.

## 2. Проверка сборки

Откройте два PowerShell-окна из корня репозитория:

```powershell
cd api
.\mvnw.cmd clean test
```

```powershell
cd consumer
.\mvnw.cmd clean test
```

Интеграционный тест API использует Testcontainers, поэтому для выполнения тестов должен быть запущен Docker Desktop.

## 3. Запуск приложений

В одном терминале:

```powershell
cd api
.\mvnw.cmd spring-boot:run
```

В другом терминале:

```powershell
cd consumer
.\mvnw.cmd spring-boot:run
```

- Swagger UI: http://localhost:8081/swagger-ui.html
- OpenAPI JSON: http://localhost:8081/v3/api-docs
- Health: http://localhost:8081/actuator/health
- Kafka: `localhost:9092`
- PostgreSQL API: `localhost:5433`, БД `task_manager_db`
- PostgreSQL аудит: `localhost:5434`, БД `task_events_db`
- Redis: `localhost:6379`

## 4. Минимальная ручная проверка

Создайте пользователя, категорию и задачу через Swagger UI. Затем измените задачу и удалите её. Consumer должен сохранить три события в `task_event_audit` (типы `TASK_CREATED`, `TASK_UPDATED`, `TASK_DELETED`).

Посмотреть события:

```powershell
docker compose exec audit-postgres psql -U task_events -d task_events_db -c "SELECT event_type, task_id, title, received_at FROM task_event_audit ORDER BY received_at;"
```

Если вы меняли `AUDIT_POSTGRES_USER` или `AUDIT_POSTGRES_DB` в `.env`, замените их в этой команде.

## Остановка

```powershell
docker compose down
```

Тома PostgreSQL/Kafka при этом сохраняются. `docker compose down -v` **удалит данные**.

## Важно

- Эта конфигурация предназначена для локальной разработки, а не для публикации в интернете.
- `.env` исключён из Git. Значения по умолчанию — только для локальной машины.
- Текущий механизм `AFTER_COMMIT` не гарантирует доставку Kafka-событий при падении брокера. Надёжная доставка (outbox) — отдельная задача после проверки базового сценария.
