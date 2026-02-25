package org.example;

import java.util.concurrent.TimeUnit;
import jakarta.inject.Inject;
import org.apache.camel.CamelContext;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Producer;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.NotifyBuilder;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A simple unit test showing how to test the application in Quarkus.
 */
@QuarkusTest
class MyApplicationTest {

    @Inject
    CamelContext context;

    @Test
    void testRestRoute() {
        var response = context.createFluentProducerTemplate()
                .to("http://localhost:8081/api/hello")
                .request(String.class);

        Assertions.assertEquals("{\"Hello\":\"World!\"}", response);
    }


    @Test
    void testJmsRoute() {

        context.createFluentProducerTemplate()
                .to("activemq:inputtest")
                .withBody("Test Message")
                .send();

        var exchange = context.createConsumerTemplate()
                .receive("activemq:outputtest", 5000);
        Assertions.assertNotNull(exchange);
        String body = exchange.getIn().getBody(String.class);
        Assertions.assertEquals("Test Message", body);

    }
}
