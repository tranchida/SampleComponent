package org.example;

import ch.vd.technical.esb.camel.CamelEsbMessageAdapter;
import ch.vd.technical.esb.store.EsbStore;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.support.DefaultMessage;

public class MyProcessor implements Processor {

    private final EsbStore esbStore;

    public MyProcessor(EsbStore esbStore) {
        this.esbStore = esbStore;
    }

    @Override
    public void process(Exchange exchange) throws Exception {

        var in = exchange.getIn();
        var msg = new CamelEsbMessageAdapter(new DefaultMessage(exchange), esbStore, true);

        msg.setBusinessId(in.getMessageId());
        msg.setBusinessUser("esb");
        msg.setApplication("samplecomponent");
        msg.setDomain("test");
        msg.setContext("test");
        msg.setBodyEmpty();

        exchange.setMessage(msg.getMessage());
    }
}
