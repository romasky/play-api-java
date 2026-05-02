package com.testFramework.play_qa_api.models.createUser.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserResp {

    private String id;
    private String email;
    private String username;
    private ProfileResp profile;
    private Object contacts;
    private Object address;
    private Object employment;
    private Object settings;
    private MetadataResp metadata;
}
