package org.example;

import ch.vd.technical.esb.store.EsbStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import org.apache.camel.Processor;

/**
 * Class to configure the Camel application in Quarkus using CDI.
 */
@ApplicationScoped
public class MyConfiguration {

    @Produces
    @Named("myProcessor")
    public Processor myProcessor(@Named("esbStore") EsbStore esbStore) {
        return new MyProcessor(esbStore);
    }
}
