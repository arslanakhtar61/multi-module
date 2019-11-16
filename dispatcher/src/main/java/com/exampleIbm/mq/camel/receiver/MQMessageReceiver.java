package com.exampleIbm.mq.camel.receiver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

@Component
public class MQMessageReceiver {

    private static final Logger logger = LogManager.getLogger(MQMessageReceiver.class);
    private static final int MAX_BUFFER_SIZE = 4096;

    @JmsListener(destination = "${jmsCommponent.ibm.mq.queue:null}", containerFactory = "jmsFactory-dispatcher")
    public void receiveMessage(Message message){
        String textMessage = null;
        try {
            if(message instanceof TextMessage){
                textMessage = ((TextMessage) message).getText();
            }else if(message instanceof BytesMessage){
                textMessage = parseByteMessage(message);
            }
        } catch (JMSException e) {
            logger.error("failed to receiveMessage{}", e);
        }
        System.out.println("Receiving from MQMessageReceiver " +  textMessage);

    }

    private String parseByteMessage(Message message) throws JMSException {
        byte[] buffer = new byte[MAX_BUFFER_SIZE];
        int size = -1;
        StringBuilder sb = new StringBuilder(MAX_BUFFER_SIZE / 4);
        while( (size = ((BytesMessage) message).readBytes(buffer)) > 0 ){
            sb.append(new String(buffer, 0, size));
        }
        return sb.toString();
    }
}
