package com.example.ibm.mq.camel.publisher;

import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class Publisher1 extends SpringRouteBuilder {

    @Override
    public void configure() throws Exception {

        //CronScheduledRoutePolicy startPolicy = new CronScheduledRoutePolicy();
        //startPolicy.setRouteStartTime("0 0/3 * * * ?");

    from("file:{{json.file.path}}?noop=true&idempotent=false&fileName=email.json&scheduler=spring&scheduler.cron=0/5+*+*+*+*+?")
        //from("quartz://myscheduler?cron=0/5+*+*+*+*+*")
                //.pollEnrich("file:{{json.file.path}}?noop=true&fileName=email.json")
            .convertBodyTo(String.class)
            .process(exchange -> {
                //System.out.println(exchange.getIn().getBody());
            })
            .log("${body}")
            .to("jms:topic:dev/mailbox")
    .end();

    from("file:{{xml.file.path}}?noop=true&idempotent=false&scheduler=spring&scheduler.cron=0/5+*+*+*+*+?")
        //from("quartz://myscheduler?cron=0/5+*+*+*+*+*")
        //.pollEnrich("file:{{json.file.path}}?noop=true&fileName=email.json")
        .convertBodyTo(String.class)
        .process(exchange -> {
            //System.out.println(exchange.getIn().getBody());
        })
        //https://stackoverflow.com/a/43129060/6434650
        //https://stackoverflow.com/a/4402743/6434650
        .setHeader("requestAction").xpath("//*[local-name()='name1']", String.class)
        .process(exchange -> {
            //System.out.println(exchange.getIn().getBody());
        })
        //.log("${body}")
        .to("jms:topic:dev/mailbox2")
    .end();

    }
}
