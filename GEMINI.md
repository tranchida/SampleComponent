# GEMINI.md - Apache Camel Main Project Context

This project is a standalone **Apache Camel 4.18.0** application using **Java 17** and **Maven**. It demonstrates how to run Camel without a heavyweight container like Spring Boot or Quarkus, leveraging Camel's built-in `Main` class and dependency injection.

## Project Overview

- **Architecture**: Standalone Camel Main application.
- **Core Technologies**: Apache Camel, ActiveMQ Classic (via Docker), Jolokia, SLF4J (Simple), JUnit 5.
- **Wiring Model**: Uses Camel's internal DI system:
  - `@Configuration`: Marks a class as a configuration source (e.g., `MyConfiguration.java`).
  - `@BindToRegistry`: Registers beans into the Camel registry via factory methods.
  - `@PropertyInject`: Injects values from `application.properties`.
- **Infrastructure**: Includes a `compose.yaml` for running an ActiveMQ Classic broker locally.

## Building and Running

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker (for ActiveMQ)

### Commands
| Task | Command |
| :--- | :--- |
| **Start Infrastructure** | `docker compose up -d` |
| **Build Project** | `mvn install` |
| **Run Application** | `mvn camel:run` |
| **Run All Tests** | `mvn test` |
| **Run Single Test** | `mvn test -Dtest=MyApplicationTest` |
| **Create Fat Jar** | `mvn package` |
| **Run Fat Jar** | `java -jar target/SampleComponent-1.0-SNAPSHOT-executable-jar.jar` |

## Development Conventions

- **Routing**: Routes are defined in classes extending `RouteBuilder` (e.g., `MyRouteBuilder`, `ActiveMQRouteBuilder`) and are auto-detected by Camel Main if they are in the same package (or sub-packages) as the main class.
- **Configuration**:
  - Global properties are in `src/main/resources/application.properties`.
  - Bean wiring and component setup (like ActiveMQ) are done in `MyConfiguration.java`.
- **ActiveMQ Integration**:
  - Configured with transactions (`.transacted()`) and a `JmsTransactionManager`.
  - Connection pooling is available (commented out in `pom.xml` but prepared in `MyConfiguration`).
- **Testing**:
  - Tests extend `CamelMainTestSupport` from `camel-test-main-junit5`.
  - Use `NotifyBuilder` for integration testing to verify route completion without manual mocks.

## JMX and Monitoring

- **Jolokia**: Enabled on port `8778` by default.
- **URL**: `http://localhost:8778/jolokia/`
- You can use tools like **Hawtio** or simple `curl` commands to query Camel MBeans via this endpoint.

## Key Files

- `src/main/java/org/example/MyApplication.java`: The entry point that initializes `org.apache.camel.main.Main`.
- `src/main/java/org/example/MyConfiguration.java`: Centralized bean and component configuration.
- `src/main/resources/application.properties`: Main configuration file.
- `compose.yaml`: Infrastructure definition for local development.
