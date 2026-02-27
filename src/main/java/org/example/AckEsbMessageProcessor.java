package org.example;

import ch.vd.technical.esb.EsbMessage;
import ch.vd.technical.esb.EsbMessageImpl;
import ch.vd.technical.esb.util.EsbClientVersion;
import ch.vd.technical.esb.util.EsbProviderVersion;
import ch.vd.technical.esb.util.TechnicalConstants;
import jakarta.jms.Queue;
import lombok.Setter;
import org.apache.camel.*;
import org.apache.camel.support.DefaultExchange;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class AckEsbMessageProcessor implements Processor {

    @Setter
    private String application;
    @Setter
    private String domain;

    private final SimpleDateFormat sdf = new SimpleDateFormat(EsbMessage.DATE_FORMAT);

    public void process(Exchange exchange) throws Exception {

        try (var producer = exchange.getContext().createProducerTemplate(1)) {
            // in message
            Message in = exchange.getIn();

            // ack message
            Exchange ex = new DefaultExchange(exchange.getContext(), ExchangePattern.InOnly);
            Message ackMessage = ex.getIn();

            ackMessage.setHeaders(new HashMap<>(in.getHeaders()));

            Date now = new Date();
            ackMessage.setHeader(EsbMessage.MESSAGE_SEND_DATE, sdf.format(now));
            ackMessage.setHeader(EsbMessage.MESSAGE_CREATION_DATE, sdf.format(now));
            ackMessage.setHeader(EsbMessageImpl.ESB_ACK_MESSAGE_ID, in.getHeader(EsbMessage.MESSAGE_ID));

            // queue name
            Queue queue = (Queue) in.getHeader("JMSDestination");
            // JMSDestination if available, otherwise use serviceDestination
            Object ackDestination = queue != null ? queue.getQueueName() : in.getHeader(EsbMessage.SERVICE_DESTINATION);

            ackMessage.setHeader(EsbMessageImpl.ESB_ACK_DESTINATION, ackDestination);
            ackMessage.setHeader(EsbMessageImpl.ESB_ORIG_DOMAIN, in.getHeader(EsbMessage.DOMAIN));
            ackMessage.setHeader(EsbMessageImpl.ESB_ORIG_CONTEXT, in.getHeader(EsbMessage.CONTEXT));
            ackMessage.setHeader(EsbMessageImpl.ESB_ORIG_APPLICATION, in.getHeader(EsbMessage.APPLICATION));

            ackMessage.setHeader(EsbMessage.DOMAIN, domain);
            ackMessage.setHeader(EsbMessage.CONTEXT, EsbMessageImpl.ESB_CONTEXT_ACK);
            ackMessage.setHeader(EsbMessage.APPLICATION, application);
            ackMessage.setHeader(EsbMessage.SERVICE_DESTINATION, TechnicalConstants.AUDIT_QUEUE_NAME);

            ackMessage.setHeader(EsbMessage.MESSAGE_IS_ACK, Boolean.TRUE.toString());
            ackMessage.setHeader(EsbMessage.MESSAGE_ID, UUID.randomUUID().toString());

            ackMessage.setHeader(EsbMessageImpl.ESB_CLIENT_VERSION, EsbClientVersion.getVersion());
            ackMessage.setHeader(EsbMessageImpl.ESB_PROVIDER_VERSION, EsbProviderVersion.getVersion());

            ackMessage.setBody("<?xml version=\"1.0\" encoding=\"UTF-8\"?><empty/>");

            producer.send("activemq:audit", ex);

            // update headers for current exchange
            in.setHeader(EsbMessageImpl.ESB_ORIG_DOMAIN, in.getHeader(EsbMessage.DOMAIN));
            in.setHeader(EsbMessageImpl.ESB_ORIG_CONTEXT, in.getHeader(EsbMessage.CONTEXT));
            in.setHeader(EsbMessageImpl.ESB_ORIG_APPLICATION, in.getHeader(EsbMessage.APPLICATION));
            in.setHeader(EsbMessageImpl.ESB_ORIG_MESSAGE_ID, in.getHeader(EsbMessage.MESSAGE_ID));

            in.setHeader(EsbMessage.DOMAIN, domain);
            in.setHeader(EsbMessage.APPLICATION, application);

            // generate new message Id
            in.setHeader(EsbMessage.MESSAGE_ID, UUID.randomUUID().toString());
        }
    }

}
