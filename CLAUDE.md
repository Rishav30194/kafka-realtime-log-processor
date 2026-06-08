# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A small Spring Boot + Kafka demo that generates synthetic log messages, routes the
`ERROR`-level ones to a dedicated topic, and consumes them. It is a learning/reference
project — there are no REST endpoints and no persistence; observable behavior is via logs.

## Build & run

```bash
docker compose up -d          # start single-node Kafka (KRaft, no Zookeeper) on localhost:9092
mvn clean package             # compile, run tests, build executable jar
mvn spring-boot:run           # run the app
java -jar target/kafka-realtime-log-processor-1.0-SNAPSHOT.jar   # run the packaged fat jar
mvn test                      # run all tests
mvn test -Dtest=LogRoutingTest#errorLogsAreRoutedToErrorTopic   # run a single test
docker compose down           # stop the broker
```

Kafka must be reachable at `localhost:9092` before running the app. The tests do **not**
need Docker — they spin up an embedded broker (`@EmbeddedKafka`).

Tunable producer knobs (CLI `--flag=value`, env, or `application.yml` under `app.producer`):
`enabled` (default true), `count` (default 50), `delay-ms` (default 200).

## Architecture

The pipeline is a chain of three beans over two topics (constants in `config/KafkaTopics`):

```
LogProducer --> "logs" --> LogRouter --> "error-logs" --> ErrorLogConsumer
```

- `producer/LogProducer` — on `ApplicationReadyEvent` (async, so startup isn't blocked) emits
  `count` random-level `LogMessage` records as JSON to `logs`, keyed by level. Gated by
  `app.producer.enabled` so tests can disable the startup burst. `send()` is also called
  directly by the test.
- `consumer/LogRouter` — `@KafkaListener` on `logs` (group `log-router`); forwards only
  `ERROR` messages to `error-logs`.
- `consumer/ErrorLogConsumer` — `@KafkaListener` on `error-logs` (group `error-log-group-dev`);
  logs each error.
- `config/KafkaTopics` — declares both topics as `NewTopic` beans (auto-created via `KafkaAdmin`)
  and holds the topic-name constants.
- `config/JacksonConfig` — provides the shared `ObjectMapper` bean. **Required**: the plain
  (non-web) `spring-boot-starter` does not autoconfigure one.

Messages move as plain `String` JSON: Kafka uses String serdes (see `application.yml`), and
Jackson conversion is done explicitly in the producer/router/consumer.

## Conventions specific to this repo

- Java 22 (`maven.compiler.release`).
- Dependency versions are governed by the imported `spring-boot-dependencies` BOM — do **not**
  re-pin Spring/Kafka/Jackson/logging versions on individual dependencies, or you risk the kind
  of transitive split (e.g. `logback-core` vs `logback-classic`) the BOM exists to prevent.
- Each `@KafkaListener` declares its own `groupId`; there is intentionally no global
  `spring.kafka.consumer.group-id`, so the two consumers stay in separate groups.
- Lombok is used for the `LogMessage` model.
- `LogRoutingTest` uses `@EmbeddedKafka` and points `spring.kafka.bootstrap-servers` at
  `${spring.embedded.kafka.brokers}`; it seeks the test consumer to the end of `error-logs`
  before producing so leftover records from other tests don't leak in.
