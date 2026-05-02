package com.testFramework.play_qa_api.models.error;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorDetailResp {
    private String code;
    private String message;
    private String details;
    private String field;
    private List<ValidationErrorResp> validation;
}
