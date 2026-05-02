package com.testFramework.play_qa_api.models.createUser.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UsersListResp {

    private List<UserResp> users;
    private Integer page;

    @JsonProperty("per_page")
    private Integer perPage;

    @JsonProperty("total_pages")
    private Integer totalPages;
}
