package com.exampleIbm.mq.camel.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonRootName(value = "email")
public class Email implements Common{

    @JsonProperty("to")
    private String to;

    @JsonProperty("body")
    private String body;

    public Email() {
    }

    public Email(String to, String body) {
        this.to = to;
        this.body = body;
    }

    @JsonGetter("to")
    public String getTo() {
        return to;
    }

    @JsonSetter("to")
    public void setTo(String to) {
        this.to = to;
    }

    @JsonGetter("body")
    public String getBody() {
        return body;
    }

    @JsonSetter("body")
    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return String.format("Email{to=%s, body=%s}", getTo(), getBody());
    }

}