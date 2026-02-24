package org.example;

import org.apache.camel.builder.RouteBuilder;

public class ActiveMQRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("activemq:{{activemq.queue}}")
                .transacted()
                .log("Message reçu depuis ActiveMQ : ${body}")
                .to("activemq:finaldest")
                .throwException(new RuntimeException("Oups"));
    }
}