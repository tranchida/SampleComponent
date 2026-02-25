package org.example;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.builder.RouteBuilder;

@ApplicationScoped
public class ActiveMQRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("activemq:inputtest")
                .transacted()
                .id("activemq-route")
                .log("Received message from ActiveMQ: ${body}")
                .to("activemq:outputtest");
    }
}
