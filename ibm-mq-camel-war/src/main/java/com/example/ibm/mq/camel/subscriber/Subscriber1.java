package com.example.ibm.mq.camel.subscriber;

import com.example.ibm.mq.camel.model.Common;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

//@Component
public class Subscriber1 extends SpringRouteBuilder {

    @Override
    public void configure() throws Exception {

        JacksonDataFormat format = new JacksonDataFormat();
        //format.useList();
        format.useMap();
        format.setUnmarshalType(Map.class);

        from("jms-fun:topic:dev/mailbox?durableSubscriptionName=app1.1&clientId=app1.1")
                .unmarshal(format)
                .process(exchange -> {
                    System.out.println(exchange.getIn().getBody());
                    HashMap<String, Common> map = (HashMap<String, Common>) exchange.getIn().getBody();
                    //Person person = (Person)map.get("person");
                })
                .log("Received Subscriber1: " + "${body}")
        .end();

//        from("jms-fun:topic:dev/mailbox?durableSubscriptionName=app1.2&clientId=app1.2")
//                .unmarshal(format)
//                .process(exchange -> {
//                    System.out.println(exchange.getIn().getBody());
//                    HashMap<String, Common> map = (HashMap<String, Common>) exchange.getIn().getBody();
//                    //Person person = (Person)map.get("person");
//                })
//                .log("Received Subscribe2: " + "${body}")
//        .end();

//        from("jms-fun:topic:dev/mailbox")
//            .unmarshal(format)
//            .process(exchange -> {
//                System.out.println(exchange.getIn().getBody());
//                HashMap<String, Common> map = (HashMap<String, Common>) exchange.getIn().getBody();
//                //Person person = (Person)map.get("person");
//            })
//            .log("Received Subscribe3: " + "${body}")
//        .end();



    }
}
