package com.testFramework.play_qa_api.models.error;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidationErrorResp {
    private String field;
    private String message;
    private Object value;
}
