package com.exampleIbm.mq.camel.sender;

import com.exampleIbm.mq.camel.model.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;

@Component
public class Sender1 {

    @Autowired
    @Qualifier("queueTemplate-dispatcher")
    private JmsTemplate jmsTemplate;

    @Value("${jmsCommponent.ibm.mq.queue:null}")
    private String queue;

    @Bean
    @Scheduled(fixedRate = 5000)
    public String send(){
        Person person = new Person("Hello", "World");
        try{
            System.out.println("Sending from Sender1");
            jmsTemplate.convertAndSend(queue, person, new MessagePostProcessor() {
                public Message postProcessMessage(Message message) throws JMSException {
                    message.setIntProperty("AccountID", 1234);
                    message.setJMSCorrelationID("123-00001");
                    return message;
                }
            });
            return "OK";
        }catch(JmsException ex){
            ex.printStackTrace();
            return "FAIL";
        }
    }

}
