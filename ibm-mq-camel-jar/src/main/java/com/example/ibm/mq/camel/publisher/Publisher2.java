package com.example.ibm.mq.camel.publisher;

import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class Publisher2 extends SpringRouteBuilder {

    @Override
    public void configure() throws Exception {

        //CronScheduledRoutePolicy startPolicy = new CronScheduledRoutePolicy();
        //startPolicy.setRouteStartTime("0 0/3 * * * ?");

    from("file:{{json.file.path}}?noop=true&idempotent=false&scheduler=spring&scheduler.cron=0/5+*+*+*+*+?")
        //from("quartz://myscheduler?cron=0/5+*+*+*+*+*")
                //.pollEnrich("file:{{json.file.path}}?noop=true&fileName=email.json")
            .convertBodyTo(String.class)
            .process(exchange -> {
                //System.out.println(exchange.getIn().getBody());
            })
            //.log("${body}")
            .to("jms-fun:queue:DEV.QUEUE.1")
    .end();


    }
}
