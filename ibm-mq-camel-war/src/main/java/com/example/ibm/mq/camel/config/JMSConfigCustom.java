package com.example.ibm.mq.camel.config;

import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.msg.client.wmq.common.CommonConstants;
import org.apache.camel.component.jms.JmsComponent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.jms.ConnectionFactory;

@Configuration
@EnableJms
@EnableScheduling
public class JMSConfigCustom {

    @Bean
    @DependsOn(value = { "mqConnectionFactory-fun" })
    public JmsTransactionManager jmsTransactionManager(@Qualifier("mqConnectionFactory-fun") final MQConnectionFactory mqConnectionFactory) {
        JmsTransactionManager jmsTransactionManager = new JmsTransactionManager();
        jmsTransactionManager.setConnectionFactory(mqConnectionFactory);
        return jmsTransactionManager;
    }

    @Bean("mqConnectionFactory-fun")
    @Primary
    public MQConnectionFactory mqConnectionFactory() {
        MQConnectionFactory mqConnectionFactory = new MQConnectionFactory();
        try{
            mqConnectionFactory.setQueueManager("QM1");
            mqConnectionFactory.setHostName("localhost");
            mqConnectionFactory.setPort(1414);
            mqConnectionFactory.setChannel("DEV.APP.SVRCONN");
            //mqConnectionFactory.setIntProperty(CommonConstants.WMQ_CONNECTION_MODE, CommonConstants.WMQ_CM_CLIENT); //https://stackoverflow.com/a/27813520/6434650
            mqConnectionFactory.setTransportType(CommonConstants.WMQ_CM_CLIENT);
        }catch (Exception e){

        }
        return mqConnectionFactory;
    }

    @Bean (name="userCredentialsConnectionFactoryAdapter-fun")
    @DependsOn(value = { "mqConnectionFactory-fun" })
    @Primary
    public UserCredentialsConnectionFactoryAdapter getUserCredentialsConnectionFactoryAdapter(
            @Qualifier("mqConnectionFactory-fun") final MQConnectionFactory mqConnectionFactory) {
        UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter = new UserCredentialsConnectionFactoryAdapter();
        userCredentialsConnectionFactoryAdapter.setUsername("admin");
        userCredentialsConnectionFactoryAdapter.setPassword("passw0rd");
        userCredentialsConnectionFactoryAdapter.setTargetConnectionFactory(mqConnectionFactory);
        return userCredentialsConnectionFactoryAdapter;
    }

    @Bean//("jms")
    public JmsComponent jmsComponent(@Qualifier("userCredentialsConnectionFactoryAdapter-fun") final UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter, final JmsTransactionManager jmsTransactionManager) {
        JmsComponent jmsComponent = JmsComponent.jmsComponentTransacted(userCredentialsConnectionFactoryAdapter, jmsTransactionManager);
        jmsComponent.setSubscriptionDurable(true);
        return jmsComponent;
    }

    @Bean
    public JmsListenerContainerFactory<?> jmsListenerContainerFactory(@Qualifier("userCredentialsConnectionFactoryAdapter-fun") final UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter,
                                                                      DefaultJmsListenerContainerFactoryConfigurer configurer) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        // This provides all boot's default to this factory, including the message converter
        configurer.configure(factory, userCredentialsConnectionFactoryAdapter);
        // You could still override some of Boot's default if necessary.
        //factory.setPubSubDomain(true); //https://github.com/ibm-messaging/mq-jms-spring/issues/22#issuecomment-481459656
        return factory;
    }

    @Bean // Serialize message content to json using TextMessage
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }

}