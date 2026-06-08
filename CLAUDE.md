# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A minimal Spring Boot demo that produces synthetic log messages to a Kafka topic and
consumes them back, filtering for `ERROR`-level entries. It is a learning/reference
project, not a production service — there are no REST endpoints, no persistence, and
output goes to stdout via `System.out.println`.

## Build & run

```bash
docker compose up -d          # start Kafka + Zookeeper (required before running the app)
mvn clean package             # compile and run tests
mvn spring-boot:run           # run the app (or: java -cp target/classes ... via your IDE)
mvn test                      # run all tests
mvn test -Dtest=ClassName#method   # run a single test
```

Note: `pom.xml` does not declare the `spring-boot-maven-plugin`, so `mvn spring-boot:run`
and an executable fat jar are not available out of the box. The app is normally launched
from the IDE (run `KafkaRealTimeLogProcessorApplication`). Adding the plugin to `pom.xml`
requires explicit approval per the workspace rules.

Kafka must be reachable at `localhost:9092` (see `docker-compose.yml`) before the app
starts, or the producer/consumer will fail to connect.

## Architecture

The whole flow lives in three classes under `com.rishav.kafka`, all wired by Spring Boot
autoconfiguration over the `logs` topic:

- `producer/LogProducer` — annotated `@PostConstruct @Async`, so it fires **once at startup**
  on a background thread. It emits 50 `LogMessage` records (random `INFO`/`DEBUG`/`ERROR`
  level) as JSON strings via `KafkaTemplate`, keyed by level, with a 200ms gap between sends.
  `@EnableAsync` on the main application class is what makes the `@Async` non-blocking.
- `consumer/ErrorLogConsumer` — a `@KafkaListener` on `logs` (group `error-log-group-dev`).
  Deserializes each message and prints only those with level `ERROR`.
- `model/LogMessage` — Lombok POJO (`level`, `timestamp`, `message`). Serialized/deserialized
  manually with a per-class `new ObjectMapper()` (not the Spring-managed bean).

Messages move as plain `String` JSON: Kafka serializers are `StringSerializer`/
`StringDeserializer` (configured in `application.yml`), and JSON conversion is done
explicitly with Jackson in both the producer and consumer rather than via a Kafka JSON serde.

## Conventions specific to this repo

- Java 22 (`maven.compiler.release` in `pom.xml`).
- Lombok is used for model boilerplate (`@Getter/@Setter/@AllArgsConstructor` etc.).
- The consumer group id is duplicated in both `application.yml` and the `@KafkaListener`
  annotation — keep them in sync if you change it.
- There are currently no test classes despite the JUnit 5 setup in `pom.xml`.
