package com.testFramework.play_qa_api.models.mail;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SendMessageReq {
    private String from;
    private String subject;
    private String body;

    @JsonProperty("html_body")
    private String htmlBody;
}
