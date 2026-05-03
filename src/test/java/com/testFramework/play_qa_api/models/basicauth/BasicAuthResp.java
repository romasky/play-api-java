package com.testFramework.play_qa_api.models.basicauth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BasicAuthResp {
    private Boolean success;
    private String message;
    private String user;
}
