#!/usr/bin/env bash
set -Eeuo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/.."
mkdir -p smoke-logs

cleanup() {
  local status=$?
  trap - EXIT
  if (( status != 0 )); then
    echo "Smoke test failed; printing application logs."
    tail -n 150 smoke-logs/api.log 2>/dev/null || true
    tail -n 150 smoke-logs/consumer.log 2>/dev/null || true
    docker compose ps || true
    docker compose logs --tail=80 || true
  fi
  if [[ -n "${api_pid:-}" ]]; then kill "$api_pid" 2>/dev/null || true; fi
  if [[ -n "${consumer_pid:-}" ]]; then kill "$consumer_pid" 2>/dev/null || true; fi
  exit "$status"
}
trap cleanup EXIT

echo "Checking infrastructure and creating Kafka topic..."
docker compose config --quiet
docker compose up -d --wait postgres audit-postgres redis kafka
docker compose run --rm kafka-init

echo "Packaging applications..."
(cd api && ./mvnw -B -ntp -DskipTests package)
(cd consumer && ./mvnw -B -ntp -DskipTests package)

echo "Starting consumer and API..."
java -jar consumer/target/task-event-consumer-0.0.1-SNAPSHOT.jar > smoke-logs/consumer.log 2>&1 &
consumer_pid=$!
java -jar api/target/taskmanager-0.0.1-SNAPSHOT.jar > smoke-logs/api.log 2>&1 &
api_pid=$!

echo "Waiting for API health..."
ready=0
for attempt in {1..90}; do
  if curl --silent --fail http://localhost:8081/actuator/health | jq -e '.status == "UP"' >/dev/null 2>&1; then
    ready=1
    break
  fi
  if ! kill -0 "$api_pid" 2>/dev/null || ! kill -0 "$consumer_pid" 2>/dev/null; then
    echo "One of the Java services exited during startup."
    exit 1
  fi
  sleep 2
done
if (( ready != 1 )); then
  echo "API did not become healthy within 180 seconds."
  exit 1
fi

echo "Testing full CRUD and event processing..."
email="smoke-$(date +%s)@example.test"
user_id=$(curl --fail-with-body --silent --show-error -X POST http://localhost:8081/api/users \
  -H 'Content-Type: application/json' \
  -d "{\"name\":\"CI Smoke Test\",\"email\":\"$email\"}" | jq -er '.id')
category_id=$(curl --fail-with-body --silent --show-error -X POST http://localhost:8081/api/categories \
  -H 'Content-Type: application/json' \
  -d '{"name":"CI Smoke Category"}' | jq -er '.id')

task_id=$(curl --fail-with-body --silent --show-error -X POST http://localhost:8081/api/tasks \
  -H 'Content-Type: application/json' \
  -d "{\"title\":\"Initial task\",\"description\":\"E2E smoke test\",\"completed\":false,\"userId\":$user_id,\"categoryId\":$category_id,\"type\":\"WORK\"}" | jq -er '.id')

curl --fail-with-body --silent --show-error "http://localhost:8081/api/tasks/$task_id" | jq -e '.title == "Initial task"' >/dev/null
curl --fail-with-body --silent --show-error -X PUT "http://localhost:8081/api/tasks/$task_id" \
  -H 'Content-Type: application/json' \
  -d "{\"title\":\"Updated task\",\"description\":\"E2E smoke test\",\"completed\":true,\"userId\":$user_id,\"categoryId\":$category_id,\"type\":\"WORK\"}" | jq -e '.title == "Updated task" and .completed == true' >/dev/null
curl --fail-with-body --silent --show-error -X DELETE "http://localhost:8081/api/tasks/$task_id" >/dev/null

echo "Waiting for created/updated/deleted events to reach audit PostgreSQL..."
verified=0
for attempt in {1..60}; do
  count=$(docker compose exec -T audit-postgres psql -U task_events -d task_events_db -Atc \
    "SELECT COUNT(*) FROM task_event_audit WHERE task_id = $task_id AND event_type IN ('TASK_CREATED','TASK_UPDATED','TASK_DELETED');")
  if [[ "$count" == "3" ]]; then
    verified=1
    break
  fi
  sleep 2
done
if (( verified != 1 )); then
  echo "Expected 3 audit events for task $task_id, got ${count:-unknown}."
  exit 1
fi

docker compose exec -T audit-postgres psql -U task_events -d task_events_db -c \
  "SELECT event_type, task_id, title, received_at FROM task_event_audit WHERE task_id = $task_id ORDER BY received_at;"
echo "Full-stack smoke test passed: task ID $task_id, all 3 Kafka events persisted."
