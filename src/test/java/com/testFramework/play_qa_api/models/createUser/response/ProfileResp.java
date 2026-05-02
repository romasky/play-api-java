package com.testFramework.play_qa_api.models.createUser.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProfileResp {

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("middle_name")
    private String middleName;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    private String bio;

    @JsonProperty("date_of_birth")
    private String dateOfBirth;

    private String gender;
    private List<String> interests;
}
