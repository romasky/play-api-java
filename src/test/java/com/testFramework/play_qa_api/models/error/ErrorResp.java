package com.testFramework.play_qa_api.models.error;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorResp {

    private Boolean success;
    private ErrorDetailResp error;
    private String timestamp;

    @JsonProperty("request_id")
    private String requestId;
}
