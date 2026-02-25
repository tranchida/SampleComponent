package org.example;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;

@ApplicationScoped
public class RestRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {

            restConfiguration().component("platform-http").bindingMode(RestBindingMode.json);

            rest("/api")
                .get("/hello")
                    .id("hello-route")

                    .to("direct:hello");

            from("direct:hello")
                .setBody().constant(java.util.Map.of("Hello", "World!"));

    }
}
