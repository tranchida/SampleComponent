# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
mvn install

# Run the application (keeps running until ctrl+c)
mvn camel:run

# Run tests
mvn test

# Run a single test
mvn test -Dtest=MyApplicationTest

# Build executable fat jar
mvn package
# Then run: java -jar target/SampleComponent-1.0-SNAPSHOT-executable-jar.jar
```

## Architecture

This is a **Camel Main** standalone application (Apache Camel 4.18.0, Java 17) that runs without Spring or Quarkus. It uses `camel-main` to bootstrap routing via `Main.run()`.

**Wiring model:** Configuration and bean registration use Camel's built-in DI annotations rather than a framework container:
- `@Configuration` on a class marks it as a configuration source
- `@BindToRegistry("name")` on a factory method registers the returned object into the Camel registry under the method name
- `@PropertyInject("key")` injects values from `application.properties`
- Route builders (`RouteBuilder` subclasses) are auto-detected from the same package as `MyApplication`

**Route:** A timer fires every `myPeriod` ms → calls `myBean.hello()` → logs → calls `myBean.bye()` → logs.

**Testing:** Tests extend `CamelMainTestSupport` (from `camel-test-main-junit5`) and override `getMainClass()`. Use `NotifyBuilder` to assert on message completion rather than mocking.

**Key files:**
- `src/main/resources/application.properties` — configures camel name, timer period, and bean string values (`hi`, `bye`)
- `MyConfiguration.java` — factory for `MyBean`, wired via `@BindToRegistry` + `@PropertyInject`
- `MyRouteBuilder.java` — defines the single route