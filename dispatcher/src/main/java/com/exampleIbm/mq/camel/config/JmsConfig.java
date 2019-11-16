package com.exampleIbm.mq.camel.config;

import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.jms.ConnectionFactory;
import javax.jms.Session;

@Configuration
@EnableScheduling
public class JmsConfig {

    private Logger log = LogManager.getLogger(JmsConfig.class);

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

    @Value("${spring.application.timeout:null}")
    private String timeout;

    @Bean("mqQueueConnectionFactory-dispatcher")
    @Primary
    public MQQueueConnectionFactory mqQueueConnectionFactory(){
        MQQueueConnectionFactory mqQueueConnectionFactory = new MQQueueConnectionFactory();
        try {
            mqQueueConnectionFactory.setHostName(hostName);
            mqQueueConnectionFactory.setQueueManager(queueManager);
            mqQueueConnectionFactory.setPort(port);

            if(StringUtils.isNotBlank(channel)){
                mqQueueConnectionFactory.setChannel(channel);
            }
            mqQueueConnectionFactory.setTransportType("BINDINGS".equalsIgnoreCase(transportType) ? WMQConstants.WMQ_CM_BINDINGS : WMQConstants.WMQ_CM_CLIENT);
            mqQueueConnectionFactory.setCCSID(1208);
        } catch (Exception e) {
            log.error("create mq connection failed", e);
        }
        return mqQueueConnectionFactory;
    }

    @Bean (name="userCredentialsConnectionFactoryAdapter-dispatcher")
    @DependsOn(value = { "mqQueueConnectionFactory-dispatcher" })
    @Primary
    public UserCredentialsConnectionFactoryAdapter getUserCredentialsConnectionFactoryAdapter(
            @Qualifier("mqQueueConnectionFactory-dispatcher") final MQQueueConnectionFactory mqQueueConnectionFactory) {
        UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter = new UserCredentialsConnectionFactoryAdapter();
        userCredentialsConnectionFactoryAdapter.setUsername(username);
        userCredentialsConnectionFactoryAdapter.setPassword(password);
        userCredentialsConnectionFactoryAdapter.setTargetConnectionFactory(mqQueueConnectionFactory);
        return userCredentialsConnectionFactoryAdapter;
    }

    @Bean("jmsFactory-dispatcher")
    @DependsOn(value = {"userCredentialsConnectionFactoryAdapter-dispatcher"})
    public JmsListenerContainerFactory<?> jmsFactory(@Qualifier("userCredentialsConnectionFactoryAdapter-dispatcher") UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter,
                                                     DefaultJmsListenerContainerFactoryConfigurer configurer){
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
        factory.setReceiveTimeout(NumberUtils.toLong(timeout));
        configurer.configure(factory, userCredentialsConnectionFactoryAdapter);
        return factory;
    }

    @Bean("queueTemplate-dispatcher")
    @DependsOn(value = {"userCredentialsConnectionFactoryAdapter-dispatcher", "jacksonJmsMessageConverter-dispatcher"})
    public JmsTemplate queueTemplate(@Qualifier("userCredentialsConnectionFactoryAdapter-dispatcher") UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter,
                                     @Qualifier("jacksonJmsMessageConverter-dispatcher") MessageConverter jacksonJmsMessageConverter){
        JmsTemplate jmsTemplate = new JmsTemplate(userCredentialsConnectionFactoryAdapter);
        jmsTemplate.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
        jmsTemplate.setMessageConverter(jacksonJmsMessageConverter);
        jmsTemplate.setReceiveTimeout(NumberUtils.toLong(timeout));
        return jmsTemplate;
    }

    // Serialize message content to json using TextMessage
    @Bean("jacksonJmsMessageConverter-dispatcher")
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }


}
