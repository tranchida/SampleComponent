# SampleComponent

Apache Camel Quarkus application demonstrating transacted JMS routing and REST endpoints.

## Stack

- **Camel Quarkus 3.31.4** / Java 21
- **ActiveMQ Classic 6.2.0** (JMS broker)
- **Quarkus ARC** (CDI dependency injection)
- **MicroProfile Health & Jolokia** (observability)

## Routes

| Route | Description |
|-------|-------------|
| `activemq:inputtest` → `activemq:outputtest` | Transacted JMS relay with logging |
| `GET /api/hello` | Returns `{"Hello": "World!"}` |

## Prerequisites

- Java 21+
- Maven 3.9+
- Docker (for ActiveMQ broker)

## Running

### 1. Start the broker

```bash
docker compose up -d
```

### 2. Development mode (live reload)

```bash
mvn quarkus:dev
```

### 3. Production build

```bash
mvn package
java -jar target/quarkus-app/quarkus-run.jar
```

### 4. Native build

```bash
mvn package -Pnative
```

## Testing

Tests use an embedded in-VM broker — no Docker required.

```bash
# All tests
mvn test

# Single test
mvn test -Dtest=MyApplicationTest
```

## Configuration

| File | Purpose |
|------|---------|
| `src/main/resources/application.properties` | Quarkus config (ports, health, JMX) |
| `config/esb.properties` | ActiveMQ broker URL (`failover:tcp://localhost:61616`) |
| `config/credentials.properties` | Broker credentials |
| `config/environment.properties` | Environment-specific overrides |

## Ports

| Port | Service |
|------|---------|
| **8080** | Application (`GET /api/hello`) |
| **8778** | Jolokia → `http://localhost:8778/jolokia/` |
| **9090** | MicroProfile Health → `http://localhost:9090/q/health` |
| **61616** | ActiveMQ broker |
| **8161** | ActiveMQ web console |
