# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Start infrastructure (ActiveMQ broker required for JMS routes)
docker compose up -d

# Development mode with live reload
mvn quarkus:dev

# Run tests (uses in-VM broker via application-test.properties)
mvn test

# Run a single test
mvn test -Dtest=MyApplicationTest

# Build (produces target/quarkus-app/)
mvn package
# Then run: java -jar target/quarkus-app/quarkus-run.jar

# Native build
mvn package -Pnative
```

## Architecture

This is a **Camel Quarkus 3.31.4** application (Java 21) using Quarkus ARC (CDI) for dependency injection. The `quarkus-maven-plugin` drives build and dev mode.

**Wiring model:** Standard CDI — `@ApplicationScoped` beans, `@Produces` + `@Named` for named bean registration, `@ConfigProperty` (MicroProfile Config) for property injection. `RouteBuilder` subclasses annotated with `@ApplicationScoped` are auto-discovered.

**Routes:**
- `ActiveMQRouteBuilder` — transacted JMS: `activemq:inputtest` → log → `activemq:outputtest`
- `RestRouteBuilder` — REST via `platform-http`: `GET /api/hello` → JSON `{"Hello": "World!"}`

**Configuration:**
- `src/main/resources/application.properties` — Quarkus app config (HTTP port 8080, health, JMX, config locations)
- `config/esb.properties` — ActiveMQ broker URL (`esb.brokerUrl`, default `failover:tcp://localhost:61616`)
- `config/credentials.properties` — `esb.password`
- `config/environment.properties` — environment-specific overrides
- `src/main/resources/application-test.properties` — overrides broker URL to `vm://localhost?broker.persistent=false` (embedded broker, no Docker needed for tests)

**`MyConfiguration.java`** produces three CDI beans: `ActiveMQConnectionFactory`, `PooledConnectionFactory` (max 2 connections), and `ActiveMQComponent` (JTA-transacted, `CACHE_NONE`). The `PlatformTransactionManager` wraps the container's `TransactionManager` via `JtaTransactionManager`.

**Testing:** `@QuarkusTest` + `camel-quarkus-junit5`. Tests inject `CamelContext` directly and use `FluentProducerTemplate` / `ConsumerTemplate` to drive and assert routes. The test profile activates `application-test.properties` automatically.

**Observability:** Monitoring séparé du port applicatif (8080) :
- **Jolokia** → `http://localhost:8778/jolokia/` (port dédié, serveur HTTP propre de `camel-quarkus-jolokia`)
- **MicroProfile Health** → `http://localhost:9090/q/health` (`quarkus.management.enabled=true`)
- JMX management activé via `camel.quarkus.management.enabled=true`

**Infrastructure:** `compose.yaml` runs `apache/activemq-classic:latest` — broker port 61616, web console port 8161.
