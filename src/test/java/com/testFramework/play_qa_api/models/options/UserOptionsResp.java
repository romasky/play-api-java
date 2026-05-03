package com.testFramework.play_qa_api.models.options;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserOptionsResp {

    @JsonProperty("allowed_methods")
    private List<String> allowedMethods;

    private Map<String, String> endpoints;
    private AuthInfo authentication;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AuthInfo {
        private String type;
        private String header;
        private String format;

        @JsonProperty("required_for")
        private List<String> requiredFor;
    }
}
