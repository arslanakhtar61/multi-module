package com.example.ibm.mq.camel.config;

import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;
import com.ibm.msg.client.wmq.common.CommonConstants;
import org.apache.camel.component.jms.JmsComponent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
import javax.jms.Session;

@Configuration
@EnableJms
@EnableScheduling
public class JMSConfigCustom {

    @Value("${jmsCommponent.ibm.mq.queueManager:null}")
    private String queueManager;

    @Value("${jmsCommponent.ibm.mq.hostName:null}")
    private String hostName;

    @Value("${jmsCommponent.ibm.mq.port:null}")
    private int port;

    @Value("${jmsCommponent.ibm.mq.channel:null}")
    private String channel;

    @Value("${jmsCommponent.ibm.mq.username:null}")
    private String username;

    @Value("${jmsCommponent.ibm.mq.password:null}")
    private String password;

    @Value("${spring.application.name:null}")
    private String appName;

    @Value("${spring.application.transportType:null}")
    private String transportType;

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
            mqConnectionFactory.setQueueManager(queueManager);
            mqConnectionFactory.setHostName(hostName);
            mqConnectionFactory.setPort(port);
            mqConnectionFactory.setChannel(channel);
            //mqConnectionFactory.setIntProperty(CommonConstants.WMQ_CONNECTION_MODE, CommonConstants.WMQ_CM_CLIENT); //https://stackoverflow.com/a/27813520/6434650
            //mqConnectionFactory.setTransportType(CommonConstants.WMQ_CM_CLIENT);
            mqConnectionFactory.setTransportType("BINDINGS".equalsIgnoreCase(transportType) ? WMQConstants.WMQ_CM_BINDINGS : WMQConstants.WMQ_CM_CLIENT);

            //mqConnectionFactory.setClientID(appName);
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
        userCredentialsConnectionFactoryAdapter.setUsername(username);
        userCredentialsConnectionFactoryAdapter.setPassword(password);
        userCredentialsConnectionFactoryAdapter.setTargetConnectionFactory(mqConnectionFactory);
        return userCredentialsConnectionFactoryAdapter;
    }

    @Bean("jms-fun")
    @DependsOn(value = { "userCredentialsConnectionFactoryAdapter-fun" })
    public JmsComponent jmsComponent(@Qualifier("userCredentialsConnectionFactoryAdapter-fun") final UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter, final JmsTransactionManager jmsTransactionManager) {
        JmsComponent jmsComponent = JmsComponent.jmsComponentTransacted(userCredentialsConnectionFactoryAdapter, jmsTransactionManager);
        //jmsComponent.setSubscriptionDurable(true);
        jmsComponent.setAcknowledgementMode(Session.AUTO_ACKNOWLEDGE);
        //jmsComponent.setClientId(appName);
        return jmsComponent;
    }

    @Bean
    @DependsOn(value = { "userCredentialsConnectionFactoryAdapter-fun" })
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