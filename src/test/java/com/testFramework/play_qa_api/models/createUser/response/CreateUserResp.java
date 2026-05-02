package com.testFramework.play_qa_api.models.createUser.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateUserResp {

    private String id;
    private String email;
    private String username;

    @JsonProperty("access_token")
    private String accessToken;

    private ProfileResp profile;
    private Object contacts;
    private Object address;
    private Object employment;
    private Object settings;
    private MetadataResp metadata;
}
