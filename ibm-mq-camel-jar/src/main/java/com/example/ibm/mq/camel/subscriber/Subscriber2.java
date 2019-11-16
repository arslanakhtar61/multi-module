package com.example.ibm.mq.camel.subscriber;

import org.apache.camel.LoggingLevel;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class Subscriber2 extends SpringRouteBuilder {

    @Override
    public void configure() throws Exception {

        JacksonDataFormat format = new JacksonDataFormat();
        //format.useList();
        format.useMap();
        format.setUnmarshalType(Map.class);

        from("jms-fun:queue:DEV.QUEUE.1")
                //.unmarshal(format)
                .process(exchange -> {
                    //System.out.println(exchange.getIn().getBody());
                    //HashMap<String, Common> map = (HashMap<String, Common>) exchange.getIn().getBody();
                    //Person person = (Person)map.get("person");
                })
                .doTry()
                    .setHeader("requestAction").jsonpath("requestAction", String.class)
                .endDoTry()
                .doCatch(Exception.class)
                    .log(LoggingLevel.INFO, log, "Message body = ${body}, ${exception.message}").stop()
                .end()
                .choice()
                    .when(header("requestAction").isEqualToIgnoreCase("email"))
                        .to("seda:mailbox").stop()
                    .endChoice()
                    .when(header("requestAction").isEqualToIgnoreCase("person"))
                        .to("seda:person").stop()
                    .endChoice()
                    .otherwise()
                        .log("Unkown Mapping")//.stop()
                .end()
                .log("Received Subscriber2: " + "${body}")
                .to("seda:default")
        .end();

        from("seda:mailbox")
                //.unmarshal(format)
                .process(exchange -> {
                    //System.out.println(exchange.getIn().getBody());
                    //HashMap<String, Common> map = (HashMap<String, Common>) exchange.getIn().getBody();
                    //Person person = (Person)map.get("person");
                })
                .log("seda mailbox: " + "${body}")
        .end();

        from("seda:person")
                //.unmarshal(format)
                .process(exchange -> {
                    //System.out.println(exchange.getIn().getBody());
                    //HashMap<String, Common> map = (HashMap<String, Common>) exchange.getIn().getBody();
                    //Person person = (Person)map.get("person");
                })
                .log("seda person: " + "${body}")
        .end();

        from("seda:default")
                //.unmarshal(format)
                .process(exchange -> {
                    //System.out.println(exchange.getIn().getBody());
                    //HashMap<String, Common> map = (HashMap<String, Common>) exchange.getIn().getBody();
                    //Person person = (Person)map.get("person");
                })
                .log("seda default: " + "${body}")
        .end();

    }
}
