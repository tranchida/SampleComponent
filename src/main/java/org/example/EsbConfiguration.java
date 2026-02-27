package org.example;

import ch.vd.technical.esb.store.EsbStore;
import ch.vd.technical.esb.store.raft.RaftEsbStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import jakarta.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.activemq.ActiveMQComponent;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Class to configure the Camel application in Quarkus using CDI.
 */
@ApplicationScoped
public class EsbConfiguration {

    @Produces
    @Named("ack")
    public Processor ack(ProducerTemplate producerTemplate,
                         @ConfigProperty(name = "esb.application") String application,
                         @ConfigProperty(name = "esb.domain") String domain) {

        var ack = new AckEsbMessageProcessor();
        ack.setApplication(application);
        ack.setDomain(domain);
        return ack;
    }

    @Produces
    @Named("esbStore")
    public EsbStore esbStore(@ConfigProperty(name = "esb.raft.endpoint") String endpoint) {
        var store = new RaftEsbStore();
        store.setEndpoint(endpoint);
        return store;
    }

    @Produces
    @Named("activemqConnectionFactory")
    public ConnectionFactory connectionFactory(
            @ConfigProperty(name = "esb.brokerUrl") String brokerUrl,
            @ConfigProperty(name = "esb.username") String username,
            @ConfigProperty(name = "esb.password") String password) {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();
        factory.setBrokerURL(brokerUrl);
        factory.setUserName(username);
        factory.setPassword(password);
        RedeliveryPolicy policy = new RedeliveryPolicy();
        policy.setInitialRedeliveryDelay(1000);
        policy.setMaximumRedeliveries(1);
        factory.setRedeliveryPolicy(policy);
        return factory;
    }

    @Produces
    @Named("pooledConnectionFactory")
    public PooledConnectionFactory pooledConnectionFactory(
            @Named("activemqConnectionFactory") ConnectionFactory cf) {
        PooledConnectionFactory pooled = new PooledConnectionFactory();
        pooled.setConnectionFactory(cf);
        pooled.setMaxConnections(1);
        pooled.start();
        return pooled;
    }

    public void closePooledConnectionFactory(
            @Disposes @Named("pooledConnectionFactory") PooledConnectionFactory pooled) {
        pooled.stop();
    }

    @Produces
    @Named("activemq")
    public ActiveMQComponent activeMQComponent(
            @Named("pooledConnectionFactory") ConnectionFactory cf) {
        ActiveMQComponent jms = new ActiveMQComponent();
        jms.setConnectionFactory(cf);
        jms.setTransacted(true);
        jms.setCacheLevelName("CACHE_CONSUMER");
        return jms;
    }

}
