package org.example;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;

public class RestRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {

            restConfiguration().bindingMode(RestBindingMode.json);

            rest("/api")
                .get("/hello")
                    .to("direct:hello");

            from("direct:hello")
                .setBody().constant("{ \"Hello\" : \"World!\" }");

    }
}
