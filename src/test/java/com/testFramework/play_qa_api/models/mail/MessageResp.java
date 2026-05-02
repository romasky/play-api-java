package com.testFramework.play_qa_api.models.mail;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageResp {

    private String id;
    private String from;
    private String subject;

    @JsonProperty("body_preview")
    private String bodyPreview;

    private String body;

    @JsonProperty("html_body")
    private String htmlBody;

    private Map<String, String> headers;

    @JsonProperty("received_at")
    private String receivedAt;
}
