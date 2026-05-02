package com.testFramework.play_qa_api.models.login;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResp {

    private Boolean success;
    private String message;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("user_id")
    private String userId;

    private String email;
    private String username;

    @JsonProperty("expires_at")
    private String expiresAt;
}
