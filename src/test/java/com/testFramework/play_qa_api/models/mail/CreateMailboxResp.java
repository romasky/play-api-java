package com.testFramework.play_qa_api.models.mail;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateMailboxResp {

    private String id;
    private String token;

    @JsonProperty("email_address")
    private String emailAddress;

    private String domain;

    @JsonProperty("expires_at")
    private String expiresAt;

    @JsonProperty("created_at")
    private String createdAt;
}
