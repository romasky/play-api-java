package com.testFramework.play_qa_api.models.basicauth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BasicAuthErrorResp {
    private String error;
    private String message;
}
