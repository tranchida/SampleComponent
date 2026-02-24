package org.example;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.BindToRegistry;
import org.apache.camel.Configuration;
import org.apache.camel.PropertyInject;
import org.apache.camel.component.activemq.ActiveMQComponent;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.connection.JmsTransactionManager;

/**
 * Class to configure the Camel application.
 */
@Configuration
public class MyConfiguration {

    @BindToRegistry
    public MyBean myBean(@PropertyInject("hi") String hi, @PropertyInject("bye") String bye) {
        // this will create an instance of this bean with the name of the method (eg myBean)
        return new MyBean(hi, bye);
    }

    @BindToRegistry
    public CachingConnectionFactory activeMQConnectionFactory(
            @PropertyInject("activemq.broker-url") String brokerUrl,
            @PropertyInject("activemq.username") String username,
            @PropertyInject("activemq.password") String password) {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        factory.setUserName(username);
        factory.setPassword(password);

        CachingConnectionFactory cachingFactory = new CachingConnectionFactory(factory);
        cachingFactory.setSessionCacheSize(10);
        cachingFactory.setReconnectOnException(true);
        return cachingFactory;
    }

    @BindToRegistry
    public JmsTransactionManager transactionManager(CachingConnectionFactory activeMQConnectionFactory) {
        return new JmsTransactionManager(activeMQConnectionFactory);
    }

    @BindToRegistry("activemq")
    public ActiveMQComponent activeMQComponent(
            CachingConnectionFactory activeMQConnectionFactory,
            JmsTransactionManager transactionManager) {
        ActiveMQComponent component = new ActiveMQComponent();
        component.setConnectionFactory(activeMQConnectionFactory);
        component.setTransactionManager(transactionManager);
        component.setTransacted(true);
        component.setCacheLevelName("CACHE_NONE");
        return component;
    }

}

