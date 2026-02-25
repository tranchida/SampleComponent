package org.example;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import jakarta.transaction.TransactionManager;
import jakarta.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.apache.camel.component.activemq.ActiveMQComponent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.jta.JtaTransactionManager;

/**
 * Class to configure the Camel application in Quarkus using CDI.
 */
@ApplicationScoped
public class MyConfiguration {

    @Produces
    @Named("activemqConnectionFactory")
    public ActiveMQConnectionFactory connectionFactory(
            @ConfigProperty(name = "esb.brokerUrl") String brokerUrl,
            @ConfigProperty(name = "esb.username") String username,
            @ConfigProperty(name = "esb.password") String password) {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();
        factory.setBrokerURL(brokerUrl);
        factory.setUserName(username);
        factory.setPassword(password);
        return factory;
    }

    @Produces
    @Named("pooledConnectionFactory")
    public ConnectionFactory pooledConnectionFactory(@Named("activemqConnectionFactory") ActiveMQConnectionFactory cf) {
        PooledConnectionFactory pooled = new PooledConnectionFactory();
        pooled.setConnectionFactory(cf);
        pooled.setMaxConnections(2);
        return pooled;
    }

    @Produces
    public PlatformTransactionManager transactionManager(TransactionManager tm) {
        return new JtaTransactionManager(tm);
    }

    @Produces
    @Named("activemq")
    public ActiveMQComponent activeMQComponent(
            @Named("pooledConnectionFactory") ConnectionFactory cf,
            PlatformTransactionManager tm
    ) {
        ActiveMQComponent jms = new ActiveMQComponent();
        jms.setConnectionFactory(cf);
        jms.setTransactionManager(tm);
        jms.setTransacted(true);
        jms.setCacheLevelName("CACHE_NONE"); // important avec JTA
        return jms;
    }

}
